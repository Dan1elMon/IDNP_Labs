package com.example.navcompose_loginregistro

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegistroScreen(
    context: Context,
    onRegistroExitoso: (String) -> Unit,
    onCancelar: () -> Unit,
    onUsuarioYaExiste: (String) -> Boolean
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; mensajeError = "" },
            label = { Text("Nuevo usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; mensajeError = "" },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when {
                    usuario.isBlank() || password.isBlank() -> {
                        mensajeError = "Complete todos los campos"
                    }
                    onUsuarioYaExiste(usuario) -> {
                        mensajeError = "El usuario ya existe"
                    }
                    else -> {
                        CuentasRepository.guardarCuenta(context, usuario, password)
                        onRegistroExitoso(usuario)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aceptar")
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}