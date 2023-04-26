package com.alexxingplus.nntuandroid.ui.tasks

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.alexxingplus.nntuandroid.ui.DBHelper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class TaskStorage(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DBHelper.DATABASE_NAME, factory, DBHelper.DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "myDBfile.db"
        const val TABLE_NAME = "TaskStorage"

        const val idKey = "id"
        const val titleKey = "title"
        const val descriptionKey = "description"
        const val subjectKey = "subject"
        const val priorityKey = "priority"
        const val deadlineKey = "deadline"
        const val doneKey = "done"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db == null) { return }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME" +
                    "($idKey INTEGER PRIMARY KEY," +
                    "$titleKey TEXT, " +
                    "$descriptionKey TEXT, " +
                    "$subjectKey TEXT, " +
                    "$priorityKey INTEGER," +
                    "$deadlineKey INTEGER," +
                    "$doneKey BOOL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    private fun save(task: Task){
        val values = ContentValues()
        values.put(idKey, task.id)
        values.put(titleKey, task.title)
        values.put(descriptionKey, task.description)
        values.put(subjectKey, task.subject)
        task.priority.let {
            values.put(priorityKey, task.priority!!.rawValue)
        } ?: run {
            values.put(priorityKey, 1)
        }
        values.put(deadlineKey, task.deadline.time)
        values.put(doneKey, task.done)
        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun save(tasks: ArrayList<Task>){
        thread {
            val db = writableDatabase
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
            for (task in tasks){
                save(task)
            }
            db.close()
        }
    }

    fun load(): ArrayList<Task> {
        var output = ArrayList<Task>()
        val tempCursor = cursor
        tempCursor!!.moveToFirst()
        while (!tempCursor.isAfterLast) {
            val id = tempCursor.getInt(idKey)
            val title = tempCursor.getString(titleKey)
            val description = tempCursor.getString(descriptionKey)
            val subject = tempCursor.getString(subjectKey)
            val priorityInt = tempCursor.getInt(priorityKey)
            val deadlineLong = tempCursor.getLong(deadlineKey)
            val done = tempCursor.getBool(doneKey)
            val priority = Priority.init(priorityInt)
            val deadline = Date(deadlineLong)
            val task = Task(id, title, description, subject, priority, deadline, done)
            output.add(task)
            tempCursor.moveToNext()
        }
        tempCursor.close()
        readableDatabase.close()
        return output
    }

    fun clear() {
        try {
            val db = writableDatabase
            db.delete(TABLE_NAME, null, null)
        } catch (e: Exception) {
            Log.d("clear SQL error", e.toString())
        }
    }

    private val cursor: Cursor?
    get(){
        val db = readableDatabase
        try {
            return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        } catch (e: Exception){
            Log.d("cursor isn't reachable", e.toString())
            onCreate(writableDatabase)
            return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        }
    }
}

fun Cursor.getString(key: String): String{
    val index = this.getColumnIndex(key)
    return this.getString(index)
}

fun Cursor.getInt(key: String): Int{
    val index = this.getColumnIndex(key)
    return this.getInt(index)
}

fun Cursor.getLong(key: String): Long{
    val index = this.getColumnIndex(key)
    return this.getLong(index)
}

fun Cursor.getBool(key: String): Boolean {
    val index = this.getColumnIndex(key)
    return this.getInt(index) > 0
}


