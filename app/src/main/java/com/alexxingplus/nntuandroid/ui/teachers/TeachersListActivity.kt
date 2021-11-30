package com.alexxingplus.nntuandroid.ui.teachers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.alexxingplus.nntuandroid.R

class TeachersListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_list)

        var allTeachers = ArrayList<Teacher>()
        var teacherList = findViewById<ListView>(R.id.teacherList)
        val searchBar = findViewById<EditText>(R.id.teacherSearchBar)

        teacherList.adapter = teacherAdapter(this, allTeachers)

        fun updateTeachers(data: ArrayList<Teacher>){
            var adapter = teacherList.adapter as teacherAdapter
            adapter.teachersToShow = data
            adapter.loading = false
            adapter.notifyDataSetChanged()
        }

        val watcher = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = searchBar.text.toString()
                if (searchText == ""){
                    updateTeachers(allTeachers)
                } else {
                    val newTeachers = findTeachers(allTeachers, searchText)
                    updateTeachers(newTeachers)
                }
            }
        }
        searchBar.addTextChangedListener(watcher)

//        loadTeachers { result ->
//            this.runOnUiThread {
//                allTeachers = result
//                updateTeachers(allTeachers)
//            }
//        }

        newLoadTeachers(this, success = { teachers ->
            this.runOnUiThread{
                allTeachers = teachers
                updateTeachers(allTeachers)
            }
        }, error = {})
    }

    private class teacherAdapter(context: Context, teachersToShow: ArrayList<Teacher>) : BaseAdapter() {

        val mContext: Context
        public var teachersToShow: ArrayList<Teacher>
        public var loading = true
        init{
            mContext = context
            this.teachersToShow = teachersToShow
        }

        override fun getCount(): Int {
            return if (teachersToShow.size == 0){
                1
            } else {
                teachersToShow.size
            }
        }

        override fun getItem(position: Int): Any {
            return "hihi"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(mContext)
            var cell = inflater.inflate(R.layout.room_cell, parent, false)!!
            val name = cell.findViewById<TextView>(R.id.roomName)
            val card = cell.findViewById<CardView>(R.id.roomCard)

            if (teachersToShow.size == 0){
                name.text = if (loading) "Загрузка..." else "Ничего не найдено"
            } else {
                name.text = teachersToShow[position].name
                card.setOnClickListener{
                    val intent = Intent(mContext, SingleTeacherActivity::class.java)
                    freeTeacher = teachersToShow[position]
                    mContext.startActivity(intent)
                }
            }

            if (position == 0){
                val scale: Float = mContext.resources.getDisplayMetrics().density
                val dpAsPixels: Int = (10 * scale + 0.5f).toInt()
                cell.setPadding(0,dpAsPixels,0,0)

            }
            return cell
        }
    }
}

