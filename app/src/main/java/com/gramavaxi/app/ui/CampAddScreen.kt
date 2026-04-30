package com.gramavaxi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gramavaxi.app.R
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.repo.Repository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampAddScreen(nav: NavController) {
    val ctx = LocalContext.current
    val repo = remember { Repository.get(ctx) }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var vaccines by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val cal = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
    }
    var dateMs by remember { mutableStateOf(cal.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    val df = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMs = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state = state) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_add_camp)) },
                navigationIcon = { BackButton(nav) }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_camp_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text(stringResource(R.string.field_location)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vaccines, onValueChange = { vaccines = it },
                label = { Text(stringResource(R.string.field_vaccines_offered)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.field_notes)) },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.field_date), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(df.format(Date(dateMs)), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text(stringResource(R.string.action_pick_date))
                    }
                }
            }
            Button(
                onClick = {
                    if (title.isBlank() || location.isBlank()) return@Button
                    scope.launch {
                        repo.addCamp(
                            Camp(
                                title = title.trim(),
                                location = location.trim(),
                                date = dateMs,
                                vaccinesOffered = vaccines.trim().ifBlank { "-" },
                                notes = notes.trim().ifBlank { null }
                            )
                        )
                        nav.popBackStack()
                    }
                },
                enabled = title.isNotBlank() && location.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}
