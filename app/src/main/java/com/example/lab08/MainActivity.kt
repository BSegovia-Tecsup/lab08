package com.example.lab08


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)
            }
        }
    }

}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchTasks(it)
            },
            label = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text(if (isEditing) "Editar tarea" else "Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    if (isEditing && taskToEdit != null) {
                        viewModel.editTask(taskToEdit!!.copy(description = newTaskDescription))
                        isEditing = false
                        taskToEdit = null
                    } else {
                        viewModel.addTask(newTaskDescription)
                    }
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (isEditing) "Actualizar tarea" else "Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones para filtrar tareas
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { viewModel.getAllTasks() }) {
                Text("Todas")
            }
            Button(onClick = { viewModel.getCompletedTasks() }) {
                Text("Completadas")
            }
            Button(onClick = { viewModel.getPendingTasks() }) {
                Text("Pendientes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones para ordenar tareas
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { viewModel.sortTasksByName() }) {
                Text("Ordenar por nombre")
            }
            Button(onClick = { viewModel.sortTasksByDate() }) {
                Text("Ordenar por fecha")
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar tareas según el filtro aplicado
        tasks.forEach { task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = task.description)
                Row {
                    Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                        Text(if (task.isCompleted) "Completada" else "Pendiente")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        newTaskDescription = task.description
                        taskToEdit = task
                        isEditing = true
                    }) {
                        Text("Editar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        viewModel.deleteTask(task)
                    }) {
                        Text("Eliminar")
                    }
                }
            }
        }

        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskScreen() {
    // Simular un ViewModel
    val viewModel = TaskViewModel(FakeTaskDao())

    // Llama a tu TaskScreen con el ViewModel simulado
    TaskScreen(viewModel = viewModel)
}

// Fake DAO para la previsualización
class FakeTaskDao : TaskDao {
    private val tasks = mutableListOf(
        Task(id = 1, description = "Tarea 1", isCompleted = false),
        Task(id = 2, description = "Tarea 2", isCompleted = true),
        Task(id = 3, description = "Tarea 3", isCompleted = false)
    )

    override suspend fun getAllTasks(): List<Task> = tasks

    override suspend fun getTasksByCompletionStatus(isCompleted: Boolean): List<Task> =
        tasks.filter { it.isCompleted == isCompleted }

    override suspend fun insertTask(task: Task) {
        tasks.add(task)
    }

    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
        }
    }

    override suspend fun deleteTaskById(taskId: Int) {
        tasks.removeIf { it.id == taskId }
    }

    override suspend fun deleteAllTasks() {
        tasks.clear()
    }

    override suspend fun updateTaskDescription(taskId: Int, newDescription: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(description = newDescription)
        }
    }

    override suspend fun searchTasks(query: String): List<Task> =
        tasks.filter { it.description.contains(query, ignoreCase = true) }
}




