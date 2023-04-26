package com.alexxingplus.nntuandroid.ui.tasks

import android.net.Uri
import java.net.URL

sealed class TaskRequest {
    class Get(val group: String): TaskRequest()
    class Add(val task: Task, val groupName: String): TaskRequest()
    class Delete(val id: Int): TaskRequest()
}

private val TaskRequest.queryItems: HashMap<String, String>
get(){
    return when (this) {
        is TaskRequest.Get -> hashMapOf(
            "method" to "get",
            "groupName" to this.group
        )
        is TaskRequest.Add -> hashMapOf(
            "method" to "add",
            "task" to this.task.JSONString,
            "groupName" to this.groupName
        )
        is TaskRequest.Delete -> hashMapOf(
            "method" to "delete",
            "id" to "${this.id}"
        )
    }
}

val TaskRequest.url: URL
get(){
    val builder = Uri.Builder()
    builder.scheme("http").encodedAuthority("194.58.97.17:3002")
    for (item in this.queryItems){
        builder.appendQueryParameter(item.key, item.value)
    }
    return URL(builder.build().toString())
}