package com.example.patients.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Patient tel que renvoyé par l'API (GET).
// Supporte à la fois "id" et "_id" (format MongoDB / Mongoose).
@Serializable(with = PatientSerializer::class)
data class Patient(
    val id: String = "",
    val nom: String = "",
    val prenom: String = "",
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

@Serializable
private data class PatientSurrogate(
    val id: String = "",
    @SerialName("_id") val mongoId: String = "",
    val nom: String = "",
    val prenom: String = "",
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

object PatientSerializer : KSerializer<Patient> {
    private val surrogateSerializer = PatientSurrogate.serializer()
    override val descriptor = surrogateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Patient) {
        val surrogate = PatientSurrogate(
            id = value.id,
            mongoId = value.id,
            nom = value.nom,
            prenom = value.prenom,
            age = value.age,
            telephone = value.telephone,
            medecin = value.medecin,
            statut = value.statut
        )
        encoder.encodeSerializableValue(surrogateSerializer, surrogate)
    }

    override fun deserialize(decoder: Decoder): Patient {
        val surrogate = decoder.decodeSerializableValue(surrogateSerializer)
        val finalId = surrogate.id.ifBlank { surrogate.mongoId }
        return Patient(
            id = finalId,
            nom = surrogate.nom,
            prenom = surrogate.prenom,
            age = surrogate.age,
            telephone = surrogate.telephone,
            medecin = surrogate.medecin,
            statut = surrogate.statut
        )
    }
}

// Données envoyées pour créer ou modifier un patient (POST / PUT) : pas d'id.
@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String? = null
)

// Corps des réponses d'erreur de l'API : { "message": "...", "erreurs": [...] }
@Serializable
data class ApiError(
    val message: String = "",
    val erreurs: List<String> = emptyList()
)

