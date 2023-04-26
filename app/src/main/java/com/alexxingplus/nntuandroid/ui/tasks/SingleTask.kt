package com.alexxingplus.nntuandroid.ui.tasks

import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alexxingplus.nntuandroid.R
import java.text.SimpleDateFormat
import java.util.*

val Date.description: String
get(){
    val formatter = SimpleDateFormat("EEEE, dd MMMM HH:mm", Locale.getDefault())
    return formatter.format(this).uppercase()
}

class SingleTask: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_task)
        val titleLabel: TextView = findViewById(R.id.taskTitleLabel)
        val priorityLabel: TextView = findViewById(R.id.taskPriorityLabel)
        val subjectLabel: TextView = findViewById(R.id.taskSubjectLabel)
        val descriptionLabel: TextView = findViewById(R.id.taskDescriptionLabel)
        val deadlineLabel: TextView = findViewById(R.id.taskDeadlineLabel)
        val deleteButton: Button = findViewById(R.id.taskDeleteButton)
        val doneBox: CheckBox = findViewById(R.id.doneCheckBox)


        val task = intent.getParcelableExtra<Task>("task")!!
        val id = task.id ?: 0
        titleLabel.text = task?.title
        priorityLabel.text = task.priority?.description
        subjectLabel.text = task.subject
        descriptionLabel.text = task.description
        deadlineLabel.text = task.deadline?.description
        doneBox.isChecked = task.done ?: false

        fun deleteAlert(){
            var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
            dialog.setTitle("Задача удалится у всех")
            dialog.setMessage("Вы уверены, что хотите её удалить?")
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Удалить", DialogInterface.OnClickListener { dialogInterface, _ ->
                intentDeleteTask?.let { it1 -> it1(id) }
                finish()
            })
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Отмена", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            dialog.show()
        }

        deleteButton.setOnClickListener {
            deleteAlert()
        }

        doneBox.setOnClickListener {
            task.done = doneBox.isChecked
            intentDoneTask?.let { it1 -> it1(doneBox.isChecked) }
        }
    }
}