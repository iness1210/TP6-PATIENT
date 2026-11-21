package com.example.patients.data.repository

import com.example.patients.data.local.PatientDao
import com.example.patients.data.local.toEntity
import com.example.patients.data.local.toPatient
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.remote.PatientApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import retrofit2.HttpException

// Exercice 12 : Room est la source unique de vérité.
// L'écran lit toujours Room ; l'API ne sert qu'à mettre Room à jour.
class OfflinePatientRepository(
    private val api: PatientApi,
    private val dao: PatientDao,
    private val prefs: UserPreferencesRepository,
    json: Json
) : BaseRepository(json), PatientRepository {

    override fun observePatients(): Flow<List<Patient>> =
        dao.observerTous().map { liste -> liste.map { it.toPatient() } }

    // Lève IOException (pas de réseau) ou HttpException (4xx/5xx) : c'est le ViewModel qui les gère
    override suspend fun refresh() {
        val distants = api.getPatients()
        // 1) remplacer le contenu de Room (un patient sans id ne peut pas être une clé primaire : ignoré)
        dao.remplacerTout(distants.filter { it.id.isNotBlank() }.map { it.toEntity() })
        // 2) enregistrer la date de synchro
        prefs.setDerniereSynchro(System.currentTimeMillis())
    }

    // Après un succès sur l'API, on met Room à jour : l'écran se rafraîchit tout seul via le Flow
    override suspend fun createPatient(request: PatientRequest): Result<Patient> =
        safeCall { api.createPatient(request) }
            .onSuccess { dao.upsertTous(listOf(it.toEntity())) }

    override suspend fun updatePatient(id: String, request: PatientRequest): Result<Patient> =
        safeCall { api.updatePatient(id, request) }
            .onSuccess { dao.upsertTous(listOf(it.toEntity())) }

    override suspend fun deletePatient(id: String): Result<Unit> =
        safeCall {
            val response = api.deletePatient(id)
            if (!response.isSuccessful) throw HttpException(response)
        }.onSuccess { dao.supprimer(id) }
}
