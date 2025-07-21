package com.mea.contact_import_export.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R

@Composable
fun TopAppBarMenu(
    onSyncClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isSyncEnabled: Boolean = true,
    isExportEnabled: Boolean = true,
    isImportEnabled: Boolean = true,
    dontShowAdPolicyDialog: Boolean,
    onToggleAdPolicyDialog: () -> Unit
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
            // Settings item for ad policy dialog toggle
            DropdownMenuItem(
                onClick = {
                    onToggleAdPolicyDialog()
                },
                text = {
                    Row {
//                        if (dontShowAdPolicyDialog) {
//                            Icon(Icons.Filled.Check, contentDescription = null)
//                        } else {
//                            Icon(Icons.Outlined.Warning, contentDescription = null)
//                        }
                        Checkbox(
                            checked = dontShowAdPolicyDialog,
                            onCheckedChange = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.ad_policy_menu_toggle),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            )
        }
    }
}