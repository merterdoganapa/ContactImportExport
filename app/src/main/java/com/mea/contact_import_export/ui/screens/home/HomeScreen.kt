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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        exportUiState.hasPermission = isGranted
        if (isGranted) {
            exportContactsViewModel.loadContacts()
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
        if (hasPermission) {
            exportContactsViewModel.loadContacts()
        } else {
            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNavigationBar(
                    selectedMainTab = selectedMainTab,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
                        Toast.makeText(context, R.string.delete_coming_soon, Toast.LENGTH_SHORT).show()
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
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
