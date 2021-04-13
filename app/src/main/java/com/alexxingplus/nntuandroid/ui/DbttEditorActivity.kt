package com.alexxingplus.nntuandroid.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import com.alexxingplus.nntuandroid.R
import java.lang.Exception
import kotlin.concurrent.thread

fun estTimeFromString(input:String): Int{
    var output = 0
    try {
        output = input.replace(":","").toInt()
    } catch (e: Exception){
        Log.i("Парсин времени", e.toString())
    }
    return output
}

class DbttEditorActivity : AppCompatActivity() {

    private var adapter = EditorAdapter(this, ArrayList<Lesson>())
    private var nowDay = 0



    fun filterLessons(tt: ArrayList<Lesson>, day: Int): ArrayList<Lesson>{
        var output = ArrayList<Lesson>()
        for (lesson in tt){
            if (lesson.day == day){
                output.add(lesson)
            }
        }
        output.sortBy{ estTimeFromString(it.startTime) }
        return output
    }

    override fun onResume() {
        super.onResume()
        fun showAMessage(){
            var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
            dialog.setTitle("Автозагрузка теперь выключена 👌")
            dialog.setMessage("Чтобы измененное расписание не заменилось загруженным с интернета, автозагрузка отключена. Ваши изменения в расписании сохраняются на устройстве автоматически. Также вы можете загрузить расписание на сервер")
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            dialog.show()
        }
        fun saveTT(){
            thread {
                val db = DBHelper(this, null)
                db.saveTT(editingTT)
//                runOnUiThread {
//                    Toast.makeText(this, "Сохранено! Теперь вы можете загрузить это расписание на сервер", Toast.LENGTH_LONG).show()
//                }
            }
        }


        if (freeLesson.name != "" && freeLesson.stopTime != "" && freeLesson.stopTime != "" && freeLesson.weeks != ArrayList<Int>()){
            val isUpdating = getDefaults("isUpdating", this) ?: 0 == 1
            if (isUpdating){
                showAMessage()
                setDefaults("isUpdating", -1, this)
            }
            if (index == -2){
                editingTT.add(freeLesson)
            } else if (index >= 0){
                editingTT[index] = freeLesson
            }
            index = -1
            freeLesson = emptyLesson()
            saveTT()
            adapter.data = filterLessons(editingTT, nowDay)
            adapter.notifyDataSetChanged()
        } else if (index != -1) {
            val isUpdating = getDefaults("isUpdating", this) ?: 0 == 1
            if (isUpdating){
                showAMessage()
                setDefaults("isUpdating", -1, this)
            }
            if (index >= 0){
                editingTT.removeAt(index)
            }
            index = -1
            saveTT()
            adapter.data = filterLessons(editingTT, nowDay)
            adapter.notifyDataSetChanged()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dbtt_editor)
        setSupportActionBar(findViewById(R.id.toolbar))
//        var nowDay = 0

        var listView: ListView = findViewById(R.id.lessonList)
        val nextDayButton: ImageButton = findViewById(R.id.rightEditButton)
        val prevDayButton: ImageButton = findViewById(R.id.leftEditButton)
//        var saveButton: ImageButton = findViewById(R.id.saveButton)
        val addButton: FloatingActionButton = findViewById(R.id.floatingAddButton)
        val dayLabel: TextView = findViewById(R.id.dayLabel)
        val days : Array<String> = arrayOf(getString(R.string.Понедельник), getString(R.string.Вторник), getString(R.string.Среда), getString(R.string.Четверг), getString(R.string.Пятница), getString(R.string.Суббота), getString(R.string.Воскресенье))
        val group =  intent.getStringExtra("group") ?: ""
        listView.adapter = adapter

        thread {
            val db = DBHelper(this, null)
            val savedLessons = db.loadTTfromSQLite()
            if (editingTT.size == 0){
                editingTT = savedLessons
            }
            val autoUpdate = getDefaults("isUpdating", this) ?: 0 == 1
            if (editingTT.size == 0 || autoUpdate){
                downloadTT(group, this, fun(data){
                    if (data.size > 0){
                        editingTT = data
                    }
                    adapter.data = filterLessons(editingTT, nowDay)
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
            })
            } else {
                adapter.data = filterLessons(editingTT, nowDay)
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }
        }



        fun updateLayout(){
            nextDayButton.isClickable = true
            nextDayButton.isEnabled = true
            nextDayButton.alpha = 1F

            prevDayButton.isClickable = true
            prevDayButton.isEnabled = true
            prevDayButton.alpha = 1F

            if (nowDay >= 6){
                nowDay = 6
                nextDayButton.isEnabled = false
                nextDayButton.isClickable = false
                nextDayButton.alpha = 0.5F
            }
            if (nowDay <= 0){
                nowDay = 0
                prevDayButton.isEnabled = false
                prevDayButton.isClickable = false
                prevDayButton.alpha = 0.5F
            }
            dayLabel.text = days[nowDay]
        }

        fun updateDay(){
            adapter.data = filterLessons(editingTT, nowDay)
            adapter.notifyDataSetChanged()
            updateLayout()
        }

        updateLayout()

//        val viewToSwipe: View = findViewById(R.id.lessonList)
//        val swiper = object : OnSwipeTouchListener(this@DbttEditorActivity){
//            override fun onSwipeLeft() {
//                super.onSwipeLeft()
//                nowDay += 1
//                updateLayout()
//                updateDay()
//            }
//
//            override fun onSwipeRight() {
//                super.onSwipeRight()
//                nowDay -= 1
//                updateLayout()
//                updateDay()
//            }
//        }
//
//        viewToSwipe.setOnTouchListener(swiper)
////        listView.setOnTouchListener(swiper)

        nextDayButton.setOnClickListener {
            nowDay +=1
            updateLayout()
//            if (nowDay >= 6){
//                nowDay = 6
//                nextDayButton.isClickable = false
//                nextDayButton.alpha = 0.5F
//            } else {
//                nextDayButton.isClickable = true
//                nextDayButton.alpha = 1F
//                prevDayButton.isClickable = true
//                prevDayButton.alpha = 1F
//                nowDay += 1
//            }
            updateDay()
        }

        prevDayButton.setOnClickListener {
            nowDay -= 1
            updateLayout()
//            if (nowDay <= 0){
//                nowDay = 0
//                prevDayButton.isClickable = false
//                prevDayButton.alpha = 0.5F
//            } else {
//                nextDayButton.isClickable = true
//                nextDayButton.alpha = 1F
//                prevDayButton.isClickable = true
//                prevDayButton.alpha = 1F
//                nowDay -= 1
//
//            }
            updateDay()
        }

        addButton.setOnClickListener {
            index = -2
            val intent = Intent(this, singleEditorDBTT::class.java)
            freeLesson.day = nowDay
            this.startActivity(intent)
        }

//        saveButton.setOnClickListener {
//            setDefaults("isUpdating", -1, this)
//            thread {
//                val db = DBHelper(this, null)
//                db.saveTT(editingTT)
//                runOnUiThread {
//                    Toast.makeText(this, "Сохранено! Теперь вы можете загрузить это расписание на сервер", Toast.LENGTH_LONG).show()
//                }
//            }
//        }


    }

