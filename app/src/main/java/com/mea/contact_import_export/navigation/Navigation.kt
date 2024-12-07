package com.mea.contact_import_export.navigation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun BottomNavigationBar(navController: NavHostController, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    BottomAppBar {
        NavigationBarItem(
            selected = pagerState.currentPage == 0,
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0)
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Contacts") },
            label = { Text("Contacts") }
        )

        NavigationBarItem(
            selected = pagerState.currentPage == 1,
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(1)
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Groups") },
            label = { Text("Groups") }
        )
    }
}