package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(private val dao: TaskDao) : ViewModel() {


    // Estado para la lista de tareas
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks


    init {
        // Al inicializar, cargamos las tareas de la base de datos
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }


    // Función para añadir una nueva tarea
    fun addTask(description: String) {
        val newTask = Task(description = description)
        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks() // Recargamos la lista
        }
    }


    // Función para alternar el estado de completado de una tarea
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks() // Recargamos la lista
        }
    }


    // Función para eliminar todas las tareas
    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList() // Vaciamos la lista en el estado
        }
    }

    fun editTask(taskId: Int, newDescription: String) {
        viewModelScope.launch {
            dao.updateTaskDescription(taskId, newDescription)
            _tasks.value = dao.getAllTasks() // Actualizamos la lista de tareas
        }
    }

    fun editTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
            _tasks.value = dao.getAllTasks() // Actualizamos la lista de tareas
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTaskById(task.id)
            _tasks.value = dao.getAllTasks() // Actualizamos la lista de tareas
        }
    }

    // Función para obtener solo las tareas completadas
    fun getCompletedTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getTasksByCompletionStatus(isCompleted = true)
        }
    }

    // Función para obtener solo las tareas pendientes
    fun getPendingTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getTasksByCompletionStatus(isCompleted = false)
        }
    }

    // Función para obtener todas las tareas (resetear el filtro)
    fun getAllTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }
}

