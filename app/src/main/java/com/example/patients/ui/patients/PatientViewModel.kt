package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.local.Tri
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.repository.PatientRepository
import com.example.patients.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PatientViewModel(
    private val repo: PatientRepository,
    prefs: UserPreferencesRepository
) : ViewModel() {

    private val horsLigne = MutableStateFlow(false)

    // Exercice 2 (TP2) : filtre choisi (null = Tous, sinon "Actif" ou "Inactif"), appliqué sur la liste locale
    private val _filter = MutableStateFlow<String?>(null)
    val filter: StateFlow<String?> = _filter.asStateFlow()

    // true pendant un appel à l'API (affiche l'indicateur tant que Room est vide)
    private val _chargement = MutableStateFlow(true)
    val chargement: StateFlow<Boolean> = _chargement.asStateFlow()

    // Messages ponctuels à afficher une seule fois (Snackbar)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    // 5.2 : combine recalcule l'état dès que l'UNE des sources change
    val uiState: StateFlow<PatientsEcranState> = combine(
        repo.observePatients(), prefs.parametres, horsLigne, _filter
    ) { patients, p, hl, statut ->
        val filtres = if (statut == null) patients else patients.filter { it.statut == statut }
        val tries = when (p.tri) {
            Tri.NOM -> filtres.sortedBy { it.nom.lowercase() }
            Tri.AGE -> filtres.sortedBy { it.age ?: Int.MAX_VALUE }
        }
        PatientsEcranState(tries, hl, p.derniereSynchro)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PatientsEcranState())

    init { rafraichir() }

    // API -> Room. Si l'appel échoue, l'écran garde les données locales et affiche « hors ligne ».
    fun rafraichir() {
        viewModelScope.launch {
            _chargement.value = true
            try {
                repo.refresh()
                horsLigne.value = false
            } catch (e: IOException) {          // pas de réseau, serveur éteint
                horsLigne.value = true
            } catch (e: HttpException) {        // le serveur a répondu 4xx / 5xx
                horsLigne.value = true
            } catch (e: SerializationException) { // JSON inattendu
                horsLigne.value = true
            } finally {
                _chargement.value = false
            }
        }
    }

    // Le filtre s'applique localement : plus besoin de rappeler l'API
    fun setFilter(statut: String?) {
        _filter.value = statut
    }

    // Les 3 fonctions ci-dessous modifient l'API puis Room : la liste se met à jour toute seule
    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repo.createPatient(request)
                .onSuccess { patient -> _messages.emit("${patient.prenom} ${patient.nom} ajouté") }
                .onFailure { e -> _messages.emit(e.message ?: "Ajout impossible") }
        }
    }

    fun updatePatient(id: String, request: PatientRequest) {
        viewModelScope.launch {
            repo.updatePatient(id, request)
                .onSuccess { patient -> _messages.emit("${patient.prenom} ${patient.nom} modifié") }
                .onFailure { e -> _messages.emit(e.message ?: "Modification impossible") }
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repo.deletePatient(patient.id)
                .onSuccess { _messages.emit("${patient.prenom} ${patient.nom} supprimé") }
                .onFailure { e -> _messages.emit(e.message ?: "Suppression impossible") }
        }
    }

    // Indique à Android comment construire ce ViewModel (il a besoin du repository et des préférences)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                PatientViewModel(
                    app.container.patientRepository,
                    app.container.userPreferencesRepository
                )
            }
        }
    }
}
