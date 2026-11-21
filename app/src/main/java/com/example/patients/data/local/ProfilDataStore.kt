package com.example.patients.data.local

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

// Exercice 7 : DataStore typé, un objet entier sérialisé en JSON
@Serializable
data class ProfilSoignant(
    val nom: String = "",
    val specialite: String = "",
    val numeroOrdre: String = ""
)

object ProfilSerializer : Serializer<ProfilSoignant> {
    override val defaultValue = ProfilSoignant()

    override suspend fun readFrom(input: InputStream): ProfilSoignant =
        try {
            Json.decodeFromString(input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Profil illisible", e)
        }

    override suspend fun writeTo(t: ProfilSoignant, output: OutputStream) {
        output.write(Json.encodeToString(t).encodeToByteArray())
    }
}

val Context.profilDataStore: DataStore<ProfilSoignant> by dataStore(
    fileName = "profil.json",
    serializer = ProfilSerializer
)
