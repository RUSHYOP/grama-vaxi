package com.gramavaxi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gramavaxi.app.R
import com.gramavaxi.app.repo.Repository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(nav: NavController, animalId: Long) {
    val ctx = LocalContext.current
    val repo = remember { Repository.get(ctx) }
    val scope = rememberCoroutineScope()
    val animal by repo.observeAnimal(animalId).collectAsState(initial = null)
    val vaccinations by repo.observeVaccinations(animalId).collectAsState(initial = emptyList())
    val df = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(animal?.name ?: "") },
                navigationIcon = { BackButton(nav) },
                actions = {
                    IconButton(onClick = {
                        val a = animal ?: return@IconButton
                        scope.launch {
                            repo.deleteAnimal(a)
                            nav.popBackStack()
                        }
                    }) { Icon(Icons.Filled.Delete, null) }
                }
            )
        }
    ) { padding ->
        val a = animal
        if (a == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (a.photoPath != null) {
                            AsyncImage(
                                model = File(a.photoPath),
                                contentDescription = null,
                                modifier = Modifier.size(96.dp)
                            )
                        } else {
                            Icon(Icons.Filled.Pets, null, modifier = Modifier.size(96.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(a.name, style = MaterialTheme.typography.headlineMedium)
                            Text("${a.species} • ${a.breed}", style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.age_months, a.ageMonths))
                            Text(a.gender, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.vaccination_history),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(vaccinations, key = { it.id }) { v ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (v.administeredDate != null) Icons.Filled.CheckCircle else Icons.Filled.Vaccines,
                            null,
                            tint = if (v.administeredDate != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(v.vaccineName, style = MaterialTheme.typography.titleLarge)
                            if (v.administeredDate != null) {
                                Text(stringResource(R.string.given_on, df.format(Date(v.administeredDate))))
                            } else {
                                Text(stringResource(R.string.due_on, df.format(Date(v.dueDate))))
                            }
                        }
                        if (v.administeredDate == null) {
                            TextButton(onClick = { scope.launch { repo.markVaccineAdministered(v) } }) {
                                Text(stringResource(R.string.action_mark_done))
                            }
                        }
                    }
                }
            }
        }
    }
}
