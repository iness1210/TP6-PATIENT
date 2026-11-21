@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.patients.ui.parametres

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.local.Tri

// Exercice 6 : une FilterChip par valeur de Tri et un Switch pour le thème
// Exercice 8 : « Dr <nom> — <spécialité> » en haut + champ pour modifier la spécialité
@Composable
fun ParametresScreen(
    onBack: () -> Unit = {},
    vm: ParametresViewModel = viewModel(factory = ParametresViewModel.Factory)
) {
    val p by vm.parametres.collectAsStateWithLifecycle()
    val profil by vm.profil.collectAsStateWithLifecycle()

    // Textes en cours de saisie ; réinitialisés quand la valeur enregistrée change
    var nomSaisi by remember(profil.nom) { mutableStateOf(profil.nom) }
    var specialiteSaisie by remember(profil.specialite) { mutableStateOf(profil.specialite) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Partie 3 : profil du soignant ---
            Text(
                text = "Dr ${profil.nom} — ${profil.specialite}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nomSaisi,
                onValueChange = { nomSaisi = it },
                label = { Text("Nom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = specialiteSaisie,
                onValueChange = { specialiteSaisie = it },
                label = { Text("Spécialité") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Button(
                onClick = {
                    vm.changerNom(nomSaisi.trim())
                    vm.changerSpecialite(specialiteSaisie.trim())
                },
                modifier = Modifier.padding(top = 8.dp)
            ) { Text("Enregistrer le profil") }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // --- Partie 2 : préférences ---
            Text("Trier les patients par")
            Row {
                Tri.entries.forEach { t ->
                    FilterChip(
                        selected = p.tri == t,
                        onClick = { vm.choisirTri(t) },
                        label = { Text(t.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Thème sombre", Modifier.weight(1f))
                Switch(
                    checked = p.themeSombre,
                    onCheckedChange = vm::basculerTheme
                )
            }
        }
    }
}
