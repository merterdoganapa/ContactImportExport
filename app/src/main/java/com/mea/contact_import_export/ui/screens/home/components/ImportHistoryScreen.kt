package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R
import com.mea.contact_import_export.data.ImportHistoryPreference

@Composable
fun ImportHistoryScreen(
    modifier: Modifier = Modifier,
    history: List<ImportHistoryPreference.ImportHistoryEntry>,
    onBack: () -> Unit,
    onClearAll: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            ScreenTopBar(
                title = stringResource(id = R.string.recent_imports),
                showBackButton = true,
                onBack = onBack,
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFEE2E2))
                            .clickable(onClick = onClearAll)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.clear_all),
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (history.isEmpty()) {
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
                history.forEach { entry ->
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
