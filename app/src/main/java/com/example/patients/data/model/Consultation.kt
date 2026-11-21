package com.example.patients.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

// Exercice 3 : consultation telle que renvoyée par GET /api/consultations?patient=<id>
// Supporte à la fois "id" et "_id" (format MongoDB / Mongoose).
@Serializable(with = ConsultationSerializer::class)
data class Consultation(
    val id: String = "",
    val patient: JsonElement? = null,
    val date: String? = null,
    val motif: String? = null,
    val diagnostic: String? = null,
    val notes: String? = null
)

@Serializable
private data class ConsultationSurrogate(
    val id: String = "",
    @SerialName("_id") val mongoId: String = "",
    val patient: JsonElement? = null,
    val date: String? = null,
    val motif: String? = null,
    val diagnostic: String? = null,
    val notes: String? = null
)

object ConsultationSerializer : KSerializer<Consultation> {
    private val surrogateSerializer = ConsultationSurrogate.serializer()
    override val descriptor = surrogateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Consultation) {
        val surrogate = ConsultationSurrogate(
            id = value.id,
            mongoId = value.id,
            patient = value.patient,
            date = value.date,
            motif = value.motif,
            diagnostic = value.diagnostic,
            notes = value.notes
        )
        encoder.encodeSerializableValue(surrogateSerializer, surrogate)
    }

    override fun deserialize(decoder: Decoder): Consultation {
        val surrogate = decoder.decodeSerializableValue(surrogateSerializer)
        val finalId = surrogate.id.ifBlank { surrogate.mongoId }
        return Consultation(
            id = finalId,
            patient = surrogate.patient,
            date = surrogate.date,
            motif = surrogate.motif,
            diagnostic = surrogate.diagnostic,
            notes = surrogate.notes
        )
    }
}

