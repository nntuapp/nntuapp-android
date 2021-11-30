package com.alexxingplus.nntuandroid.ui.teachers

import android.content.Context
import android.util.Log
import com.alexxingplus.nntuandroid.ui.teachersURL
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

data class Teacher (
    val name: String,
    val position: String,

    val disciplines: String?,
    val educationLevel : String?,
    val qualification: String?,
    val academicDegree: String?,
    val academicRank: String?,
    val specialty: String?,
    val moreEducation: String?,
    val experience: String?,
    val specExperience: String?,

    val phone: String?,
    val email: String?,
    val branch: String?
){
    override fun equals(other: Any?): Boolean {
        if (other is Teacher){
            return this.name.equals(other.name)
        } else {return false}
    }
}

fun loadTeachers(callback: (ArrayList<Teacher>) -> Unit){
    var url = URL(teachersURL)
    thread {
        try {
            var JSONstring = url.readText()
            var result = Klaxon().parseArray<Teacher>(JSONstring)
            if (result != null){
                callback(ArrayList(result.sortedBy {it.name}))
            }
        } catch (e: Exception){
            Log.d("Teachers went wrong", e.toString())
            callback(ArrayList<Teacher>())
        }
    }
}

fun newLoadTeachers(context: Context, success: (ArrayList<Teacher>) -> Unit, error: () -> Unit){
    val queue = Volley.newRequestQueue(context)
    val output = ArrayList<Teacher>()
    val request = JsonArrayRequest(Request.Method.GET, teachersURL, null, { array ->
        for (i in 0 until array.length()){
            val teacher = array.getJSONObject(i)
            val name = teacher.getString("name")
            val position = teacher.getString("position")

            val disciplines = getOptionalString(teacher, "disciplines")

            val educationLevel = getOptionalString(teacher, "educationLevel")
            val qualification = getOptionalString(teacher, "qualification")
            val academicDegree = getOptionalString(teacher, "academicDegree")
            val academicRank = getOptionalString(teacher, "academicRank")
            val specialty = getOptionalString(teacher, "specialty")
            val moreEducation = getOptionalString(teacher, "moreEducation")
            val experience = getOptionalString(teacher, "experience")
            val specExperience = getOptionalString(teacher, "specExperience")

            val phone = getOptionalString(teacher, "phone")
            val email = getOptionalString(teacher, "email")
            val branch = getOptionalString(teacher, "branch")

            output.add(Teacher(name, position, disciplines, educationLevel, qualification, academicDegree, academicRank, specialty, moreEducation, experience, specExperience, phone, email, branch))
        }
        success(output)
    }, { error ->
        Log.d("request@newLoadTeachers", error.toString())
        error()
    })
    queue.add(request)
}

fun getOptionalString(obj: JSONObject, key: String): String? {
    return if (obj.isNull(key)) null else obj.getString(key)
}

fun findTeachers(data: ArrayList<Teacher>, keyword: String): ArrayList<Teacher>{
    val output = ArrayList<Teacher>()
    for (teacher in data){
        if (teacher.name.toUpperCase().contains(keyword.toUpperCase())){
            output.add(teacher)
        }
    }
    return ArrayList(output.sortedBy{it.name})
}

var freeTeacher : Teacher? = null