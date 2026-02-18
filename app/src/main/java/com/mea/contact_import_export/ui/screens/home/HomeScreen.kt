package com.mea.contact_import_export.ui.screens.home

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mea.contact_import_export.R
import com.mea.contact_import_export.ui.screens.exportcontacts.ExportContactsScreenViewModel
import com.mea.contact_import_export.ui.screens.home.components.ContactsScreen
import com.mea.contact_import_export.ui.screens.home.components.HomeBottomNavigationBar
import com.mea.contact_import_export.ui.screens.home.components.ImportExportScreen
import com.mea.contact_import_export.ui.screens.home.components.PlaceholderTabScreen
import com.mea.contact_import_export.ui.screens.importcontacts.ImportContactsScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun AppContent(
    importContactsViewModel: ImportContactsScreenViewModel = hiltViewModel(),
    exportContactsViewModel: ExportContactsScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val exportUiState by exportContactsViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedMainTab = MainTab.fromRoute(currentRoute)
    val showBottomBar = selectedMainTab != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isPhoneImporting by rememberSaveable { mutableStateOf(false) }
    var phoneImportRequested by rememberSaveable { mutableStateOf(false) }
    var navigateToContactsAfterImport by rememberSaveable { mutableStateOf(false) }
    var previousLoadingState by remember { mutableStateOf(false) }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        exportUiState.hasPermission = isGranted
        if (isGranted) {
            exportContactsViewModel.loadContacts()
        } else if (phoneImportRequested) {
            isPhoneImporting = false
            phoneImportRequested = false
        }
    }

    val importFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null && activity != null) {
                importContactsViewModel.showAdAndProcessVcfFile(activity, activity, uri)
                Toast.makeText(context, context.getString(R.string.import_started), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        importContactsViewModel.setLauncher(importFilePickerLauncher)
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        exportUiState.hasPermission = hasPermission
    }

    LaunchedEffect(exportUiState.isLoading, exportUiState.hasPermission, phoneImportRequested) {
        if (!phoneImportRequested) {
            previousLoadingState = exportUiState.isLoading
            return@LaunchedEffect
        }

        if (!exportUiState.hasPermission && !exportUiState.isLoading) {
            isPhoneImporting = false
            phoneImportRequested = false
            navigateToContactsAfterImport = false
            previousLoadingState = false
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.phone_import_permission_required))
            }
            return@LaunchedEffect
        }

        if (previousLoadingState && !exportUiState.isLoading) {
            val message = if (exportUiState.allContacts.isEmpty()) {
                context.getString(R.string.no_phone_contacts_found)
            } else {
                context.getString(R.string.phone_import_completed)
            }
            isPhoneImporting = false
            phoneImportRequested = false
            if (navigateToContactsAfterImport) {
                navController.navigate(MainTab.Contacts.route) {
                    launchSingleTop = true
                }
                navigateToContactsAfterImport = false
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
        previousLoadingState = exportUiState.isLoading
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNavigationBar(
                    selectedMainTab = selectedMainTab,
                    onTabSelected = { tab ->
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Contacts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTab.Contacts.route) {
                ContactsScreen(
                    contacts = exportUiState.allContacts,
                    searchText = exportUiState.searchText,
                    selectedContacts = exportUiState.selectedContacts,
                    onSearchTextChange = exportContactsViewModel::searchContacts,
                    onSync = {
                        exportContactsViewModel.syncContacts {
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    },
                    onContactClick = exportContactsViewModel::updateSelectedContacts,
                    onSelectContacts = { contacts ->
                        exportContactsViewModel.updateSelectedContacts(contacts.toMutableList())
                    },
                    onExportSelected = {
                        if (activity == null) return@ContactsScreen
                        exportContactsViewModel.showAdAndExportContacts(activity, activity)
                    },
                    onDeleteSelected = {
                        val removedCount = exportContactsViewModel.removeSelectedContactsFromList()
                        if (removedCount > 0) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.contacts_deleted_count, removedCount),
                                    actionLabel = context.getString(R.string.undo_label),
                                    withDismissAction = true
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    exportContactsViewModel.undoLastDeletedContacts()
                                }
                            }
                        }
                    },
                    onMergeSelected = {
                        Toast.makeText(context, R.string.merge_coming_soon, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable(MainTab.Import.route) {
                ImportExportScreen(
                    selectedTab = ImportExportTab.Import,
                    onSelectTab = { selectedTab ->
                        val targetRoute = if (selectedTab == ImportExportTab.Import) {
                            MainTab.Import.route
                        } else {
                            MainTab.Export.route
                        }
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onImportPhone = {
                        isPhoneImporting = true
                        phoneImportRequested = true
                        navigateToContactsAfterImport = true
                        exportContactsViewModel.syncContacts {
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    },
                    isImportingPhone = isPhoneImporting,
                    onImportVcf = { importContactsViewModel.selectVcfFile() },
                    onExportToVcf = {
                        if (activity == null) return@ImportExportScreen
                        val selected = if (exportUiState.selectedContacts.isEmpty()) {
                            exportUiState.allContacts.toMutableList()
                        } else {
                            exportUiState.selectedContacts.toMutableList()
                        }
                        exportContactsViewModel.updateSelectedContacts(selected)
                        exportContactsViewModel.showAdAndExportContacts(activity, activity)
                    }
                )
            }
            composable(MainTab.Export.route) {
                ImportExportScreen(
                    selectedTab = ImportExportTab.Export,
                    onSelectTab = { selectedTab ->
                        val targetRoute = if (selectedTab == ImportExportTab.Import) {
                            MainTab.Import.route
                        } else {
                            MainTab.Export.route
                        }
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onImportPhone = {
                        isPhoneImporting = true
                        phoneImportRequested = true
                        navigateToContactsAfterImport = true
                        exportContactsViewModel.syncContacts {
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    },
                    isImportingPhone = isPhoneImporting,
                    onImportVcf = { importContactsViewModel.selectVcfFile() },
                    onExportToVcf = {
                        if (activity == null) return@ImportExportScreen
                        val selected = if (exportUiState.selectedContacts.isEmpty()) {
                            exportUiState.allContacts.toMutableList()
                        } else {
                            exportUiState.selectedContacts.toMutableList()
                        }
                        exportContactsViewModel.updateSelectedContacts(selected)
                        exportContactsViewModel.showAdAndExportContacts(activity, activity)
                    }
                )
            }
            composable(MainTab.Settings.route) {
                PlaceholderTabScreen(
                    title = stringResource(id = R.string.settings_tab),
                    subtitle = stringResource(id = R.string.settings_placeholder)
                )
            }
        }
    }
}
