package com.example.lab06.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab06.screens.DetalleLugarScreen
import com.example.lab06.screens.EdificiosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "edificios"
    ) {
        composable("edificios") {
            EdificiosScreen(navController = navController)
        }
        composable("detalle/{nombre}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            DetalleLugarScreen(nombre = nombre)
        }
    }
}