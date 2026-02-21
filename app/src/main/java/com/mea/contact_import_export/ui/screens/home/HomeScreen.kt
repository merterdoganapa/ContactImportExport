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
import com.mea.contact_import_export.data.ImportHistoryPreference
import com.mea.contact_import_export.data.PurchaseResult
import com.mea.contact_import_export.data.RestoreResult
import com.mea.contact_import_export.ui.screens.exportcontacts.ExportContactsScreenViewModel
import com.mea.contact_import_export.ui.screens.home.components.ContactsScreen
import com.mea.contact_import_export.ui.screens.home.components.HomeBottomNavigationBar
import com.mea.contact_import_export.ui.screens.home.components.ImportExportScreen
import com.mea.contact_import_export.ui.screens.home.components.SettingsScreen
import com.mea.contact_import_export.ui.screens.importcontacts.ImportContactsScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun AppContent(
    importContactsViewModel: ImportContactsScreenViewModel = hiltViewModel(),
    exportContactsViewModel: ExportContactsScreenViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val exportUiState by exportContactsViewModel.uiState.collectAsState()
    val premiumUiState by premiumViewModel.uiState.collectAsState()
    val recentImports by ImportHistoryPreference.recentImportsFlow(context).collectAsState(initial = emptyList())
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
    var defaultExportContactGroup by rememberSaveable { mutableStateOf(ExportContactGroup.All) }
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
                importContactsViewModel.showAdAndProcessVcfFile(
                    activity = activity,
                    context = activity,
                    uri = uri
                ) { importedCount ->
                    coroutineScope.launch {
                        ImportHistoryPreference.addRecentImport(
                            context = context,
                            source = ImportHistoryPreference.SOURCE_VCF,
                            contactCount = importedCount
                        )
                    }
                }
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
                ImportHistoryPreference.addRecentImport(
                    context = context,
                    source = ImportHistoryPreference.SOURCE_PHONE,
                    contactCount = exportUiState.allContacts.size
                )
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
                        if (tab == MainTab.Export) {
                            defaultExportContactGroup = ExportContactGroup.All
                        }
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
                        defaultExportContactGroup = if (exportUiState.selectedContacts.isNotEmpty()) {
                            ExportContactGroup.Selected
                        } else {
                            ExportContactGroup.All
                        }
                        if (currentRoute != MainTab.Export.route) {
                            navController.navigate(MainTab.Export.route) {
                                launchSingleTop = true
                            }
                        }
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
                    defaultExportContactGroup = defaultExportContactGroup,
                    allContactsCount = exportUiState.allContacts.size,
                    selectedContactsCount = exportUiState.selectedContacts.size,
                    onExportRequest = { _, _ -> },
                    recentImports = recentImports
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
                    defaultExportContactGroup = defaultExportContactGroup,
                    allContactsCount = exportUiState.allContacts.size,
                    selectedContactsCount = exportUiState.selectedContacts.size,
                    recentImports = recentImports,
                    onExportRequest = { group, format ->
                        if (format == "csv" && !premiumUiState.isProUser) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.csv_pro_required))
                            }
                            return@ImportExportScreen
                        }
                        if (activity == null) return@ImportExportScreen
                        val selected = when (group) {
                            ExportContactGroup.All -> exportUiState.allContacts.toMutableList()
                            ExportContactGroup.Selected -> exportUiState.selectedContacts.toMutableList()
                        }
                        if (selected.isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.no_contacts_to_export_now))
                            }
                            return@ImportExportScreen
                        }
                        exportContactsViewModel.updateSelectedContacts(selected)
                        exportContactsViewModel.showAdAndExportContacts(
                            activity = activity,
                            context = activity,
                            format = format
                        )
                    }
                )
            }
            composable(MainTab.Settings.route) {
                SettingsScreen(
                    isProUser = premiumUiState.isProUser,
                    isPurchaseLoading = premiumUiState.isPurchaseLoading,
                    isRestoreLoading = premiumUiState.isRestoreLoading,
                    onBuyProClick = {
                        if (activity != null) {
                            premiumViewModel.buyPro(activity) { result ->
                                val messageRes = when (result) {
                                    PurchaseResult.Success -> R.string.purchase_success
                                    PurchaseResult.AlreadyPro -> R.string.already_pro
                                    PurchaseResult.NotActive -> R.string.purchase_not_active
                                    is PurchaseResult.Error -> R.string.purchase_failed
                                }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(messageRes))
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.purchase_failed))
                            }
                        }
                    },
                    onRestorePurchasesClick = {
                        premiumViewModel.restorePurchases { result ->
                            val messageRes = when (result) {
                                RestoreResult.Success -> R.string.restore_success
                                RestoreResult.NotFound -> R.string.restore_not_found
                                is RestoreResult.Error -> R.string.restore_failed
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(messageRes))
                            }
                        }
                    },
                    onNavigateToTab = { tab ->
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}
