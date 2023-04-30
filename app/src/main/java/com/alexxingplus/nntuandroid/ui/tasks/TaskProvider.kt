package com.alexxingplus.nntuandroid.ui.tasks

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class TaskProvider(val context: Context) {
    fun download(group: String, completion: (ArrayList<Task>?) -> Unit){
        val req = TaskRequest.Get(group)
        request(req.url.toString()){ obj ->
            val jsonObject = obj ?: run {
                completion(null)
                return@request
            }
            val tasks = parseTasks(jsonObject)
            completion(tasks)
        }
    }

    fun upload(task: Task, group: String, completion: (Boolean) -> Unit) {
        val req = TaskRequest.Add(task, group)
        request(req.url.toString()) { obj ->
            completion(obj != null)
        }
    }

    fun delete(id: Int, completion: (Boolean) -> Unit){
        val req = TaskRequest.Delete(id)
        request(req.url.toString()) { obj ->
            completion(obj != null)
        }
    }

    private fun request(url: String, completion: (JSONObject?) -> Unit){
        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(url, { response ->
            completion(response)
        }, { error ->
            Log.d("TaskProvider.Request", error.localizedMessage)
            completion(null)
        })
        queue.add(req)
    }

    private fun parseTasks(obj: JSONObject): ArrayList<Task> {
        val output = ArrayList<Task>()
        val jsonArray = obj.getJSONArray("tasks")
        for (i in 0 until jsonArray.length()) {
            val taskObject = jsonArray.getJSONObject(i)
            output.add(Task.init(taskObject))
        }
        return output
    }
}