package com.example.patients.ui.parametres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.local.Parametres
import com.example.patients.data.local.ProfilSoignant
import com.example.patients.data.local.Tri
import com.example.patients.data.repository.ProfilRepository
import com.example.patients.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Exercice 5 : le ViewModel ne connaît que les repositories
class ParametresViewModel(
    private val repo: UserPreferencesRepository,
    private val profilRepo: ProfilRepository
) : ViewModel() {

    val parametres: StateFlow<Parametres> = repo.parametres.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Parametres()
    )

    // Exercice 8 : profil du soignant (DataStore typé)
    val profil: StateFlow<ProfilSoignant> = profilRepo.profil.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfilSoignant()
    )

    fun choisirTri(tri: Tri) = viewModelScope.launch { repo.setTri(tri) }
    fun basculerTheme(actif: Boolean) { viewModelScope.launch { repo.setThemeSombre(actif) } }

    fun changerSpecialite(s: String) { viewModelScope.launch { profilRepo.changerSpecialite(s) } }
    fun changerNom(n: String) { viewModelScope.launch { profilRepo.changerNom(n) } }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                ParametresViewModel(
                    app.container.userPreferencesRepository,
                    app.container.profilRepository
                )
            }
        }
    }
}
