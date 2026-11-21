package com.example.patients.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.patients.ui.consultations.ConsultationsScreen
import com.example.patients.ui.parametres.ParametresScreen
import com.example.patients.ui.patients.PatientsScreen

// Navigation minimale entre les deux écrans, sans bibliothèque supplémentaire :
// si un patient est sélectionné on affiche ses consultations, sinon la liste.
@Composable
fun PatientsApp() {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedName by rememberSaveable { mutableStateOf("") }
    // TP 6 : écran Paramètres (tri, thème, profil du soignant)
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (showSettings) {
        BackHandler { showSettings = false }
        ParametresScreen(onBack = { showSettings = false })
        return
    }

    val patientId = selectedId
    if (patientId == null) {
        PatientsScreen(
            onOpenSettings = { showSettings = true },
            onShowConsultations = { patient ->
                selectedName = "${patient.prenom} ${patient.nom}"
                selectedId = patient.id
            }
        )
    } else {
        // Le bouton Retour du système ramène à la liste
        BackHandler { selectedId = null }
        ConsultationsScreen(
            patientId = patientId,
            patientName = selectedName,
            onBack = { selectedId = null }
        )
    }
}
