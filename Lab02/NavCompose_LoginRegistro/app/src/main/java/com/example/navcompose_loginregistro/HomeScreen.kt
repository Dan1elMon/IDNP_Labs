package com.example.navcompose_loginregistro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    usuario: String,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido $usuario!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}