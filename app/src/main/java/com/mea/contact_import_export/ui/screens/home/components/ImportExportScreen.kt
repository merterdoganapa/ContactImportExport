package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mea.contact_import_export.ui.screens.home.ImportExportTab
import com.mea.contact_import_export.ui.theme.PrimaryColor

@Composable
fun ImportExportScreen(
    modifier: Modifier = Modifier,
    selectedTab: ImportExportTab,
    onSelectTab: (ImportExportTab) -> Unit,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onImportPhone: () -> Unit,
    isImportingPhone: Boolean = false,
    onImportVcf: () -> Unit,
    onExportToVcf: () -> Unit
) {
    var selectedFormat by rememberSaveable { mutableStateOf("vcf") }
    var hasPhoneNumberOnly by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_label)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                Text(
                    text = if (selectedTab == ImportExportTab.Import) {
                        stringResource(id = R.string.import_contacts_header)
                    } else {
                        stringResource(id = R.string.export_contacts_header)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.more)
                    )
                }
            }
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
                ImportExportSegmentedTabs(
                    selectedTab = selectedTab,
                    onSelectTab = onSelectTab
                )
            }

            if (selectedTab == ImportExportTab.Import) {
                if (isImportingPhone) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = stringResource(id = R.string.phone_import_loading),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                item { SectionTitle(text = stringResource(id = R.string.import_sources_title)) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SourceCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.phone_contacts),
                            subtitle = stringResource(id = R.string.local_storage),
                            badge = null,
                            onClick = onImportPhone,
                            enabled = !isImportingPhone
                        )
                        SourceCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.vcf_file),
                            subtitle = stringResource(id = R.string.import_vcf),
                            badge = null,
                            onClick = onImportVcf,
                            enabled = !isImportingPhone
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(text = stringResource(id = R.string.recent_imports))
                        Text(
                            text = stringResource(id = R.string.view_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryColor
                        )
                    }
                }
                item {
                    RecentImportRow(
                        title = stringResource(id = R.string.phone_contacts),
                        subtitle = stringResource(id = R.string.recent_phone_import)
                    )
                }
                item {
                    RecentImportRow(
                        title = stringResource(id = R.string.vcf_file),
                        subtitle = stringResource(id = R.string.recent_vcf_import)
                    )
                }
            } else {
                item { SectionTitle(text = stringResource(id = R.string.export_format_title)) }
                item {
                    ExportFormatRow(
                        title = stringResource(id = R.string.export_to_vcf),
                        subtitle = stringResource(id = R.string.standard_contact_format),
                        isSelected = selectedFormat == "vcf",
                        badge = null,
                        onClick = {
                            selectedFormat = "vcf"
                            onExportToVcf()
                        }
                    )
                }
                item {
                    ExportFormatRow(
                        title = stringResource(id = R.string.export_to_csv),
                        subtitle = stringResource(id = R.string.spreadsheet_compatible),
                        isSelected = selectedFormat == "csv",
                        badge = "PRO",
                        onClick = { selectedFormat = "csv" }
                    )
                }
                item {
                    ExportFormatRow(
                        title = stringResource(id = R.string.cloud_sync),
                        subtitle = stringResource(id = R.string.sync_with_cloud),
                        isSelected = selectedFormat == "cloud",
                        badge = "PRO",
                        onClick = { selectedFormat = "cloud" }
                    )
                }
                item { SectionTitle(text = stringResource(id = R.string.advanced_filters)) }
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
                        FilterRow(
                            title = stringResource(id = R.string.contact_group),
                            value = stringResource(id = R.string.all_contacts_count)
                        )
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
}

@Composable
private fun ImportExportSegmentedTabs(
    selectedTab: ImportExportTab,
    onSelectTab: (ImportExportTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8EBF0))
            .padding(4.dp)
    ) {
        Row {
            SegmentTabButton(
                title = stringResource(id = R.string.import_title),
                isSelected = selectedTab == ImportExportTab.Import,
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(ImportExportTab.Import) }
            )
            SegmentTabButton(
                title = stringResource(id = R.string.export_title),
                isSelected = selectedTab == ImportExportTab.Export,
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(ImportExportTab.Export) }
            )
        }
    }
}

@Composable
private fun SegmentTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) PrimaryColor else Color(0xFF667085),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SourceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .height(108.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (badge != null) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE7F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.take(1).uppercase(),
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA67000),
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFFFEDC2))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A8593)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF6B7280),
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun RecentImportRow(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F2F7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A8593)
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ExportFormatRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE7F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA67000),
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color(0xFFFFEDC2))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A8593)
                )
            }
            RadioButton(selected = isSelected, onClick = onClick)
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
