package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R
import com.mea.contact_import_export.ui.components.PrimaryButton
import com.mea.contact_import_export.ui.screens.home.ExportContactGroup

@Composable
fun ExportScreen(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    defaultExportContactGroup: ExportContactGroup = ExportContactGroup.All,
    allContactsCount: Int = 0,
    selectedContactsCount: Int = 0,
    onExportRequest: (group: ExportContactGroup, format: String) -> Unit = { _, _ -> }
) {
    var selectedFormat by rememberSaveable { mutableStateOf("vcf") }
    var hasPhoneNumberOnly by rememberSaveable { mutableStateOf(true) }
    var selectedContactGroup by rememberSaveable(defaultExportContactGroup) {
        mutableStateOf(defaultExportContactGroup)
    }

    LaunchedEffect(selectedContactsCount) {
        if (selectedContactsCount == 0 && selectedContactGroup == ExportContactGroup.Selected) {
            selectedContactGroup = ExportContactGroup.All
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            ScreenTopBar(
                title = stringResource(id = R.string.export_contacts_header),
                showBackButton = showBackButton,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionBlock(title = stringResource(id = R.string.export_format_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SelectableOptionRow(
                            iconRes = R.drawable.ic_export_format_vcf,
                            iconBackground = Color(0xFFE4EEFF),
                            title = stringResource(id = R.string.export_to_vcf),
                            subtitle = stringResource(id = R.string.standard_contact_format),
                            isSelected = selectedFormat == "vcf",
                            badge = null,
                            onClick = { selectedFormat = "vcf" }
                        )
                        SelectableOptionRow(
                            iconRes = R.drawable.ic_export_format_csv,
                            iconBackground = Color(0xFFFFEFC8),
                            title = stringResource(id = R.string.export_to_csv),
                            subtitle = stringResource(id = R.string.spreadsheet_compatible),
                            isSelected = selectedFormat == "csv",
                            badge = "PRO",
                            onClick = { selectedFormat = "csv" }
                        )
                    }
                }
            }

            item {
                val canExport = when (selectedContactGroup) {
                    ExportContactGroup.All -> allContactsCount > 0
                    ExportContactGroup.Selected -> selectedContactsCount > 0
                }
                PrimaryButton(
                    text = stringResource(id = R.string.export_contacts_button),
                    onClick = { onExportRequest(selectedContactGroup, selectedFormat) },
                    enabled = canExport,
                )
            }

            item { SectionHeading(title = stringResource(id = R.string.advanced_filters)) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    FilterRow(
                        title = stringResource(id = R.string.date_created),
                        value = stringResource(id = R.string.all_time)
                    )
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = stringResource(id = R.string.contact_group),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ContactGroupOptionRow(
                            title = stringResource(
                                id = R.string.all_contacts_count_dynamic,
                                allContactsCount
                            ),
                            selected = selectedContactGroup == ExportContactGroup.All,
                            onClick = { selectedContactGroup = ExportContactGroup.All }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ContactGroupOptionRow(
                            title = stringResource(
                                id = R.string.selected_contacts_count_dynamic,
                                selectedContactsCount
                            ),
                            selected = selectedContactGroup == ExportContactGroup.Selected,
                            enabled = selectedContactsCount > 0,
                            onClick = { selectedContactGroup = ExportContactGroup.Selected }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.has_phone_number),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(id = R.string.only_contacts_with_digits),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7A8593)
                            )
                        }
                        Switch(
                            checked = hasPhoneNumberOnly,
                            onCheckedChange = { hasPhoneNumberOnly = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    title: String,
    value: String
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F8FC))
                .padding(horizontal = 10.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF556172)
            )
            Text(
                text = "v",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF97A3B6)
            )
        }
    }
}

@Composable
private fun ContactGroupOptionRow(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFEAF3FF) else Color(0xFFF5F8FC))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) Color(0xFF556172) else Color(0xFF97A3B6)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !enabled -> Color(0xFFF2F4F7)
                        selected -> Color(0xFF1F86FF)
                        else -> Color.White
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = when {
                        !enabled -> Color(0xFFD0D5DD)
                        selected -> Color(0xFF1F86FF)
                        else -> Color(0xFFAED3FF)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
