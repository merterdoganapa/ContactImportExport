package com.mea.contact_import_export.ui.screens.exportcontacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mea.contact_import_export.R
import com.mea.contact_import_export.ui.theme.PrimaryColor
import com.mea.contact_import_export.ui.view.ContactsList

@Composable
fun ExportContactsScreen(
    viewModel: ExportContactsScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!uiState.hasPermission && isGranted) {
            viewModel.loadContacts()
        }
        uiState.hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        uiState.hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            // Show error message
        }

        uiState.allContacts.isEmpty() -> {
            EmptyExportContactsView(
                onSyncClick = {
                    viewModel.syncContacts {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            )
        }

        else -> {
            ContactsList(
                contacts = uiState.allContacts,
                selectedContacts = uiState.selectedContacts,
                onContactSelected = { contact ->
                    viewModel.updateSelectedContacts(contact)
                },
                onDeselectAllClick = {
                    viewModel.updateSelectedContacts(mutableListOf())
                },
                onSelectAllClick = {
                    viewModel.updateSelectedContacts(uiState.allContacts.toMutableList())
                },
                onSearchTextChange = { searchText ->
                    viewModel.searchContacts(searchText)
                },
                onExportSelectedContactsClick = {
                    viewModel.exportContacts(context)
                },
                searchText = uiState.searchText
            )
        }
    }
}

@Composable
fun EmptyExportContactsView(
    onSyncClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clickable { onSyncClick() },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.sync),
                contentDescription = stringResource(id = R.string.sync_contacts),
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(color = PrimaryColor)
            )

            Text(text = stringResource(id = R.string.no_contacts_to_export))

            TextButton(onClick = onSyncClick) {
                Text(text = stringResource(id = R.string.sync))
            }
        }
    }
}