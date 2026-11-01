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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.BuildConfig
import com.mea.contact_import_export.R
import com.mea.contact_import_export.ui.theme.PrimaryColor

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    isProUser: Boolean,
    isPurchaseLoading: Boolean,
    isRestoreLoading: Boolean,
    revenueCatAppUserId: String,
    onBuyProClick: () -> Unit,
    onRestorePurchasesClick: () -> Unit,
    onRevenueCatIdClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onRateUsClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            ScreenTopBar(title = stringResource(id = R.string.settings_tab))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                SettingsSectionTitle(text = stringResource(id = R.string.settings_premium_section))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumBannerCard(
                        isProUser = isProUser,
                        isPurchaseLoading = isPurchaseLoading,
                        onBuyProClick = onBuyProClick
                    )

                    TextButton(
                        onClick = onRestorePurchasesClick,
                        enabled = !isRestoreLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isRestoreLoading) {
                                stringResource(id = R.string.settings_restore_in_progress)
                            } else {
                                stringResource(id = R.string.settings_restore_purchases)
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionTitle(text = stringResource(id = R.string.settings_about_section))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AboutRow(
                        iconRes = R.drawable.ic_about_info,
                        title = stringResource(id = R.string.settings_app_version),
                        trailingText = stringResource(
                            id = R.string.settings_app_version_value_compact,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        onClick = null
                    )
                    HorizontalDivider(color = Color(0xFFE8EDF4))
                    AboutRow(
                        iconRes = R.drawable.ic_about_info,
                        title = stringResource(id = R.string.settings_account_id),
                        trailingText = revenueCatAppUserId.ifBlank { stringResource(id = R.string.settings_not_available_short) },
                        onClick = if (revenueCatAppUserId.isBlank()) null else onRevenueCatIdClick,
                        trailingCompact = true,
                        showChevron = false
                    )
                    HorizontalDivider(color = Color(0xFFE8EDF4))
                    AboutRow(
                        iconRes = R.drawable.ic_about_policy,
                        title = stringResource(id = R.string.settings_privacy_policy_terms),
                        trailingText = null,
                        onClick = onPrivacyPolicyClick
                    )
                }
            }

            item {
                SettingsSectionTitle(text = stringResource(id = R.string.settings_support_section))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AboutRow(
                        iconRes = R.drawable.ic_support_contact,
                        title = stringResource(id = R.string.settings_contact_us),
                        trailingText = null,
                        onClick = onContactUsClick
                    )
                    HorizontalDivider(color = Color(0xFFE8EDF4))
                    AboutRow(
                        iconRes = R.drawable.ic_support_rate,
                        title = stringResource(id = R.string.settings_rate_us),
                        trailingText = null,
                        onClick = onRateUsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumBannerCard(
    isProUser: Boolean,
    isPurchaseLoading: Boolean,
    onBuyProClick: () -> Unit
) {
    val gradient = if (isProUser) {
        Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFF22C55E)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF137FEC), Color(0xFF2F80ED)))
    }
    val title = if (isProUser) {
        stringResource(id = R.string.settings_premium_active_title)
    } else {
        stringResource(id = R.string.settings_upgrade_pro_title)
    }
    val subtitle = if (isProUser) {
        stringResource(id = R.string.settings_premium_active_subtitle)
    } else {
        stringResource(id = R.string.settings_upgrade_pro_subtitle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isProUser) Icons.Default.Check else Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFEAF3FF),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!isProUser) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .clickable(enabled = !isPurchaseLoading, onClick = onBuyProClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPurchaseLoading) {
                            stringResource(id = R.string.settings_purchase_in_progress)
                        } else {
                            stringResource(id = R.string.settings_upgrade_now)
                        },
                        color = Color(0xFF137FEC),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PRO",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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
private fun AboutRow(
    iconRes: Int,
    title: String,
    trailingText: String?,
    onClick: (() -> Unit)?,
    trailingCompact: Boolean = false,
    showChevron: Boolean = true
) {
    val isClickable = onClick != null
    val rowModifier = if (isClickable) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick ?: {})
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
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            color = Color(0xFF1F2937)
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = if (trailingCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleSmall,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = if (trailingCompact) Modifier.widthIn(max = 128.dp) else Modifier
            )
        }
        if (isClickable && showChevron) {
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB0BAC8)
            )
        }
    }
}
