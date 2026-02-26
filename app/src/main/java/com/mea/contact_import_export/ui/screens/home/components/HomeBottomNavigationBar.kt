package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R
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
                    when (tab) {
                        MainTab.Contacts -> Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(id = tab.labelRes)
                        )

                        MainTab.Import -> Icon(
                            painter = painterResource(id = R.drawable.ic_empty_import),
                            contentDescription = stringResource(id = tab.labelRes),
                            modifier = Modifier.size(20.dp)
                        )

                        MainTab.Export -> Icon(
                            painter = painterResource(id = R.drawable.ic_empty_export),
                            contentDescription = stringResource(id = tab.labelRes)
                        )

                        MainTab.Settings -> Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = tab.labelRes)
                        )
                    }
                },
                label = { Text(text = stringResource(id = tab.labelRes)) }
            )
        }
    }
}
