package com.mea.contact_import_export.ui.screens.importcontacts

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import android.widget.Toast
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
fun ImportContactsScreen(
    viewModel: ImportContactsScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        uiState.hasPermission = isGranted
    }

    val fileSelectionFailed = stringResource(id = R.string.file_selection_failed)
    val addContactsFailed = stringResource(id = R.string.add_contacts_failed)
    val addContactsSuccess = stringResource(id = R.string.add_contacts_success)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                viewModel.processVcfFile(context, it)
            }
        } else {
            Toast.makeText(context, fileSelectionFailed, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        uiState.hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.setLauncher(launcher)

        permissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
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
            EmptyContactsView(
                onImportClick = {
                    viewModel.selectVcfFile()
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
                onImportSelectedContactsClick = {
                    viewModel.addContactsToPhone(
                        context,
                        uiState.selectedContacts,
                        addContactsSuccess,
                        addContactsFailed
                    )
                },
                onSearchTextChange = { searchText ->
                    viewModel.searchContacts(searchText)
                },
                searchText = uiState.searchText
            )
        }
    }
}

@Composable
fun EmptyContactsView(
    onImportClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clickable { onImportClick() },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.import_contacts_icon),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(color = PrimaryColor)
            )

            Text(text = stringResource(id = R.string.no_contacts_to_import))

            TextButton(onClick = onImportClick) {
                Text(text = stringResource(id = R.string.import_title))
            }
        }
    }
}