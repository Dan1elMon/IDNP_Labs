package com.example.lab09

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.example.lab09.ui.theme.Lab09Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ==========================
// ENTIDADES
// ==========================

@Entity(tableName = "edificios")
data class EdificioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val categoria: String
)

@Entity(
    tableName = "ambientes",
    foreignKeys = [
        ForeignKey(
            entity = EdificioEntity::class,
            parentColumns = ["id"],
            childColumns = ["edificioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["edificioId"])]
)
data class AmbienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val edificioId: Int,
    val nombre: String
)

@Entity(
    tableName = "notas",
    foreignKeys = [
        ForeignKey(
            entity = AmbienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["ambienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ambienteId"])]
)
data class NotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ambienteId: Int,
    val texto: String
)

// ==========================
// RELACIONES
// ==========================

data class EdificioConAmbientes(
    @Embedded val edificio: EdificioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "edificioId"
    )
    val ambientes: List<AmbienteEntity>
)

data class AmbienteConNotas(
    @Embedded val ambiente: AmbienteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ambienteId"
    )
    val notas: List<NotaEntity>
)

// ==========================
// DAO
// ==========================

@Dao
interface EdificioDao {

    @Insert
    suspend fun insertarEdificio(edificio: EdificioEntity): Long

    @Insert
    suspend fun insertarAmbiente(ambiente: AmbienteEntity): Long

    @Insert
    suspend fun insertarNota(nota: NotaEntity): Long

    @Update
    suspend fun actualizarEdificio(edificio: EdificioEntity)

    @Update
    suspend fun actualizarAmbiente(ambiente: AmbienteEntity)

    @Update
    suspend fun actualizarNota(nota: NotaEntity)

    @Delete
    suspend fun eliminarEdificio(edificio: EdificioEntity)

    @Delete
    suspend fun eliminarAmbiente(ambiente: AmbienteEntity)

    @Delete
    suspend fun eliminarNota(nota: NotaEntity)

    @Query("SELECT * FROM edificios")
    suspend fun obtenerTodos(): List<EdificioEntity>

    @Transaction
    @Query("SELECT * FROM edificios")
    suspend fun obtenerConAmbientes(): List<EdificioConAmbientes>

    @Transaction
    @Query("SELECT * FROM ambientes")
    suspend fun obtenerTodosAmbientesConNotas(): List<AmbienteConNotas>

    @Query("SELECT * FROM ambientes WHERE edificioId = :edificioId")
    suspend fun obtenerAmbientesDeEdificio(edificioId: Int): List<AmbienteEntity>

    @Query("SELECT * FROM notas WHERE ambienteId = :ambienteId")
    suspend fun obtenerNotasDeAmbiente(ambienteId: Int): List<NotaEntity>
}

// ==========================
// BASE DE DATOS
// ==========================

@Database(
    entities = [EdificioEntity::class, AmbienteEntity::class, NotaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun edificioDao(): EdificioDao
}

fun crearBaseDeDatos(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app.db"
    ).build()
}

// ==========================
// VIEWMODEL
// ==========================

class EdificiosViewModel(private val dao: EdificioDao) : ViewModel() {

    private val _edificios = MutableStateFlow<List<EdificioConAmbientes>>(emptyList())
    val edificios: StateFlow<List<EdificioConAmbientes>> = _edificios

    private val _ambientesConNotas = MutableStateFlow<List<AmbienteConNotas>>(emptyList())
    val ambientesConNotas: StateFlow<List<AmbienteConNotas>> = _ambientesConNotas

    init {
        viewModelScope.launch {
            if (dao.obtenerTodos().isEmpty()) {
                insertarDatosIniciales()
            }
            refrescar()
        }
    }

    private suspend fun insertarDatosIniciales() {
        val idBiblioteca = dao.insertarEdificio(
            EdificioEntity(nombre = "Biblioteca Central", categoria = "Servicios")
        ).toInt()

        val idSala = dao.insertarAmbiente(
            AmbienteEntity(edificioId = idBiblioteca, nombre = "Sala de lectura")
        ).toInt()

        val idHemeroteca = dao.insertarAmbiente(
            AmbienteEntity(edificioId = idBiblioteca, nombre = "Hemeroteca")
        ).toInt()

        dao.insertarNota(NotaEntity(ambienteId = idSala, texto = "El proyector no funciona"))
        dao.insertarNota(NotaEntity(ambienteId = idSala, texto = "Se reserva los martes"))
        dao.insertarNota(NotaEntity(ambienteId = idHemeroteca, texto = "Requiere limpieza de estantes"))

        val idIngenieria = dao.insertarEdificio(
            EdificioEntity(nombre = "Facultad de Ingeniería", categoria = "Académico")
        ).toInt()

        val idLaboratorio = dao.insertarAmbiente(
            AmbienteEntity(edificioId = idIngenieria, nombre = "Laboratorio de cómputo")
        ).toInt()

        dao.insertarNota(
            NotaEntity(ambienteId = idLaboratorio, texto = "Actualizar equipos del laboratorio")
        )
    }

