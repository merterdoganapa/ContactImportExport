package com.mea.contact_import_export

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mea.contact_import_export.ui.screens.home.AppContent
import com.mea.contact_import_export.ui.theme.ContactImportExportTheme
import dagger.hilt.android.AndroidEntryPoint

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
