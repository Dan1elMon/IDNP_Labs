package com.example.lab06.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab06.ui.theme.Lab06Theme

@Preview(showBackground = true, heightDp = 900)
@Composable
fun PreviewTarjetas() {
    Lab06Theme {
        Column {
            TarjetaLugar(
                nombre = "Edificio A - Ingeniería",
                descripcion = "Laboratorios de cómputo",
                imagen = ColorPainter(Color(0xFF90CAF9)),
                colorFondo = Color(0xFFE3F2FD)
            )
            TarjetaCompacta(
                nombre = "Edificio C - Rectorado",
                imagen = ColorPainter(Color(0xFFEF9A9A))
            )
            TarjetaConEtiqueta(
                nombre = "Edificio E - Laboratorios",
                etiqueta = "NUEVO",
                imagen = ColorPainter(Color(0xFFFFCC80))
            )
        }
    }
}