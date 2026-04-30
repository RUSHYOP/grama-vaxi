package com.gramavaxi.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gramavaxi.app.ui.AnimalDetailScreen
import com.gramavaxi.app.ui.AnimalListScreen
import com.gramavaxi.app.ui.AnimalRegisterScreen
import com.gramavaxi.app.ui.CampAddScreen
import com.gramavaxi.app.ui.CampListScreen
import com.gramavaxi.app.ui.DiseaseReportScreen
import com.gramavaxi.app.ui.HomeScreen
import com.gramavaxi.app.ui.SettingsScreen
import com.gramavaxi.app.ui.theme.GramaVaxiTheme

class MainActivity : AppCompatActivity() {

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            GramaVaxiTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") { HomeScreen(nav) }
                    composable("animals") { AnimalListScreen(nav) }
                    composable("animals/new") { AnimalRegisterScreen(nav) }
                    composable("animals/{id}") { entry ->
                        val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        AnimalDetailScreen(nav, id)
                    }
                    composable("camps") { CampListScreen(nav) }
                    composable("camps/new") { CampAddScreen(nav) }
                    composable("disease") { DiseaseReportScreen(nav) }
                    composable("settings") { SettingsScreen(nav) }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
