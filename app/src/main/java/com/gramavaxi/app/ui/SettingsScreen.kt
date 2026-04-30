package com.gramavaxi.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.gramavaxi.app.GramaVaxiApp
import com.gramavaxi.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GramaVaxiApp
    var lang by remember {
        mutableStateOf(app.prefs().getString(GramaVaxiApp.PREF_LANG, "en") ?: "en")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = { BackButton(nav) }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = lang == "en", onClick = { selectLang(app, "en"); lang = "en" })
                Text("English", style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = lang == "kn", onClick = { selectLang(app, "kn"); lang = "kn" })
                Text("ಕನ್ನಡ (Kannada)", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.settings_about_body))
        }
    }
}

private fun selectLang(app: GramaVaxiApp, code: String) {
    app.prefs().edit().putString(GramaVaxiApp.PREF_LANG, code).apply()
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
}
