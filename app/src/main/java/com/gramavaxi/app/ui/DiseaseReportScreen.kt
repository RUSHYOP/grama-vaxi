package com.gramavaxi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gramavaxi.app.R
import com.gramavaxi.app.repo.Repository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SYMPTOMS = listOf(
    "Fever", "Loss of appetite", "Coughing", "Diarrhoea",
    "Mouth sores", "Lameness", "Skin lesions", "Weakness"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseReportScreen(nav: NavController) {
    val ctx = LocalContext.current
    val repo = remember { Repository.get(ctx) }
    val scope = rememberCoroutineScope()
    val animals by repo.observeAnimals().collectAsState(initial = emptyList())
    val reports by repo.observeReports().collectAsState(initial = emptyList())
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    var selectedAnimalId by remember { mutableStateOf<Long?>(null) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var notes by remember { mutableStateOf("") }
    var lastRef by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_disease)) },
                navigationIcon = { BackButton(nav) }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.report_choose_animal), style = MaterialTheme.typography.titleLarge)
            if (animals.isEmpty()) {
                Text(stringResource(R.string.empty_animals))
            } else {
                ExposedDropdown(
                    options = animals.map { it.id to it.name },
                    selectedId = selectedAnimalId,
                    label = stringResource(R.string.field_animal),
                    onSelect = { selectedAnimalId = it }
                )
            }

            Text(stringResource(R.string.report_symptoms), style = MaterialTheme.typography.titleLarge)
            FlowChips(SYMPTOMS, selectedSymptoms)

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.field_notes)) },
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )

            Button(
                enabled = selectedAnimalId != null && selectedSymptoms.isNotEmpty(),
                onClick = {
                    val aid = selectedAnimalId ?: return@Button
                    val a = animals.firstOrNull { it.id == aid } ?: return@Button
                    scope.launch {
                        val ref = repo.reportDisease(
                            animalId = aid,
                            animalName = a.name,
                            symptoms = selectedSymptoms.joinToString(", "),
                            notes = notes.trim().ifBlank { null },
                            photoPath = null
                        )
                        lastRef = ref
                        showSheet = true
                        selectedSymptoms.clear()
                        notes = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.action_send_report)) }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.report_history), style = MaterialTheme.typography.titleLarge)

            if (reports.isEmpty()) {
                Text(stringResource(R.string.empty_reports))
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth().height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reports, key = { it.id }) { r ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalHospital, null, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(r.animalName, style = MaterialTheme.typography.titleLarge)
                                    Text(r.symptoms, style = MaterialTheme.typography.bodyMedium)
                                    Text("${r.referenceId} • ${df.format(Date(r.createdAt))}",
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSheet && lastRef != null) {
            AlertDialog(
                onDismissRequest = { showSheet = false },
                confirmButton = {
                    TextButton(onClick = { showSheet = false }) { Text(stringResource(R.string.ok)) }
                },
                title = { Text(stringResource(R.string.report_sent_title)) },
                text = {
                    Text(stringResource(R.string.report_sent_body, lastRef!!))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdown(
    options: List<Pair<Long, String>>,
    selectedId: Long?,
    label: String,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val text = options.firstOrNull { it.first == selectedId }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            readOnly = true,
            value = text,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(id); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(options: List<String>, selected: MutableList<String>) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { sym ->
            val isSel = selected.contains(sym)
            FilterChip(
                selected = isSel,
                onClick = {
                    if (isSel) selected.remove(sym) else selected.add(sym)
                },
                label = { Text(sym) }
            )
        }
    }
}
