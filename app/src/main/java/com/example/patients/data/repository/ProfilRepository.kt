package com.example.patients.data.repository

import androidx.datastore.core.DataStore
import com.example.patients.data.local.ProfilSoignant
import kotlinx.coroutines.flow.Flow

// Exercice 8
class ProfilRepository(private val dataStore: DataStore<ProfilSoignant>) {

    val profil: Flow<ProfilSoignant> = dataStore.data

    suspend fun changerSpecialite(s: String) {
        dataStore.updateData { it.copy(specialite = s) }
    }

    suspend fun changerNom(n: String) {
        dataStore.updateData { it.copy(nom = n) }
    }
}
