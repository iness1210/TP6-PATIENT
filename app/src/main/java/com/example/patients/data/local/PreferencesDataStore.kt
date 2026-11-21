package com.example.patients.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Exercice 2 : déclaré UNE SEULE FOIS, au niveau du fichier (top-level).
// Deux instances sur le même fichier feraient planter l'application.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parametres")

enum class Tri { NOM, AGE }

object Cles {
    val TRI = stringPreferencesKey("tri")
    val THEME_SOMBRE = booleanPreferencesKey("theme_sombre")      // un booléen
    val DERNIERE_SYNCHRO = longPreferencesKey("derniere_synchro") // un Long (millisecondes)
}

data class Parametres(
    val tri: Tri = Tri.NOM,
    val themeSombre: Boolean = false,
    val derniereSynchro: Long? = null
)
