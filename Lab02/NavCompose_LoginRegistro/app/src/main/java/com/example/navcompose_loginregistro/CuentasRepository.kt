package com.example.navcompose_loginregistro

import android.content.Context
import java.io.File

object CuentasRepository {

    private const val ARCHIVO = "cuentas.txt"
    private const val SEPARADOR = "|"

    /** Guarda una cuenta en cuentas.txt (MODE_APPEND). */
    fun guardarCuenta(context: Context, usuario: String, password: String) {
        val linea = "$usuario$SEPARADOR$password\n"
        context.openFileOutput(ARCHIVO, Context.MODE_APPEND).use { output ->
            output.write(linea.toByteArray())
        }
    }

    /** Lee todas las cuentas del archivo. */
    fun leerCuentas(context: Context): List<Pair<String, String>> {
        val file = File(context.filesDir, ARCHIVO)
        if (!file.exists()) return emptyList()

        return context.openFileInput(ARCHIVO).bufferedReader().useLines { lineas ->
            lineas
                .filter { it.isNotBlank() }
                .mapNotNull { linea ->
                    val partes = linea.split(SEPARADOR)
                    if (partes.size == 2) partes[0] to partes[1] else null
                }
                .toList()
        }
    }

    /** Verifica si existe coincidencia usuario/contraseña. */
    fun existeCuenta(context: Context, usuario: String, password: String): Boolean {
        return leerCuentas(context).any { it.first == usuario && it.second == password }
    }

    /** Verifica si un usuario ya está registrado. */
    fun usuarioExiste(context: Context, usuario: String): Boolean {
        return leerCuentas(context).any { it.first == usuario }
    }
}