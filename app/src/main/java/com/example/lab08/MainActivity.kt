package com.example.lab08


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                viewModel.searchTasks(it) // Llama a la función de búsqueda cada vez que el texto cambia
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



