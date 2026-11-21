@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.patients.ui.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientsScreen(
    onShowConsultations: (Patient) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val chargement by viewModel.chargement.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // Exercice 1 : id du patient en cours de modification (null = aucune modification)
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    // Affiche chaque message envoyé par le ViewModel
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients") },
                actions = {
                    IconButton(onClick = { viewModel.rafraichir() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Exercice 14 : bandeau orange quand l'API est injoignable
            if (uiState.horsLigne) {
                BandeauHorsLigne(
                    derniereSynchro = uiState.derniereSynchro,
                    onActualiser = { viewModel.rafraichir() }
                )
            }

            // Exercice 2 : filtre par statut (l'état du filtre est dans le ViewModel)
            StatusFilterRow(selected = filter, onSelect = { viewModel.setFilter(it) })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    // Room est vide et on attend la première réponse de l'API
                    uiState.patients.isEmpty() && chargement ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    // Rien en local et pas de réseau : on l'explique
                    uiState.patients.isEmpty() && uiState.horsLigne ->
                        ErrorContent(
                            message = "Aucune donnée locale. Connectez-vous pour synchroniser.",
                            onRetry = { viewModel.rafraichir() },
                            modifier = Modifier.align(Alignment.Center)
                        )

                    else ->
                        PatientList(
                            patients = uiState.patients,
                            onDelete = { viewModel.deletePatient(it) },
                            onEdit = { editingId = it.id },
                            onShowConsultations = onShowConsultations
                        )
                }
            }
        }
    }

    // Boîte de dialogue : ajout
    if (showAddDialog) {
        PatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addPatient(request)
                showAddDialog = false
            }
        )
    }

    // Boîte de dialogue : modification (exercice 1)
    val patientToEdit = uiState.patients.find { it.id == editingId }
    if (patientToEdit != null) {
        PatientDialog(
            patient = patientToEdit,
            onDismiss = { editingId = null },
            onConfirm = { request ->
                viewModel.updatePatient(patientToEdit.id, request)
                editingId = null
            }
        )
    }
}

// Exercice 14 : « Hors ligne — dernière synchro : <date> » + bouton Actualiser
@Composable
fun BandeauHorsLigne(derniereSynchro: Long?, onActualiser: () -> Unit) {
    val date = derniereSynchro
        ?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(it)) }
        ?: "jamais"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF9800))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hors ligne — dernière synchro : $date",
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onActualiser) { Text("Actualiser", color = Color.Black) }
    }
}

// Exercice 2 : trois FilterChip (Tous / Actif / Inactif)
@Composable
fun StatusFilterRow(selected: String?, onSelect: (String?) -> Unit) {
    val options = listOf<Pair<String, String?>>(
        "Tous" to null,
        "Actif" to "Actif",
        "Inactif" to "Inactif"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun PatientList(
    patients: List<Patient>,
    onDelete: (Patient) -> Unit,
    onEdit: (Patient) -> Unit,
    onShowConsultations: (Patient) -> Unit
) {
    if (patients.isEmpty()) {
        Text("Aucun patient", modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            // grand espace en bas : le bouton flottant ne cache pas le dernier patient
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(patients, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onClick = { onEdit(patient) },
                    onShowConsultations = { onShowConsultations(patient) },
                    onDelete = { onDelete(patient) }
                )
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onShowConsultations: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.prenom} ${patient.nom}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listOfNotNull(
                        patient.age?.let { "$it ans" },
                        patient.medecin,
                        patient.statut
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Exercice 3 : ouvre l'écran des consultations
            IconButton(onClick = onShowConsultations) {
                Icon(Icons.Default.DateRange, contentDescription = "Consultations")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

// Boîte de dialogue d'ajout (patient = null) ou de modification (patient fourni, exercice 1)
@Composable
fun PatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit,
    patient: Patient? = null
) {
    val isEdit = patient != null
    // La clé (id) réinitialise les champs si on change de patient
    var nom by rememberSaveable(patient?.id) { mutableStateOf(patient?.nom ?: "") }
    var prenom by rememberSaveable(patient?.id) { mutableStateOf(patient?.prenom ?: "") }
    var age by rememberSaveable(patient?.id) { mutableStateOf(patient?.age?.toString() ?: "") }
    var telephone by rememberSaveable(patient?.id) { mutableStateOf(patient?.telephone ?: "") }
    var medecin by rememberSaveable(patient?.id) { mutableStateOf(patient?.medecin ?: "") }
    var statut by rememberSaveable(patient?.id) { mutableStateOf(patient?.statut ?: "Actif") }
    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Modifier le patient" else "Nouveau patient") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = prenom, onValueChange = { prenom = it },
                    label = { Text("Prénom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = telephone, onValueChange = { telephone = it },
                    label = { Text("Téléphone") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin") }, singleLine = true
                )
                // Le statut ne se change qu'en modification
                if (isEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Actif", "Inactif").forEach { option ->
                            FilterChip(
                                selected = statut == option,
                                onClick = { statut = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = telephone.trim().ifBlank { null },
                            medecin = medecin.trim().ifBlank { null },
                            statut = if (isEdit) statut else null
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
