package com.example.patients.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.patients.data.local.Cles
import com.example.patients.data.local.Parametres
import com.example.patients.data.local.Tri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Exercices 3 et 4 : lecture (Flow) et écriture (edit) des préférences
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val parametres: Flow<Parametres> = dataStore.data
        .catch { e ->
            // Fichier illisible : préférences vides. Les autres erreurs ne sont PAS masquées.
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            Parametres(
                tri = Tri.valueOf(prefs[Cles.TRI] ?: Tri.NOM.name),
                themeSombre = prefs[Cles.THEME_SOMBRE] ?: false,   // false par défaut
                derniereSynchro = prefs[Cles.DERNIERE_SYNCHRO]     // null si absent
            )
        }

    suspend fun setTri(tri: Tri) {
        dataStore.edit { prefs -> prefs[Cles.TRI] = tri.name }
    }

    suspend fun setThemeSombre(actif: Boolean) {
        dataStore.edit { prefs -> prefs[Cles.THEME_SOMBRE] = actif }
    }

    suspend fun setDerniereSynchro(millis: Long) {
        dataStore.edit { prefs -> prefs[Cles.DERNIERE_SYNCHRO] = millis }
    }
}
