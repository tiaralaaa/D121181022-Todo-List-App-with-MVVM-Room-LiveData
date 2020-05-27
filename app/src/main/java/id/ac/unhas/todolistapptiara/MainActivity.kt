package id.ac.unhas.todolistapptiara

import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.unhas.todolistapptiara.R
import id.ac.unhas.todolistapptiara.utilities.convertMillis
import id.ac.unhas.todolistapptiara.utilities.convertNumberToMonthName
import id.ac.unhas.todolistapptiara.adapters.TodoListAdapter
import id.ac.unhas.todolistapptiara.data.database.TodoItem
import id.ac.unhas.todolistapptiara.utilities.Constants
import id.ac.unhas.todolistapptiara.viewmodel.TodoViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.todo_item_display_details_dialog.*
import kotlinx.android.synthetic.main.todo_list.*

class MainActivity : AppCompatActivity(), TodoListAdapter.TodoItemClickListener {

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var searchView: SearchView
    private lateinit var todoAdapter: TodoListAdapter

    private var dialog: Dialog? = null
    private var countDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setting up the components needed for recycler view.

        rv_todo_list.layoutManager = LinearLayoutManager(this)
        todoAdapter = TodoListAdapter(this)
        rv_todo_list.adapter = todoAdapter

        // Setting up the components needed to fill the recycler view.
        // LiveData along with observer patter provided by google
        // It provides a smooth experience and good performance.
        todoViewModel = ViewModelProviders.of(this).get(TodoViewModel::class.java)
        todoViewModel.getAllTodoItemList().observe(this, Observer { it ->

            val itemsWithNoDeadline = mutableListOf<TodoItem>()
            val completedItems = mutableListOf<TodoItem>()

            for (item in it) {
                if (item.dueTime!!.toInt() == 0 && !item.completed) {
                    itemsWithNoDeadline.add(item)
                } else if (item.completed) {
                    completedItems.add(item)
                }
            }

            for (item in itemsWithNoDeadline) {
                it.remove(item)
            }

            for (item in completedItems) {
                it.remove(item)
            }

            it.sortBy { it.dueTime }

            it.addAll(itemsWithNoDeadline)
            it.addAll(completedItems)

            todoAdapter.setTodoItems(it)

            if (it.size == 0) {
                displayEmptyTaskListImage()
            }
        })

        fab_add_item.setOnClickListener {
            clearSearchView()
            val intent = Intent(this@MainActivity, AddEditTodoItemActivity::class.java)
            startActivityForResult(intent, Constants.INTENT_CREATE_TODO_ITEM)
        }
    }

    override fun onPause() {
        super.onPause()

        // checking if the dialog was open during
        // screen rotation so it does not cause memory leaks.
        // recreation of the dialogs are not handled as not much of an important data will be lost.
        // user will need to click the same item again if s/he wants to display the details.
        // same is applicable for countDialog.
        dialog?.dismiss()
        countDialog?.dismiss()
    }

    private fun createMockTodoItems(count: Int) {
        val todoList = mutableListOf<TodoItem>()

        for (index in 0..count) {
            todoList.add(
                TodoItem(
                    null,
                    "Title $index",
                    "Description $index",
                    "Tag $index",
                    0,
                    0,
                    false
                )
            )
        }

        todoViewModel.saveTodoItems(todoList)
    }

    private fun clearSearchView() {
        if (!searchView.isIconified) {
            searchView.isIconified = true
            return
        }
    }

    override fun onDeleteClicked(todoItem: TodoItem) {
        todoViewModel.deleteTodoItem(todoItem)
    }

    override fun onItemClicked(todoItem: TodoItem) {
        clearSearchView()

        // display the details of the item in a dialog.
        displayEventDetails(todoItem)

    }

    override fun onCheckClicked(todoItem: TodoItem) {
        todoViewModel.toggleCompleteState(todoItem)
    }

    private fun displayEventDetails(todoItem: TodoItem) {
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.todo_item_display_details_dialog)

        dialog!!.tv_todo_title_content.text = todoItem.title
        dialog!!.tv_todo_description_content.text = todoItem.description
        dialog!!.tv_todo_tags_content.text = todoItem.tags

        if (todoItem.dueTime!!.toInt() == 0) {
            dialog!!.tv_todo_due_content.text = getString(R.string.no_due_is_set)
        } else {
            val dateValues = convertMillis(todoItem.dueTime)
            val displayFormat: String
            if (dateValues[4] < 10) {
                displayFormat = String
                    .format(
                        getString(R.string.due_date_minute_less_than_ten),
                        convertNumberToMonthName(dateValues[1]),
                        dateValues[0],
                        dateValues[2],
                        dateValues[3],
                        dateValues[4]
                    )
            } else {
                displayFormat = String
                    .format(
                        getString(R.string.due_date_minute_greater_than_ten),
                        convertNumberToMonthName(dateValues[1]),
                        dateValues[0],
                        dateValues[2],
                        dateValues[3],
                        dateValues[4]
                    )
            }

            dialog!!.tv_todo_due_content.text = displayFormat
        }

        if (todoItem.completed) {
            dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_incomplete)
        } else {
            dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_complete)
        }
        dialog!!.button_complete_todo_item.setOnClickListener {
            if (!todoItem.completed) {
                dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_incomplete)
            } else {
                dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_complete)
            }
            onCheckClicked(todoItem)
        }

        dialog!!.show()
    }

    private fun hideEmptyTaskListImage() {
        if (iv_empty_task_list.visibility == View.VISIBLE) {
            iv_empty_task_list.visibility = View.GONE
        }
    }

    private fun displayEmptyTaskListImage() {
        if (iv_empty_task_list.visibility == View.GONE) {
            iv_empty_task_list.visibility = View.VISIBLE
        }
    }
}
