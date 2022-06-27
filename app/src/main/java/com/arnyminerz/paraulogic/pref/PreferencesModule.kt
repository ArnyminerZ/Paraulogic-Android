package com.arnyminerz.paraulogic.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "Paraulogic")

object PreferencesModule {
    val ErrorReportingEnabledKey = booleanPreferencesKey(PREF_ERROR_REPORTING)

    val TriedToSignIn = booleanPreferencesKey(PREF_SHOWN_LOGIN)

    val Language = stringPreferencesKey(PREF_LANGUAGE)

    val ShownDonateDialog = booleanPreferencesKey(PREF_SHOWN_DONATE_DIALOG)

    val NumberOfLaunches = intPreferencesKey(PREF_NUMBER_OF_LAUNCHES)
}
