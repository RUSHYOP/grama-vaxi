package com.gramavaxi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gramavaxi.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.titleLarge
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f),
                    Icons.Filled.Pets,
                    stringResource(R.string.nav_animals)
                ) { nav.navigate("animals") }
                Tile(
                    Modifier.weight(1f),
                    Icons.Filled.CalendarMonth,
                    stringResource(R.string.nav_camps)
                ) { nav.navigate("camps") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    Modifier.weight(1f),
                    Icons.Filled.LocalHospital,
                    stringResource(R.string.nav_disease)
                ) { nav.navigate("disease") }
                Tile(
                    Modifier.weight(1f),
                    Icons.Filled.Settings,
                    stringResource(R.string.nav_settings)
                ) { nav.navigate("settings") }
            }
        }
    }
}

@Composable
private fun Tile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
