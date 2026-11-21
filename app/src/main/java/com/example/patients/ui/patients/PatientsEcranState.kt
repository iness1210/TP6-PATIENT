package com.example.patients.ui.patients

import com.example.patients.data.model.Patient

// Ce que l'écran Patients affiche (5.2)
data class PatientsEcranState(
    val patients: List<Patient> = emptyList(),
    val horsLigne: Boolean = false,
    val derniereSynchro: Long? = null
)
