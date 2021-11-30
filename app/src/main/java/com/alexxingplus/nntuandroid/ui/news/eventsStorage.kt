package com.alexxingplus.nntuandroid.ui.news
import android.app.DownloadManager
import android.content.Context
import com.beust.klaxon.Klaxon

import android.graphics.Color
import android.util.Log
import android.util.Pair
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.eventsURL
import com.alexxingplus.nntuandroid.ui.getDefaults
import com.alexxingplus.nntuandroid.ui.lastIdURL
import com.alexxingplus.nntuandroid.ui.setDefaults
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import kotlin.concurrent.thread

//var color: UIColor = .black
//var author: String
//var type: String
//var title: String
//var description: String
//var startTime: Int?
//var stopTime: Int?
//var place: String?
//var imageLink: String?
//var links: [(String, String)]

class Event(
    var color: Int,
    var author: String,
    var type: String,
    var title: String,
    var description: String,
    var startTime: Long?,
    var stopTime: Long?,
    var place: String?,
    var imageLink: String?,
    var links: ArrayList<Pair<String, String>>
) : Comparable<Event>{
    override fun compareTo(other: Event): Int {
//        if let first = $0.startTime{
//            if let second = $1.startTime {
//            if $0.stopTime != nil && $1.stopTime == nil {
//            return true
//        } else if $0.stopTime == nil && $1.stopTime == nil{
//            return first > second
//        } else {return first < second}
//        }
//            return true
//        }
//        return true
        if (this.startTime != null) {
            if (other.startTime != null){
                if (this.stopTime != null && other.stopTime == null){
                    return -1
                } else if (this.stopTime == null && other.stopTime == null){
                    return if (this.startTime!! < other.startTime!!) 1 else -1
                } else {
                    return if (this.startTime!! > other.startTime!!) 1 else -1
                }
            }
            return 1
        }
        return 1
    }
}

data class PreEvent(
    var color: String,
    var author: String,
    var type: String,
    var title: String,
    var description: String,
    var startTime: Long?,
    var stopTime: Long?,
    var place: String?,
    var image: String?,
    var links: String
){
    fun toEvent(): Event {
        val tempLinks = this.links.split(",")
        val newLinks = ArrayList<Pair<String, String>>()
        for (i in 0..tempLinks.size - 2 step 2) {
            newLinks.add(Pair(tempLinks[i], tempLinks[i + 1]))
        }
        return Event(
            color = Color.parseColor(this.color),
            author = this.author,
            type = this.type,
            title = this.title,
            description = this.description,
            startTime = this.startTime,
            stopTime = this.stopTime,
            place = this.place,
            imageLink = this.image,
            links = newLinks
        )
    }
}

data class manyEvents(
    val data: ArrayList<PreEvent>
){}

fun loadEvents(callback: (ArrayList<Event>) -> Unit){
    val url = URL(eventsURL)
    try {
        val JSONstring = url.readText()
        Log.d("json", JSONstring)
        val preData = Klaxon().parse<manyEvents>(JSONstring)
        if (preData != null){
            val result = ArrayList<Event>()
            for (preEvent in preData.data){
                result.add(preEvent.toEvent())
            }
            callback(result)
        }
    } catch (e: Exception){
        Log.d("Events went wrong", e.toString())
    }
}

fun linksFromArray(input: String): ArrayList<Pair<String, String>>{
    val unorderedArray = input.split(",")
    val output = ArrayList<Pair<String, String>>()
    for (i in 0..unorderedArray.size - 2 step 2){
        output.add(Pair(unorderedArray[i], unorderedArray[i+1]))
    }
    return output
}

fun newLoadEvents(context: Context, success: (ArrayList<Event>) -> Unit, error: () -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val request = JsonObjectRequest(Request.Method.GET, eventsURL, null, { response ->
        val output = ArrayList<Event>()
        val eventsDataArray = response.getJSONArray("data")
        for (i in 0 until eventsDataArray.length()){
            val eventData = eventsDataArray.getJSONObject(i)
            val color = Color.parseColor(eventData.getString("color"))
            val author = eventData.getString("author")
            val type = eventData.getString("type")
            val title = eventData.getString("title")
            val description = eventData.getString("description")

            val startTime = if (eventData.isNull("startTime")){
                null
            } else {eventData.getLong("startTime")}
            val stopTime = if (eventData.isNull("stopTime")) null else eventData.getLong("stopTime")
            val place = if (eventData.isNull("place")) null else eventData.getString("place")
            val imageLink = if (eventData.isNull("image")) null else eventData.getString("image")
            val linksString = eventData.getString("links")
            val links = linksFromArray(linksString)

            output.add(Event(color, author, type, title, description, startTime, stopTime, place, imageLink, links))
        }
        success(output)
    }, { error ->
        Log.d("request@newLoadEvents", error.toString())
        error()
    })
    queue.add(request)
}

var freeEvent : Event? = null

var localID : Int? = null

fun setBadge(value: Int, activity: MainActivity?){
    if (activity != null){
        activity.runOnUiThread{
            activity.setBadge(value)
        }
    } else {
        Log.d("Bagde didn't work", "setBadgeFailed, activity is null")
    }
}

fun updateLastID(main: MainActivity?, context: Context){
    thread {
        loadLastID(context, success = { onlineID ->
            val existingID = loadSavedID(context)
            if (existingID == null) {
                saveID(onlineID, context)
                return@loadLastID
            }
            localID = onlineID
            if (onlineID > existingID) {
                setBadge(onlineID - existingID, main)
            }
        }, fail = {})
    }
}

fun resetBadge(main: MainActivity?, context: Context){
    setBadge(0, main)
    thread {
        loadLastID(context, success = { onlineID ->
            saveID(onlineID, context)
        }, fail = {
            val id = localID ?: loadSavedID(context) ?: return@loadLastID
            saveID(id, context)
        })
    }
}

fun saveID(ID: Int, context: Context) {
    setDefaults("lastID", ID, context)
}

fun loadSavedID(context: Context): Int? {
    val saved = getDefaults("lastID", context)
    if (saved != 0) {return saved}
    else {return null}
}

fun loadLastID(context: Context, success: (Int) -> Unit, fail: () -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, lastIdURL, null,
        { response ->
            val ID = response.getInt("lastID")
            success(ID)
        },
        { error ->
            Log.d("loadlastID error", error.message.toString())
            fail()
        }
    )
    queue.add(jsonObjectRequest)
}

//fun updateBadge(num: Int, act: AppCompatActivity) {
//    val bar = act.findViewById<BottomNavigationView>(R.id.nav_view)
//    bar.getOrCreateBadge(R.id.navigation_notifications).number = num
//}
