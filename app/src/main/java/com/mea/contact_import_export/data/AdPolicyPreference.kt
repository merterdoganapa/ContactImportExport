package com.mea.contact_import_export.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AdPolicyPreference {
    private const val DATASTORE_NAME = "ad_policy_prefs"
    private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
    private val KEY_DONT_SHOW_AGAIN = booleanPreferencesKey("dont_show_ad_policy_again")

    fun dontShowAgainFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DONT_SHOW_AGAIN] ?: false
        }

    suspend fun setDontShowAgain(context: Context, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DONT_SHOW_AGAIN] = value
        }
    }
} 