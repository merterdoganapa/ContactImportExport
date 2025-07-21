package com.mea.contact_import_export.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.mea.contact_import_export.R

@Composable
fun TopAppBarMenu(
    onSyncClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isSyncEnabled: Boolean = true,
    isExportEnabled: Boolean = true,
    isImportEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isSyncEnabled) {
                DropdownMenuItem(
                    onClick = onSyncClick, text = {
                        Text(stringResource(id = R.string.sync_contacts))
                    }
                )
            }
            if (isImportEnabled) {
                DropdownMenuItem(
                    onClick = onImportClick, text = {
                        Text(stringResource(id = R.string.import_contacts))
                    }
                )
            }
            if (isExportEnabled) {
                DropdownMenuItem(
                    onClick = onExportClick, text = {
                        Text(stringResource(id = R.string.export_contacts))
                    }
                )
            }
        }
    }
}