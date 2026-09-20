package com.example.navcompose_loginregistro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val context = this@MainActivity

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        // ---------- LOGIN ----------
                        composable("login") {
                            LoginScreen(
                                onLoginExitoso = { usuario ->
                                    Toast.makeText(
                                        context,
                                        "Bienvenido $usuario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("home/$usuario") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onIrARegistro = {
                                    navController.navigate("registro")
                                },
                                onValidarCredenciales = { usuario, password ->
                                    CuentasRepository.existeCuenta(context, usuario, password)
                                }
                            )
                        }

                        // ---------- REGISTRO ----------
                        composable("registro") {
                            RegistroScreen(
                                context = context,
                                onRegistroExitoso = { usuario ->
                                    Toast.makeText(
                                        context,
                                        "Cuenta creada: $usuario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                },
                                onCancelar = {
                                    navController.popBackStack()
                                },
                                onUsuarioYaExiste = { usuario ->
                                    CuentasRepository.usuarioExiste(context, usuario)
                                }
                            )
                        }

                        // ---------- HOME (con argumento) ----------
                        composable(
                            route = "home/{usuario}",
                            arguments = listOf(
                                navArgument("usuario") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val usuario = backStackEntry.arguments
                                ?.getString("usuario")
                                ?: "Usuario"

                            HomeScreen(
                                usuario = usuario,
                                onCerrarSesion = {
                                    navController.navigate("login") {
                                        popUpTo("home/{usuario}") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}