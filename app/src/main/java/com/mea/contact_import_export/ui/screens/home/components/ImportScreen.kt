package com.mea.contact_import_export.ui.screens.home.components

import androidx.annotation.DrawableRes
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R
import com.mea.contact_import_export.data.ImportHistoryPreference
import com.mea.contact_import_export.ui.components.PrimaryButton
import com.mea.contact_import_export.ui.theme.PrimaryColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ImportScreen(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onImportPhone: () -> Unit,
    isImportingPhone: Boolean = false,
    onImportVcf: () -> Unit,
    onViewAllRecentImports: () -> Unit = {},
    recentImports: List<ImportHistoryPreference.ImportHistoryEntry> = emptyList()
) {
    var selectedSource by rememberSaveable { mutableStateOf(ImportSourceOption.Phone) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            ScreenTopBar(
                title = stringResource(id = R.string.import_contacts_header),
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
            item {
                SectionBlock(title = stringResource(id = R.string.import_sources_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SelectableOptionRow(
                            iconRes = R.drawable.ic_import_source_phone,
                            iconBackground = Color(0xFFE7F0FF),
                            iconTint = PrimaryColor,
                            title = stringResource(id = R.string.phone_contacts),
                            subtitle = stringResource(id = R.string.local_storage),
                            isSelected = selectedSource == ImportSourceOption.Phone,
                            onClick = { selectedSource = ImportSourceOption.Phone },
                            enabled = !isImportingPhone
                        )
                        SelectableOptionRow(
                            iconRes = R.drawable.ic_import_source_vcf,
                            iconBackground = Color(0xFFE7F0FF),
                            iconTint = PrimaryColor,
                            title = stringResource(id = R.string.vcf_file),
                            subtitle = stringResource(id = R.string.import_vcf),
                            isSelected = selectedSource == ImportSourceOption.Vcf,
                            onClick = { selectedSource = ImportSourceOption.Vcf },
                            enabled = !isImportingPhone
                        )
                    }
                }
            }
            item {
                PrimaryButton(
                    text = stringResource(id = R.string.import_contacts),
                    enabled = !isImportingPhone,
                    onClick = {
                        when (selectedSource) {
                            ImportSourceOption.Phone -> onImportPhone()
                            ImportSourceOption.Vcf -> onImportVcf()
                        }
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeading(title = stringResource(id = R.string.recent_imports))
                    Text(
                        text = stringResource(id = R.string.view_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryColor,
                        modifier = Modifier.clickable(onClick = onViewAllRecentImports)
                    )
                }
            }
            if (recentImports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = stringResource(id = R.string.recent_imports_empty),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7A8593)
                        )
                    }
                }
            } else {
                recentImports.take(10).forEach { entry ->
                    item {
                        RecentImportRow(
                            iconRes = recentImportIconRes(entry.source),
                            title = sourceLabel(entry.source),
                            subtitle = entrySubtitle(entry)
                        )
                    }
                }
            }
        }
    }
}

private enum class ImportSourceOption {
    Phone,
    Vcf
}

@Composable
internal fun RecentImportRow(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9EDF3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF8A95A5)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF6B7280)
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
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

@DrawableRes
internal fun recentImportIconRes(source: String): Int {
    return when (source) {
        ImportHistoryPreference.SOURCE_VCF -> R.drawable.ic_import_source_vcf
        else -> R.drawable.ic_import_source_phone
    }
}

@Composable
internal fun sourceLabel(source: String): String {
    return when (source) {
        ImportHistoryPreference.SOURCE_PHONE -> stringResource(id = R.string.phone_contacts)
        ImportHistoryPreference.SOURCE_VCF -> stringResource(id = R.string.vcf_file)
        else -> source
    }
}

@Composable
internal fun entrySubtitle(
    entry: ImportHistoryPreference.ImportHistoryEntry
): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(entry.timestampMillis).atZone(zone)
    val now = Instant.now().atZone(zone)
    val timeText = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).format(dateTime)
    val dayText = when {
        dateTime.toLocalDate() == now.toLocalDate() ->
            stringResource(id = R.string.recent_time_today, timeText)

        dateTime.toLocalDate() == now.toLocalDate().minusDays(1) ->
            stringResource(id = R.string.recent_time_yesterday, timeText)

        else -> DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.getDefault()).format(dateTime)
    }
    return stringResource(id = R.string.recent_import_contacts_count, dayText, entry.contactCount)
}

