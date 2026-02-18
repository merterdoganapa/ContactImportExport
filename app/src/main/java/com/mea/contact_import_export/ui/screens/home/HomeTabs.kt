package com.mea.contact_import_export.ui.screens.home

import com.mea.contact_import_export.R

enum class MainTab(
    val labelRes: Int,
    val route: String
) {
    Contacts(R.string.contacts_tab, "contacts"),
    Import(R.string.import_title, "import"),
    Export(R.string.export_title, "export"),
    Settings(R.string.settings_tab, "settings");

    companion object {
        val bottomBarTabs = listOf(Contacts, Import, Export, Settings)

        fun fromRoute(route: String?): MainTab? {
            return bottomBarTabs.firstOrNull { it.route == route }
        }
    }
}

enum class ImportExportTab {
    Import, Export
}
