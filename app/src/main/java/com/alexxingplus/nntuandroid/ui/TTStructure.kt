package com.alexxingplus.nntuandroid.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


class Lesson (
    var startTime : String,
    var stopTime : String,
    var day : Int,
    var weeks : ArrayList<Int>,
    var rooms : ArrayList<String>,
    var name : String,
    var type : String,
    var teacher : String,
    var notes : String
){}

var editingTT = ArrayList<Lesson>()
var index = -1
var freeLesson = emptyLesson()

var isAutoLoading = false

fun emptyLesson(): Lesson{
    return Lesson("","",0,ArrayList<Int>(), ArrayList<String>(), "", "", "", "")
}

fun setDefaults(
    key: String?,
    value: Int,
    context: Context?
) {
    val preferences =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    val editor = preferences.edit()
    editor.putInt(key, value)
    editor.commit()
}

fun getDefaults(key: String?, context: Context?): Int? {
    val preferences =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    return preferences.getInt(key, 0)
}

fun setDefaults(
    key: String?,
    value: Long,
    context: Context?
) {
    val preferences =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    val editor = preferences.edit()
    editor.putLong(key, value)
    editor.commit()
}

fun getDefaultsLong(key: String?, context: Context?): Long? {
    val preferences =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    return preferences.getLong(key, 0)
}

fun stringFromWeeks(weeks: ArrayList<Int>): String {
    var tempWeeks = ""
    for (item in weeks){
        tempWeeks += item.toString() + ","
    }
    return tempWeeks.dropLast(1)
}

fun stringFromRooms (weeks: ArrayList<String>): String {
    var tempRooms = ""
    for (item in weeks){
        tempRooms += item + ","
    }
    return tempRooms.dropLast(1)
}

fun weeksFromString(weeks: String): ArrayList<Int>{
    var input = weeks
    var output = ArrayList<Int>()
    input = input.replace(" ", "")
    while (input.contains(",")){
        try{
            val index = input.indexOf(",")
            output.add(input.prefix(index).toInt())
            input = input.suffix(index+1)
        } catch (e: Exception){
            Log.d("Гадость в парсе", e.toString())
        }
    }
    try{
        output.add(input.toInt())
    } catch (e: Exception){
        Log.d("Гадость в парсе", e.toString())
    }
    if (output == arrayListOf("")){
        output = ArrayList<Int>()
    }
    return output
}

fun cleaning(input: String): String{
    if (input == "") {return input}
    var output = input
    while (output.first() == ' '){
        output = output.drop(1)
        if (output == "") {return input}
    }
    while (output.last() == ' '){
        output = output.dropLast(1)
        if (output == "") {return input}
    }
    return output
}

fun roomsFromString(rooms: String): ArrayList<String>{
    var input = rooms
    var output = ArrayList<String>()
    while (input.contains(",")){
        val index = input.indexOf(",")
        output.add(cleaning(input.prefix(index)))
        input = input.suffix(index+1)
    }
    if (output == arrayListOf("")){
        output = ArrayList<String>()
    }
    output.add(cleaning(input))
    return output
}


fun timeFromString(string: String): Int {
    try {
        return string.replace(":","").toInt()
    } catch (e:Exception){
        Log.d("Ошибка в парсе инта", e.toString())
        return 0
    }

}

