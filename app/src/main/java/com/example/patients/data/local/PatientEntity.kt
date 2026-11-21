package com.example.patients.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Exercice 9 : une ligne de la table « patients » (copie locale de ce que renvoie l'API).
// On garde aussi téléphone, médecin et statut : l'écran les affiche et filtre sur le statut.
@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String,     // l'_id MongoDB
    val nom: String,
    val prenom: String,
    val age: Int?,
    val telephone: String?,
    val medecin: String?,
    val statut: String
)
