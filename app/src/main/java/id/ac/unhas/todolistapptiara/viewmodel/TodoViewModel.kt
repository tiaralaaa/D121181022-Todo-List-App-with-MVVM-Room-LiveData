package id.ac.unhas.todolistapptiara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import id.ac.unhas.todolistapptiara.data.TodoItemRepository
import id.ac.unhas.todolistapptiara.data.database.TodoItem

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoItemRepository = TodoItemRepository(application)
    private val todoItems: LiveData<MutableList<TodoItem>> = repository.getAllTodoList()

    fun saveTodoItem(todoItem: TodoItem) {
        repository.saveTodoItem(todoItem)
    }

    fun saveTodoItems(todoItems: List<TodoItem>) {
        repository.saveTodoItems(todoItems)
    }

    fun updateTodoItem(todoItem:TodoItem) {
        repository.updateTodoItem(todoItem)
    }

    fun deleteTodoItem(todoItem: TodoItem) {
        repository.deleteTodoItem(todoItem)
    }

    fun toggleCompleteState(todoItem: TodoItem) {
        todoItem.completed = !todoItem.completed
        repository.updateTodoItem(todoItem)
    }

    fun getAllTodoItemList(): LiveData<MutableList<TodoItem>> {
        return todoItems
    }
}