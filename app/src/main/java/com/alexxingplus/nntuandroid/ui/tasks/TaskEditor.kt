package com.alexxingplus.nntuandroid.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.DBHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.net.URL
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.min

class TaskEditor: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_editor)
        val group = intent.getStringExtra("group")
        val titleField: EditText = findViewById(R.id.taskTitleField)
        val descriptionField: EditText = findViewById(R.id.taskDescriptionField)
        val suggestionView: RecyclerView = findViewById(R.id.taskSubjectSuggestions)
        var suggestions = mutableSetOf<String>()
        val subjectNames = ArrayList<String>()
        val subjectField: EditText = findViewById(R.id.taskSubjectField)
        val priorityButtons: Array<Button> = arrayOf(
            findViewById(R.id.taskLowPriority),
            findViewById(R.id.taskMediumPriority),
            findViewById(R.id.taskHighPriority)
        )
        var priority = Priority.low
        val deadlineButton: Button = findViewById(R.id.taskDeadlineButton)
        var deadline: Date? = null
        val taskDoneButton: FloatingActionButton = findViewById(R.id.taskDoneButton)
        fun updatePriorityLayout(){
            val buttonIndex = priority.rawValue - 1
            for (i in priorityButtons.indices){
                priorityButtons[i].elevation = if (i == buttonIndex) 5F else 0F
            }
        }
        fun updateDeadlineLabel(){
            if (deadline == null) { return }
            val formatter = SimpleDateFormat("dd MMMM HH:mm", Locale.getDefault())
            deadlineButton.text = formatter.format(deadline!!)
        }
        fun tempTask(): Task? {
            val title: String = titleField.text.toString() ?: return null
            var description: String? = descriptionField.text.toString()
            var subject: String? = subjectField.text.toString()
            if (title.isEmpty()) {return null}
            if (description!!.isEmpty()) {description = null}
            if (subject!!.isEmpty()) {subject = null}
            if (deadline == null) {return null}
            return Task(0, title , description, subject, priority, deadline!!, false)
        }

        for (i in priorityButtons.indices){
            priorityButtons[i].setOnClickListener {
                priority = Priority.init(i + 1)
                updatePriorityLayout()
            }
        }

        deadlineButton.setOnClickListener {
            val c = Calendar.getInstance()
            val nowYear = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                val hours = calendar.get(Calendar.HOUR)
                val nowMinute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    Calendar.getInstance(Locale.GERMANY).run{
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, monthOfYear)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR, hour)
                        set(Calendar.MINUTE, minute)
                        deadline = Date(timeInMillis)
                    }
                    updateDeadlineLabel()
                }, hours, nowMinute, false).show()
            }, nowYear, month, day).show()
        }

        fun emptyAlert(){
            var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
            dialog.setTitle("Не все данные заполнены")
            dialog.setMessage("Для задачи обязательны поля названия и срок выполнения")
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            dialog.show()
        }

        fun doneAlert(task: Task){
            var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
            dialog.setTitle("Задача появится у всех")
            dialog.setMessage("Вы уверены, что хотите её добавить?")
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Добавить", DialogInterface.OnClickListener { dialogInterface, _ ->
                val provider = TaskProvider(this)
                val groupName: String = group ?: return@OnClickListener
                provider.upload(task, groupName){ data ->
                    print(data)
                    if (data) {
                        intentTask = task
                        finish()
                    }
                }
            })
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Отмена", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            dialog.show()
        }

        taskDoneButton.setOnClickListener {
            val task = tempTask() ?: run {
                emptyAlert()
                return@setOnClickListener
            }
            doneAlert(task)
        }

        fun updateSuggestions(keyword: String){
            suggestions = mutableSetOf()
            subjectNames.forEach{
                if (it.uppercase().contains(keyword.uppercase())) {suggestions.add(it)}
            }
            val adapter = suggestionView.adapter as? SuggestionsAdapter
            adapter?.suggestions = ArrayList(suggestions)
            adapter?.notifyDataSetChanged()
        }

        fun showSuggestions(visible: Boolean){
            suggestionView.isVisible = visible
            suggestionView.isEnabled = visible
        }

        showSuggestions(false)
        suggestionView.adapter = SuggestionsAdapter(arrayListOf("хихи", "хехе", "хаха")) { selected ->
            subjectField.setText(selected)
            showSuggestions(false)
        }
        suggestionView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        subjectField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = subjectField.text.toString()
                updateSuggestions(text)
                showSuggestions(visible = !(suggestions.size == 0 || suggestions.contains(text)))
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        thread {
            val db = DBHelper(this, null)
            val tt = db.loadTTfromSQLite()
            tt.forEach {
                subjectNames.add(it.name)
            }
        }
    }

    private class SuggestionsAdapter(var suggestions: ArrayList<String>, val selectClosure: (String) -> Unit): RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val subjectName: TextView = itemView.findViewById(R.id.subjectSuggestionTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.subject_suggestion, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.subjectName.text = suggestions[position]
            holder.subjectName.setOnClickListener {
                selectClosure(suggestions[position])
            }
        }

        override fun getItemCount() = suggestions.size
    }

}