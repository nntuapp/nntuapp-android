package com.alexxingplus.nntuandroid.ui

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_STARTTIME TEXT, $COLUMN_STOPTIME TEXT, $COLUMN_DAY INTEGER, " +
                    "$COLUMN_WEEKS TEXT, $COLUMN_ROOMS TEXT, $COLUMN_NAME TEXT, $COLUMN_TYPE TEXT, $COLUMN_TEACHER TEXT, $COLUMN_NOTES TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
//        onCreate(db)
    }

    fun saveLesson(new: Lesson){
        val values = ContentValues()
        values.put(COLUMN_NAME, new.name)
        values.put(COLUMN_STARTTIME, new.startTime)
        values.put(COLUMN_STOPTIME, new.stopTime)
        values.put(COLUMN_DAY, new.day)
        values.put(COLUMN_WEEKS, stringFromWeeks(new.weeks))
        values.put(COLUMN_ROOMS, stringFromRooms(new.rooms))
        values.put(COLUMN_TYPE, new.type)
        values.put(COLUMN_TEACHER, new.teacher)
        values.put(COLUMN_NOTES, new.notes)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }


    fun saveTT(tt: ArrayList<Lesson>){
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        for (lesson in tt){
            saveLesson(lesson)
        }
    }

    fun loadTTfromSQLite(): ArrayList<Lesson>{
        var output = ArrayList<Lesson>()
        val cursor = getAllRow()
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast){
            var tempLesson = emptyLesson()
            tempLesson.startTime = cursor.getString(cursor.getColumnIndex(COLUMN_STARTTIME))
            tempLesson.stopTime = cursor.getString(cursor.getColumnIndex(COLUMN_STOPTIME))
            tempLesson.day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))
            tempLesson.weeks = weeksFromString(cursor.getString(cursor.getColumnIndex(COLUMN_WEEKS)))
            tempLesson.rooms = roomsFromString(cursor.getString(cursor.getColumnIndex(COLUMN_ROOMS)))
            tempLesson.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE))
            tempLesson.teacher = cursor.getString(cursor.getColumnIndex(COLUMN_TEACHER))
            tempLesson.notes = cursor.getString(cursor.getColumnIndex(COLUMN_NOTES))
            tempLesson.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            output.add(tempLesson)
            cursor.moveToNext()
        }
        return output
    }

    fun clear(){
        try{
            val db = this.readableDatabase
            db.delete("$TABLE_NAME", null, null)
            db.close()
        } catch (e: Exception){
            Log.d("clear SQL error", e.toString())
        }

    }

    fun getAllRow(): Cursor? {
        val db = this.readableDatabase
        try{
            return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        } catch (e: Exception){
            onCreate(this.writableDatabase)
            return db.rawQuery("SELECT * FROM ${com.alexxingplus.nntuandroid.ui.DBHelper.Companion.TABLE_NAME}", null)
            Log.d("Ошибка в getAllRow", e.toString())
        }


    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "myDBfile.db"
        const val TABLE_NAME = "DBTT"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_STARTTIME = "startTime"
        const val COLUMN_STOPTIME = "stopTime"
        const val COLUMN_DAY = "day"
        const val COLUMN_WEEKS = "weeks"
        const val COLUMN_ROOMS = "rooms"
        const val COLUMN_TYPE = "type"
        const val COLUMN_TEACHER = "teacher"
        const val COLUMN_NOTES = "notes"
    }


}