package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

// Logique commune aux repositories : exécuter un appel réseau et traduire les erreurs.
// (Extraite pour être partagée par PatientRepository et ConsultationRepository.)
abstract class BaseRepository(private val json: Json) {

    // Exécute un appel réseau et transforme toute erreur en message lisible
    protected suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
        try {
            Result.success(call())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {          // le serveur a répondu 4xx ou 5xx
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {            // pas de réponse : serveur éteint, mauvaise IP...
            Result.failure(Exception("Serveur injoignable. Vérifiez l'adresse et le réseau."))
        } catch (e: SerializationException) { // JSON reçu différent de la classe Kotlin
            Result.failure(Exception("Réponse inattendue du serveur."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erreur inconnue"))
        }

    // Lit le corps JSON de l'erreur renvoyé par notre API Node
    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        val apiError = body?.let {
            runCatching { json.decodeFromString(ApiError.serializer(), it) }.getOrNull()
        }
        return when {
            apiError != null && apiError.erreurs.isNotEmpty() -> apiError.erreurs.joinToString("\n")
            apiError != null && apiError.message.isNotBlank() -> apiError.message
            e.code() == 403 -> "Erreur 403 (Accès refusé par le serveur)"
            !body.isNullOrBlank() && !body.startsWith("<") -> body.take(150)
            else -> "Erreur ${e.code()}"
        }
    }
}