    private suspend fun refrescar() {
        _edificios.value = dao.obtenerConAmbientes()
        _ambientesConNotas.value = dao.obtenerTodosAmbientesConNotas()
    }

    fun actualizarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.actualizarEdificio(edificio)
            refrescar()
        }
    }

    fun actualizarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.actualizarAmbiente(ambiente)
            refrescar()
        }
    }

    fun eliminarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.eliminarEdificio(edificio)
            refrescar()
        }
    }

    fun eliminarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.eliminarAmbiente(ambiente)
            refrescar()
        }
    }
}

class EdificiosViewModelFactory(private val dao: EdificioDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EdificiosViewModel(dao) as T
    }
}

// ==========================
// ACTIVITY
// ==========================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab09Theme {
                val context = LocalContext.current
                val dao = remember { crearBaseDeDatos(context).edificioDao() }
                val vm: EdificiosViewModel = viewModel(
                    factory = EdificiosViewModelFactory(dao)
                )
                PantallaEdificios(vm)
            }
        }
    }
}

// ==========================
// INTERFAZ COMPOSE
// ==========================

@Composable
fun PantallaEdificios(viewModel: EdificiosViewModel) {
    val edificios by viewModel.edificios.collectAsState()
    val ambientesConNotas by viewModel.ambientesConNotas.collectAsState()

    var edificioEditando by remember { mutableStateOf<EdificioEntity?>(null) }
    var ambienteEditando by remember { mutableStateOf<AmbienteEntity?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Edificios del campus",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // CORRECCIÓN AQUÍ: Se agrega el prefijo "edificio_" al key
            items(edificios, key = { "edificio_${it.edificio.id}" }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.edificio.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "Categoría: ${item.edificio.categoria}")
                        Spacer(Modifier.height(8.dp))

                        item.ambientes.forEach { ambiente ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "• ${ambiente.nombre}")
                                }
                                TextButton(onClick = { ambienteEditando = ambiente }) {
                                    Text("Editar")
                                }
                                TextButton(onClick = { viewModel.eliminarAmbiente(ambiente) }) {
                                    Text("Eliminar")
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { edificioEditando = item.edificio }) {
                                Text("Editar edificio")
                            }
                            OutlinedButton(onClick = { viewModel.eliminarEdificio(item.edificio) }) {
                                Text("Eliminar edificio")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Ambientes y notas (segunda consulta relacional)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // CORRECCIÓN AQUÍ: Se agrega el prefijo "ambiente_" al key
            items(ambientesConNotas, key = { "ambiente_${it.ambiente.id}" }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ambiente: ${item.ambiente.nombre} (edificioId=${item.ambiente.edificioId})",
                            fontWeight = FontWeight.SemiBold
                        )

                        if (item.notas.isEmpty()) {
                            Text(text = "Sin notas", fontSize = 14.sp)
                        } else {
                            item.notas.forEach { nota ->
                                Text(text = " - ${nota.texto}", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    edificioEditando?.let { edificio ->
        EdificioDialog(
            edificio = edificio,
            onDismiss = { edificioEditando = null },
            onGuardar = {
                viewModel.actualizarEdificio(it)
                edificioEditando = null
            }
        )
    }

    ambienteEditando?.let { ambiente ->
        AmbienteDialog(
            ambiente = ambiente,
            onDismiss = { ambienteEditando = null },
            onGuardar = {
                viewModel.actualizarAmbiente(it)
                ambienteEditando = null
            }
        )
    }
}

@Composable
fun EdificioDialog(
    edificio: EdificioEntity,
    onDismiss: () -> Unit,
    onGuardar: (EdificioEntity) -> Unit
) {
    var nombre by remember(edificio.id) { mutableStateOf(edificio.nombre) }
    var categoria by remember(edificio.id) { mutableStateOf(edificio.categoria) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar edificio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && categoria.isNotBlank()) {
                        onGuardar(
                            edificio.copy(
                                nombre = nombre.trim(),
                                categoria = categoria.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AmbienteDialog(
    ambiente: AmbienteEntity,
    onDismiss: () -> Unit,
    onGuardar: (AmbienteEntity) -> Unit
) {
    var nombre by remember(ambiente.id) { mutableStateOf(ambiente.nombre) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ambiente") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del ambiente") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onGuardar(ambiente.copy(nombre = nombre.trim()))
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}