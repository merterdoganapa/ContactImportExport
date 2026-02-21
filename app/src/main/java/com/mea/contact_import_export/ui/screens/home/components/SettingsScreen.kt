package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.BuildConfig
import com.mea.contact_import_export.R
import com.mea.contact_import_export.ui.screens.home.MainTab
import com.mea.contact_import_export.ui.theme.PrimaryColor

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    isProUser: Boolean,
    isPurchaseLoading: Boolean,
    isRestoreLoading: Boolean,
    onBuyProClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
    onNavigateToTab: (MainTab) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            SettingsSectionTitle(text = stringResource(id = R.string.settings_import_export_section))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                SettingsRow(
                    icon = Icons.Default.Settings,
                    title = stringResource(id = R.string.settings_default_format),
                    subtitle = stringResource(id = R.string.settings_default_format_value),
                    onClick = null
                )
                SettingsRow(
                    icon = Icons.Default.Share,
                    title = stringResource(id = R.string.settings_export_contacts_item),
                    onClick = { onNavigateToTab(MainTab.Export) }
                )
            }
        }

        item {
            SettingsSectionTitle(text = stringResource(id = R.string.settings_tools_section))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = stringResource(id = R.string.settings_smart_duplicate_finder),
                    onClick = { onNavigateToTab(MainTab.Contacts) }
                )
                SettingsRow(
                    icon = Icons.Default.Refresh,
                    title = stringResource(id = R.string.settings_clean_up_contacts),
                    onClick = { onNavigateToTab(MainTab.Contacts) }
                )
            }
        }

        item {
            SettingsSectionTitle(text = stringResource(id = R.string.settings_premium_section))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isProUser) {
                    SettingsRow(
                        icon = Icons.Default.Check,
                        title = stringResource(id = R.string.settings_premium_active_title),
                        subtitle = stringResource(id = R.string.settings_premium_active_subtitle),
                        onClick = null
                    )
                } else {
                    SettingsRow(
                        icon = Icons.Default.Share,
                        title = stringResource(id = R.string.settings_buy_pro),
                        subtitle = if (isPurchaseLoading) {
                            stringResource(id = R.string.settings_purchase_in_progress)
                        } else {
                            stringResource(id = R.string.settings_buy_pro_subtitle)
                        },
                        onClick = if (isPurchaseLoading) null else onBuyProClick
                    )
                }
                SettingsRow(
                    icon = Icons.Default.Refresh,
                    title = stringResource(id = R.string.settings_restore_purchases),
                    subtitle = if (isRestoreLoading) {
                        stringResource(id = R.string.settings_restore_in_progress)
                    } else {
                        null
                    },
                    onClick = if (isRestoreLoading) null else onRestorePurchasesClick
                )
            }
        }

        item {
            SettingsSectionTitle(text = stringResource(id = R.string.settings_about_section))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                SettingsRow(
                    icon = Icons.Default.Settings,
                    title = stringResource(id = R.string.settings_app_version),
                    subtitle = stringResource(
                        id = R.string.settings_app_version_value,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF6B7280),
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)?
) {
    val isClickable = onClick != null
    val rowModifier = if (isClickable) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFE7F0FF)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A8593)
                )
            }
        }
        if (isClickable) {
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB0BAC8)
            )
        }
    }
}