    private class EditorAdapter (context: Context, lessonArray: ArrayList<Lesson>) : BaseAdapter() {
        public var data = ArrayList<Lesson>()
        public var mContext: Context
        init {
            mContext = context
            data = lessonArray
        }

        fun copyTheLesson(from: Lesson, to: Lesson){
            to.name = from.name
            to.startTime = from.startTime
            to.stopTime = from.stopTime
            to.teacher = from.teacher
            to.notes = from.notes
            to.day = from.day
            to.type = from.type
            for (week in from.weeks){
                to.weeks.add(week)
            }
            for (room in from.rooms){
                to.rooms.add(room)
            }
        }

        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Any {
            return "Очередной урок"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var inflater = LayoutInflater.from(mContext)
            var cell = inflater.inflate(R.layout.new_editor_cell_dbtt, parent, false)
            var upperLabel = cell.findViewById<TextView>(R.id.upperLabelEditor)
            var nameLabel = cell.findViewById<TextView>(R.id.lessonNameLabelEditor)
            var lowerLabel = cell.findViewById<TextView>(R.id.lowerLabelEditor)
            var startTime = cell.findViewById<TextView>(R.id.startTimeLabelEditor)
            var stopTime = cell.findViewById<TextView>(R.id.stopTimeLabelEditor)
            val card = cell.findViewById<CardView>(R.id.LessonCard)

            card.setOnClickListener {
                freeLesson = data[position]
//                copyTheLesson(data[position], freeLesson)
                val intent = Intent(mContext, singleEditorDBTT::class.java)
                index = editingTT.indexOf(data[position])
                mContext.startActivity(intent)
            }

            startTime.text = data[position].startTime
            stopTime.text = data[position].stopTime
            nameLabel.text = data[position].name

            val i = position
            var tempUpperLabel: String = ""

            if (data[i].weeks.contains(-2)) {
                tempUpperLabel += "<font color = #0072bc>ЧН</font>"
                if (data[i].weeks.contains(-1)) {
                    tempUpperLabel += " + "
                }
            }
            if (data[i].weeks.contains(-1)){
                tempUpperLabel += "<font color = #9e280e>НЧ</font>"
            }
            var added = false
            var tempweeks = ""
            for (week in data[i].weeks){
                if (week != -1 && week != -2){
                    added = true
                    tempweeks += week.toString() + ", "
                }
            }
            if (added){
                tempweeks = tempweeks.dropLast(2)
                if (data[i].weeks.contains(-1) || data[i].weeks.contains(-2)){
                    tempUpperLabel += ", "
                }
                tempUpperLabel += tempweeks
            }

            if (data[i].type != "") {
                tempUpperLabel += ", " + data[i].type
            }
            if (data[i].notes != ""){
                tempUpperLabel += ", " + data[i].notes
            }
            upperLabel.setText(HtmlCompat.fromHtml(tempUpperLabel, HtmlCompat.FROM_HTML_MODE_LEGACY))

            if (data[i].rooms == arrayListOf("")){
                data[i].rooms = ArrayList<String>()
            }
            var tempLowerLabel: String = ""
            if (data[i].rooms.size > 0){
                tempLowerLabel += stringFromRooms(data[i].rooms).replace(",", ", ")
                if (data[i].teacher != ""){
                    tempLowerLabel += "; " + data[i].teacher
                }
            } else if (data[i].teacher != ""){
                tempLowerLabel += data[i].teacher
            }
            lowerLabel.text = tempLowerLabel
            return cell
        }
    }
}