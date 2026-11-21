package com.example.patients.di

import android.content.Context
import androidx.room.Room
import com.example.patients.data.local.AppDatabase
import com.example.patients.data.local.dataStore
import com.example.patients.data.local.profilDataStore
import com.example.patients.data.remote.PatientApi
import com.example.patients.data.repository.ConsultationRepository
import com.example.patients.data.repository.OfflinePatientRepository
import com.example.patients.data.repository.PatientRepository
import com.example.patients.data.repository.ProfilRepository
import com.example.patients.data.repository.UserPreferencesRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Téléphone réel et émulateur sur le Wi-Fi du Mac (Node.js TP1 sur le port 5001).
private const val BASE_URL = "http://192.168.1.13:5001/api/"

// Ce que le reste de l'application peut demander au conteneur
interface AppContainer {
    val patientRepository: PatientRepository
    val consultationRepository: ConsultationRepository
    val userPreferencesRepository: UserPreferencesRepository
    val profilRepository: ProfilRepository
}

// TP 6 : le conteneur reçoit le Context (nécessaire à DataStore et à Room)
class DefaultAppContainer(private val context: Context) : AppContainer {

    // Options de sérialisation assouplies pour tolérer les variations de réponses API
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    // Affiche chaque requête et réponse dans Logcat (filtre : okhttp)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile)")
                .header("Accept", "application/json")
                .header("ngrok-skip-browser-warning", "true")
                .header("bypass-tunnel-reminder", "true")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val patientApi: PatientApi = retrofit.create(PatientApi::class.java)

    // Exercice 11 : la base Room est créée UNE SEULE FOIS
    private val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "patients.db").build()
    }

    // Exercice 5 : le DataStore a besoin d'un Context
    override val userPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }

    // Exercice 8
    override val profilRepository by lazy {
        ProfilRepository(context.profilDataStore)
    }

    // Exercice 12 : une seule ligne change par rapport au Ch.4 (c'est l'intérêt de la DI)
    override val patientRepository: PatientRepository by lazy {
        OfflinePatientRepository(patientApi, database.patientDao(), userPreferencesRepository, json)
    }

    // Exercice 3 (TP2)
    override val consultationRepository by lazy { ConsultationRepository(patientApi, json) }
}
