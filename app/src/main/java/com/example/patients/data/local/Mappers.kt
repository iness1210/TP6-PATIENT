package com.example.patients.data.local

import com.example.patients.data.model.Patient

// Exercice 10 : conversions Patient <-> PatientEntity
fun Patient.toEntity() = PatientEntity(
    id = id,
    nom = nom,
    prenom = prenom,
    age = age,
    telephone = telephone,
    medecin = medecin,
    statut = statut
)

fun PatientEntity.toPatient() = Patient(
    id = id,
    nom = nom,
    prenom = prenom,
    age = age,
    telephone = telephone,
    medecin = medecin,
    statut = statut
)
