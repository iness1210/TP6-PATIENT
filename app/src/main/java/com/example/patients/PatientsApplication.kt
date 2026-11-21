package com.example.patients

import android.app.Application
import com.example.patients.di.AppContainer
import com.example.patients.di.DefaultAppContainer

class PatientsApplication : Application() {
    // Créé une seule fois, partagé par toute l'application
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
