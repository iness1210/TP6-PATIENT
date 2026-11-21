package com.example.patients.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    // Pas de « suspend » : renvoie un Flow qui réémet la liste à chaque modification de la table
    @Query("SELECT * FROM patients")
    fun observerTous(): Flow<List<PatientEntity>>

    @Upsert
    suspend fun upsertTous(patients: List<PatientEntity>)

    @Query("DELETE FROM patients")
    suspend fun toutSupprimer()

    // Ajout : suppression d'un seul patient (après un DELETE réussi sur l'API)
    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun supprimer(id: String)

    @Transaction
    suspend fun remplacerTout(patients: List<PatientEntity>) {
        toutSupprimer()          // 1) tout supprimer
        upsertTous(patients)     // 2) insérer la nouvelle liste
    }
}
