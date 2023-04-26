package com.alexxingplus.nntuandroid.ui.tasks

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

enum class Priority(rawValue: Int) {
    low(1), medium(2), high(3);

    companion object {
        fun init(rawValue: Int): Priority {
            return when (rawValue){
                1 -> low
                2 -> medium
                3 -> high
                else -> low
            }
        }
    }
}

val Priority.rawValue: Int
get(){
    return when (this){
        Priority.low -> 1
        Priority.medium -> 2
        Priority.high -> 3
    }
}

val Priority.description: String
    get(){
        return when (this){
            Priority.low -> "низкий"
            Priority.medium -> "средний"
            Priority.high -> "высокий"
        }
    }


data class Task
    (val id: Int,
     val title: String,
     val description: String?,
     val subject: String?,
     val priority: Priority?,
     val deadline: Date,
     var done: Boolean): Parcelable
{
    val JSONString: String
    get(){
        val map = HashMap<String, Any?>()
        map["id"] = id
        map["title"] = title
        map["description"] = description
        map["subject"] = subject
        val priorityInt = priority?.rawValue ?: 1
        map["priority"] = priorityInt
        val deadlineLong: Long = deadline.time / 1000
        map["deadline"] = deadlineLong
        return JSONObject(map).toString()


//        return "{\"id\":$id,\"title\":\"$title\",\"description\":${description.jsoned()},\"subject\":${subject.jsoned()},\"priority\":$priorityInt,\"deadline\":$deadlineLong}"
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        Priority.init(parcel.readInt()),
        Date(parcel.readLong()),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(subject)
        parcel.writeInt(priority?.rawValue ?: 1)
        parcel.writeLong(deadline.time)
        parcel.writeByte(if (done) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }

        fun init(obj: JSONObject): Task {
            val id = obj.getInt("id")
            val title = obj.getString("title")
            val description = obj.getString("description")
            val subject = obj.getString("subject")
            val priorityInt = obj.getInt("priority")
            val deadlineLong = obj.getLong("deadline")

            val priority = Priority.init(priorityInt)
            val deadline = Date(deadlineLong * 1000)
            return Task(id, title, description, subject, priority, deadline, false)
        }
    }
}

var intentTask: Task? = null
var intentDeleteTask: ((Int) -> Unit)? = null
var intentDoneTask: ((Boolean) -> Unit)? = null

fun String?.jsoned(): String {
    if (this == null) {return "\"\""}
    return "\"$this\""
}