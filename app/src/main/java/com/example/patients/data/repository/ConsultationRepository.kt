package com.example.patients.data.repository

import com.example.patients.data.model.Consultation
import com.example.patients.data.remote.PatientApi
import kotlinx.serialization.json.Json

// Exercice 3
class ConsultationRepository(
    private val api: PatientApi,
    json: Json
) : BaseRepository(json) {

    suspend fun getConsultations(patientId: String): Result<List<Consultation>> =
        safeCall { api.getConsultations(patientId) }
}