fun uploadTT(tt: ArrayList<Lesson>, groupName: String, callBack:(Boolean)-> Unit, context: Context){
    val queue = Volley.newRequestQueue(context)
    val url = "http://194.58.97.17:3000/"

    var startTimes = ArrayList<String>()
    var stopTimes = ArrayList<String>()
    var days = ArrayList<Int>()
    var weeks = ArrayList<String>()
    var rooms = ArrayList<String>()
    var names = ArrayList<String>()
    var types = ArrayList<String>()
    var teachers = ArrayList<String>()
    var notes = ArrayList<String>()

    for (item in tt){
        startTimes.add(item.startTime)
        stopTimes.add(item.stopTime)
        days.add(item.day)
        weeks.add(stringFromWeeks(item.weeks))
        rooms.add(stringFromRooms(item.rooms))
        names.add(item.name)
        types.add(item.type)
        teachers.add(item.teacher)
        notes.add(item.notes)
    }

    val params = HashMap<String, Any>()
    params.put("key", postKey)
    params.put("groupName", groupName.replace("DROP","").replace("'",""))
    params.put("startTimes", startTimes)
    params.put("stopTimes", stopTimes)
    params.put("days", days)
    params.put("weeks", weeks)
    params.put("rooms", rooms)
    params.put("names", names)
    params.put("types", types)
    params.put("teachers", teachers)
    params.put("notes", notes)

    val request = JsonObjectRequest(Request.Method.POST, url, JSONObject(params.toMap()), Response.Listener<JSONObject> { response ->
        callBack(true)
        Log.i("response", response.toString())
    }, Response.ErrorListener { error ->
        Log.d("uploadTT", error.toString())
        callBack(false)
    })
    queue.add(request)
}

fun parseString(input: JSONArray): ArrayList<String> {
    val output = ArrayList<String>()
    for (i in 0..input.length() - 1){
        output.add(input.getString(i))
    }
    return output
}

fun parseInt(input: JSONArray): ArrayList<Int> {
    val output = ArrayList<Int>()
    for (i in 0..input.length() - 1){
        output.add(input.getInt(i))
    }
    return output
}

fun downloadTT(groupName: String, context: Context, callBack: (ArrayList<Lesson>) -> Unit){
    val queue = Volley.newRequestQueue(context)
    var output = ArrayList<Lesson>()
    var params = HashMap<String, Any>()
    params.put("key", receiveKey)
    params.put("groupName", groupName.replace("DROP","").replace("'",""))

    val url = "http://194.58.97.17:3000"
    var startTimes = ArrayList<String>()
    var stopTimes = ArrayList<String>()
    var days = ArrayList<Int>()
    var weeks = ArrayList<String>()
    var rooms = ArrayList<String>()
    var names = ArrayList<String>()
    var types = ArrayList<String>()
    var teachers = ArrayList<String>()
    var notes = ArrayList<String>()

    val request = JsonObjectRequest(Request.Method.POST, url, JSONObject(params.toMap()), { response ->
        Log.i("Response", response.toString())
        try{
            stopTimes = parseString(response.optJSONArray("stopTimes")!!)
            startTimes = parseString(response.optJSONArray("startTimes")!!)
            days = parseInt(response.optJSONArray("days")!!)
            weeks = parseString(response.optJSONArray("weeks")!!)
            rooms = parseString(response.optJSONArray("rooms")!!)
            names = parseString(response.optJSONArray("names")!!)
            types = parseString(response.optJSONArray("types")!!)
            teachers = parseString(response.optJSONArray("teachers")!!)
            notes = parseString(response.optJSONArray("notes")!!)
        } catch (e: Exception){
            Log.d("requestDown", e.toString())
        }
        for (i in 0..days.size - 1){
            var tempLesson = emptyLesson()
            tempLesson.startTime = if (startTimes.size > i) startTimes[i] else ""
            tempLesson.stopTime = if (stopTimes.size > i) stopTimes[i] else ""
            tempLesson.day = if (days.size > i) days[i] else 0
            tempLesson.weeks = if (weeks.size > i) weeksFromString(weeks[i]) else ArrayList<Int>()
            tempLesson.rooms = if (rooms.size > i) roomsFromString(rooms[i]) else ArrayList<String>()
            tempLesson.name = if (names.size > i) names[i] else ""
            tempLesson.type = if (types.size > i) types[i] else ""
            tempLesson.teacher = if (teachers.size > i) teachers[i] else ""
            tempLesson.notes = if (notes.size > i) notes[i] else ""
            output.add(tempLesson)
        }
        Log.i("array", output.toString())
        callBack(output)
    }, { error ->
        Log.d("downloadTT", error.toString())
        callBack(output)
    })
    queue.add(request)
}