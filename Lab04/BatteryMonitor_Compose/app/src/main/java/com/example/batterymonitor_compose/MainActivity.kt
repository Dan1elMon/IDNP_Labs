package com.example.batterymonitor_compose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterymonitor_compose.ui.theme.BatteryMonitor_ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryMonitor_ComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        BatteryScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

// ============================================================
// ACCION PERSONALIZADA PARA EL BROADCAST MANUAL
// ============================================================
const val ACCION_ACTUALIZAR_BATERIA = "com.example.batterymonitor_compose.ACTUALIZAR_BATERIA"

// ============================================================
// COMPOSABLE PRINCIPAL
// ============================================================
@Composable
fun BatteryScreen(modifier: Modifier = Modifier) {
    // --- Estados ---
    var porcentaje by remember { mutableIntStateOf(0) }  // ✅ mutableIntStateOf
    var estaCargando by remember { mutableStateOf(false) }
    var estadoTexto by remember { mutableStateOf("Desconocido") }

    val context = LocalContext.current

    // ============================================================
    // RECEIVER 1: Escucha el broadcast automatico del sistema
    // ============================================================
    val receiverSistema = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val escala = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

                if (nivel != -1 && escala != -1) {
                    porcentaje = (nivel * 100) / escala
                }

                estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                estadoTexto = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descargando"
                    BatteryManager.BATTERY_STATUS_FULL -> "Completa"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "No cargando"
                    else -> "Desconocido"
                }

                Log.d("BatteryScreen", "Evento sistema -> Bateria: $porcentaje% | Estado: $estadoTexto")
            }
        }
    }

    // ============================================================
    // RECEIVER 2: Escucha el broadcast manual (PendingIntent)
    // ============================================================
    val receiverManual = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACCION_ACTUALIZAR_BATERIA) {
                    val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val nivel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
                    val status = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ?: -1

                    porcentaje = nivel
                    estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    estadoTexto = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descargando"
                        BatteryManager.BATTERY_STATUS_FULL -> "Completa"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "No cargando"
                        else -> "Desconocido"
                    }

                    Log.d("BatteryScreen", "Evento manual -> Bateria: $porcentaje% | Estado: $estadoTexto")
                }
            }
        }
    }

    // ============================================================
    // DisposableEffect 1: Registro/desregistro receiver del SISTEMA
    // ============================================================
    DisposableEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // ✅ Desde API 34, siempre usamos RECEIVER_NOT_EXPORTED (minSdk = 34)
        context.registerReceiver(
            receiverSistema,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
        Log.d("BatteryScreen", "Receiver del SISTEMA registrado")

        onDispose {
            context.unregisterReceiver(receiverSistema)
            Log.d("BatteryScreen", "Receiver del SISTEMA desregistrado")
        }
    }

    // ============================================================
    // DisposableEffect 2: Registro/desregistro receiver MANUAL
    // ============================================================
    DisposableEffect(Unit) {
        val filter = IntentFilter(ACCION_ACTUALIZAR_BATERIA)
        // ✅ Sin el if (SDK_INT >= TIRAMISU), porque minSdk = 34
        context.registerReceiver(
            receiverManual,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
        Log.d("BatteryScreen", "Receiver MANUAL registrado")

        onDispose {
            context.unregisterReceiver(receiverManual)
            Log.d("BatteryScreen", "Receiver MANUAL desregistrado")
        }
    }

    // ============================================================
    // INTERFAZ DE USUARIO
    // ============================================================
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Monitoreo de Bateria",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Bateria: $porcentaje%",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Estado: $estadoTexto",
            fontSize = 20.sp,
            color = if (estaCargando) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (estaCargando) "Cargando" else "No cargando",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val intent = Intent(ACCION_ACTUALIZAR_BATERIA).apply {
                    setPackage(context.packageName)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    pendingIntent.send()
                    Log.d("BatteryScreen", "PendingIntent enviado correctamente")
                } catch (e: PendingIntent.CanceledException) {
                    Log.e("BatteryScreen", "Error al enviar PendingIntent", e)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Actualizar manualmente", fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryScreenPreview() {
    BatteryMonitor_ComposeTheme {
        BatteryScreen()
    }
}
