package com.example.lab06.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lab06.R
import com.example.lab06.components.TarjetaCompacta
import com.example.lab06.components.TarjetaConEtiqueta
import com.example.lab06.components.TarjetaLugar

@Composable
fun EdificiosScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Edificios del Campus",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ===== Estilo 1: TarjetaLugar =====
        TarjetaLugar(
            nombre = "Comedor Universitario",
            descripcion = "Almuerzos y desayunos para estudiantes",
            imagen = painterResource(id = R.drawable.comedor),
            colorFondo = Color(0xFFE3F2FD),
            onClick = { navController.navigate("detalle/Comedor Universitario") }
        )
        TarjetaLugar(
            nombre = "Biblioteca Central",
            descripcion = "Sala de lectura y hemeroteca",
            imagen = painterResource(id = R.drawable.biblioteca),
            colorFondo = Color(0xFFF3E5F5),
            onClick = { navController.navigate("detalle/Biblioteca Central") }
        )

        // ===== Estilo 2: TarjetaCompacta =====
        TarjetaCompacta(
            nombre = "Edificio C - Rectorado",
            imagen = painterResource(id = R.drawable.comedor),
            onClick = { navController.navigate("detalle/Edificio C - Rectorado") }
        )
        TarjetaCompacta(
            nombre = "Edificio D - Aulas Generales",
            imagen = painterResource(id = R.drawable.biblioteca),
            onClick = { navController.navigate("detalle/Edificio D - Aulas Generales") }
        )

        // ===== Reto opcional: TarjetaConEtiqueta =====
        TarjetaConEtiqueta(
            nombre = "Edificio E - Laboratorios",
            etiqueta = "NUEVO",
            imagen = painterResource(id = R.drawable.comedor),
            onClick = { navController.navigate("detalle/Edificio E - Laboratorios") }
        )
    }
}