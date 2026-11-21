package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.ui.PatientsApp
import com.example.patients.ui.parametres.ParametresViewModel
import com.example.patients.ui.theme.PatientsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // TP 6 : le thème sombre vient des préférences DataStore
            val parametresVm: ParametresViewModel = viewModel(factory = ParametresViewModel.Factory)
            val parametres by parametresVm.parametres.collectAsStateWithLifecycle()

            PatientsTheme(darkTheme = parametres.themeSombre) {
                PatientsApp()
            }
        }
    }
}
