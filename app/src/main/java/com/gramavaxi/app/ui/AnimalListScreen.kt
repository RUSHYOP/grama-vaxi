package com.gramavaxi.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gramavaxi.app.R
import com.gramavaxi.app.repo.Repository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(nav: NavController) {
    val ctx = LocalContext.current
    val repo = remember { Repository.get(ctx) }
    val animals by repo.observeAnimals().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_animals)) },
                navigationIcon = { BackButton(nav) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate("animals/new") },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text(stringResource(R.string.action_register_animal)) }
            )
        }
    ) { padding ->
        if (animals.isEmpty()) {
            Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.empty_animals), style = MaterialTheme.typography.titleLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(animals, key = { it.id }) { a ->
                    ElevatedCard(
                        Modifier.fillMaxWidth().clickable { nav.navigate("animals/${a.id}") }
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (a.photoPath != null) {
                                AsyncImage(
                                    model = File(a.photoPath),
                                    contentDescription = a.name,
                                    modifier = Modifier.size(72.dp)
                                )
                            } else {
                                Icon(Icons.Filled.Pets, null, modifier = Modifier.size(72.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(a.name, style = MaterialTheme.typography.titleLarge)
                                Text("${a.species} • ${a.breed}", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    stringResource(R.string.age_months, a.ageMonths),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
