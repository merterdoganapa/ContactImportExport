package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.mea.contact_import_export.ui.screens.home.MainTab

@Composable
fun HomeBottomNavigationBar(
    selectedMainTab: MainTab?,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        MainTab.bottomBarTabs.forEach { tab ->
            val icon = when (tab) {
                MainTab.Contacts -> Icons.Default.Person
                MainTab.Import -> Icons.Default.Add
                MainTab.Export -> Icons.Default.Share
                MainTab.Settings -> Icons.Default.Settings
            }
            NavigationBarItem(
                selected = selectedMainTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF137FEC),
                    selectedTextColor = Color(0xFF137FEC),
                    indicatorColor = Color(0xFFE3E7EC),
                    unselectedIconColor = Color(0xFF98A2B3),
                    unselectedTextColor = Color(0xFF98A2B3)
                ),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = tab.labelRes)
                    )
                },
                label = { Text(text = stringResource(id = tab.labelRes)) }
            )
        }
    }
}
