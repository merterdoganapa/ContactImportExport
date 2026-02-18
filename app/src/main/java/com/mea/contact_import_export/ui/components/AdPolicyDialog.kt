package com.mea.contact_import_export.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R

@Composable
fun AdPolicyDialog(
    onConfirm: (dontShowAgain: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dontShowAgain by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = stringResource(id = R.string.ad_policy_title)) },
        text = {
            Column {
                Text(text = stringResource(id = R.string.ad_policy_message))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Text(
                        text = stringResource(id = R.string.ad_policy_dont_show_again),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(dontShowAgain) }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        modifier = modifier
    )
} 