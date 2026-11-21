package com.example.patients.data.repository

import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import kotlinx.coroutines.flow.Flow

// 5.1 Nouveau contrat du repository (offline first)
interface PatientRepository {
    fun observePatients(): Flow<List<Patient>>   // lecture : toujours Room
    suspend fun refresh()                          // écriture : API -> Room

    // Création, modification, suppression du Ch.4 : inchangées pour l'appelant
    suspend fun createPatient(request: PatientRequest): Result<Patient>
    suspend fun updatePatient(id: String, request: PatientRequest): Result<Patient>
    suspend fun deletePatient(id: String): Result<Unit>
}
