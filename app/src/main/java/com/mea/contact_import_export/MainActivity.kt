package com.mea.contact_import_export

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mea.contact_import_export.ui.screens.exportcontacts.ExportContactsScreen
import com.mea.contact_import_export.ui.screens.exportcontacts.ExportContactsScreenViewModel
import com.mea.contact_import_export.ui.screens.importcontacts.ImportContactsScreen
import com.mea.contact_import_export.ui.screens.importcontacts.ImportContactsScreenViewModel
import com.mea.contact_import_export.ui.theme.ContactImportExportTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactImportExportTheme {
                AppContent()
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    importContactsViewModel: ImportContactsScreenViewModel = hiltViewModel(),
    exportContactsViewModel: ExportContactsScreenViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(0, 0F, { 2 })
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    TopAppBarMenu(
                        onSyncClick = {
                            exportContactsViewModel.loadContacts()
                        },
                        onExportClick = {
                            activity?.let { exportContactsViewModel.showAdAndExportContacts(it,context) }
                        },
                        onImportClick = {
                            importContactsViewModel.selectVcfFile()
                        },
                        isSyncEnabled = pagerState.currentPage == 1,
                        isExportEnabled = pagerState.currentPage == 1,
                        isImportEnabled = pagerState.currentPage == 0
                    )
                }
            )
        }
    ) { paddingValues ->

        val tabs = listOf(stringResource(id = R.string.import_title), stringResource(id = R.string.export_title))
        val coroutineScope = rememberCoroutineScope()
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ImportContactsScreen()
                    1 -> ExportContactsScreen()
                }
            }
        }
    }
}

@Composable
fun TopAppBarMenu(
    onSyncClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isSyncEnabled: Boolean = true,
    isExportEnabled: Boolean = true,
    isImportEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isSyncEnabled) {
                DropdownMenuItem(
                    onClick = onSyncClick, text = {
                        Text(stringResource(id = R.string.sync_contacts))
                    }
                )
            }
            if (isImportEnabled) {
                DropdownMenuItem(
                    onClick = onImportClick, text = {
                        Text(stringResource(id = R.string.import_contacts))
                    }
                )
            }
            if (isExportEnabled) {
                DropdownMenuItem(
                    onClick = onExportClick, text = {
                        Text(stringResource(id = R.string.export_contacts))
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = { Text(stringResource(id = R.string.search)) },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search))
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.clear_search))
                }
            }
        },
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}