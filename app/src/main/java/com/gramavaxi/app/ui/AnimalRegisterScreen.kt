package com.gramavaxi.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gramavaxi.app.R
import com.gramavaxi.app.data.entity.Animal
import com.gramavaxi.app.repo.Repository
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalRegisterScreen(nav: NavController) {
    val ctx = LocalContext.current
    val repo = remember { Repository.get(ctx) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var speciesIdx by remember { mutableStateOf(0) }
    val speciesOptions = listOf("Goat", "Sheep", "Cow")
    var breed by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var genderIdx by remember { mutableStateOf(0) }
    val genderOptions = listOf("Female", "Male")
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) { photoUri = null; photoFile = null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_register_animal)) },
                navigationIcon = { BackButton(nav) }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo
            ElevatedCard(Modifier.fillMaxWidth().height(180.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (photoUri != null) {
                        AsyncImage(model = photoFile, contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PhotoCamera, null)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.tap_to_take_photo))
                        }
                    }
                    Button(
                        onClick = {
                            val dir = File(ctx.filesDir, "animal_photos").apply { mkdirs() }
                            val f = File(dir, "animal_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                ctx, "${ctx.packageName}.fileprovider", f
                            )
                            photoFile = f
                            photoUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) { Text(stringResource(R.string.take_photo)) }
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.field_species), style = MaterialTheme.typography.titleLarge)
            SegmentedRow(speciesOptions, speciesIdx) { speciesIdx = it }

            OutlinedTextField(
                value = breed, onValueChange = { breed = it },
                label = { Text(stringResource(R.string.field_breed)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ageStr,
                onValueChange = { ageStr = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text(stringResource(R.string.field_age_months)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.field_gender), style = MaterialTheme.typography.titleLarge)
            SegmentedRow(genderOptions, genderIdx) { genderIdx = it }

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.field_notes)) },
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )

            Button(
                onClick = {
                    if (name.isBlank() || ageStr.isBlank()) return@Button
                    scope.launch {
                        repo.registerAnimal(
                            Animal(
                                name = name.trim(),
                                species = speciesOptions[speciesIdx],
                                breed = breed.trim().ifBlank { "-" },
                                ageMonths = ageStr.toIntOrNull() ?: 0,
                                gender = genderOptions[genderIdx],
                                photoPath = photoFile?.absolutePath,
                                notes = notes.trim().ifBlank { null }
                            )
                        )
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && ageStr.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

@Composable
private fun SegmentedRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { idx, label ->
            val isSelected = idx == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(idx) },
                label = { Text(label) },
                modifier = Modifier.weight(1f).height(48.dp)
            )
        }
    }
}
