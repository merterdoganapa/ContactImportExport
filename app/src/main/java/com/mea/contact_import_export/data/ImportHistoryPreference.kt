package com.mea.contact_import_export.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ImportHistoryPreference {
    private const val DATASTORE_NAME = "import_history_prefs"
    private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
    private val KEY_RECENT_IMPORTS = stringPreferencesKey("recent_imports_json")
    private const val MAX_HISTORY_ITEMS = 10

    const val SOURCE_PHONE = "phone"
    const val SOURCE_VCF = "vcf"

    @Serializable
    data class ImportHistoryEntry(
        val source: String,
        val contactCount: Int,
        val timestampMillis: Long
    )

    fun recentImportsFlow(context: Context): Flow<List<ImportHistoryEntry>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[KEY_RECENT_IMPORTS].orEmpty()
            if (raw.isBlank()) {
                emptyList()
            } else {
                runCatching { Json.decodeFromString<List<ImportHistoryEntry>>(raw) }
                    .getOrDefault(emptyList())
            }
        }

    suspend fun addRecentImport(
        context: Context,
        source: String,
        contactCount: Int
    ) {
        if (contactCount <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_IMPORTS].orEmpty()
            val decoded = if (current.isBlank()) {
                emptyList()
            } else {
                runCatching { Json.decodeFromString<List<ImportHistoryEntry>>(current) }
                    .getOrDefault(emptyList())
            }
            val updated = listOf(
                ImportHistoryEntry(
                    source = source,
                    contactCount = contactCount,
                    timestampMillis = System.currentTimeMillis()
                )
            ) + decoded
            prefs[KEY_RECENT_IMPORTS] = Json.encodeToString(updated.take(MAX_HISTORY_ITEMS))
        }
    }
}
