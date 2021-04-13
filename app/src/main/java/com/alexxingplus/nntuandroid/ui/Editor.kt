package com.alexxingplus.nntuandroid.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.alexxingplus.nntuandroid.R


val mainStartTimes : Array<String> = arrayOf("7:30", "9:20", "11:10", "13:15", "15:00", "16:45", "18:30", "20:15")
val mainStopTimes : Array<String> = arrayOf("9:05", "10:55", "12:45", "14:50", "16:35", "18:20", "20:05", "21:50")
val sStartTimes : Array<String> = arrayOf("8:00", "9:45", "11:35", "13:40", "15:25", "17:10", "18:55", "20:40")
val sStopTimes : Array<String> = arrayOf("9:35", "11:20", "13:10", "15:15", "17:00", "18:45", "20:30", "22:15")


class DisTime (
    var Name : String?,
    var Aud: String?,
    var StartTime : String?,
    var StopTime : String?
){}
class Day {
    var paras = ArrayList<DisTime>()
    init {
        paras = arrayListOf(DisTime(null, null, null, null),DisTime(null, null, null, null),DisTime(null, null, null, null),DisTime(null, null, null, null),DisTime(null, null, null, null),DisTime(null, null, null, null))
    }
}
class Week {
    var Days = ArrayList<Day>()
    init {
        Days = arrayListOf(Day(),Day(),Day(),Day(),Day(),Day())
    }
}
class TimeTable {
    var weeks = ArrayList<Week>()
    init {
        weeks = arrayListOf(Week(),Week())
    }


    override fun toString(): String {
        val output = StringBuilder()
        for (i in 0..1){
            for (j in 0..5){
                for (k in 0..5){
                    val lesson = this.weeks[i].Days[j].paras[k]
                    if (lesson.StartTime != null && lesson.StartTime != ""){
                        output.append(lesson.StartTime)
                    } else {output.append("null")}
                    output.append("&&&")
                    if (lesson.StopTime != null && lesson.StopTime != ""){
                        output.append(lesson.StopTime)
                    } else {output.append("null")}
                    output.append("&&&")
                    if (lesson.Name != null && lesson.Name != ""){
                        output.append(lesson.Name)
                    } else {output.append("null")}
                    output.append("&&&")
                    if (lesson.Aud != null && lesson.Aud != ""){
                        output.append(lesson.Aud)
                    } else {output.append("null")}
                    output.append("&&&")
                }
            }
        }
        return output.substring(0, output.length)
    }
}

fun Day.copyTo(to: Day){
    val from = this
    for (i in 0..5){
        to.paras[i].StopTime = from.paras[i].StopTime
        to.paras[i].StartTime = from.paras[i].StartTime
        to.paras[i].StartTime = from.paras[i].StartTime
        to.paras[i].Name = from.paras[i].Name
        to.paras[i].Aud = from.paras[i].Aud
    }
}

class Editor : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)

    var days = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        days = arrayListOf(getString(R.string.Понедельник), getString(R.string.Вторник), getString(R.string.Среда), getString(R.string.Четверг), getString(R.string.Пятница), getString(R.string.Суббота))

        var nowWeek : Int = 0
        var today : Int = 0

        var controllerData = TimeTable()



        var startFields: Array<EditText> = arrayOf(
            findViewById(R.id.firstStartField),
            findViewById(R.id.secondStartField),
            findViewById(R.id.thirdStartField),
            findViewById(R.id.fourthStartField),
            findViewById(R.id.fifthStartField),
            findViewById(R.id.sixthStartField)
        )
        var stopFields: Array<EditText> = arrayOf(
            findViewById(R.id.firstStopField),
            findViewById(R.id.secondStopField),
            findViewById(R.id.thirdStopField),
            findViewById(R.id.fourthStopField),
            findViewById(R.id.fifthStopField),
            findViewById(R.id.sixthStopField)
        )
        var roomFields: Array<EditText> = arrayOf(
            findViewById(R.id.firstRoomField),
            findViewById(R.id.secondRoomField),
            findViewById(R.id.thirdRoomField),
            findViewById(R.id.fourthRoomField),
            findViewById(R.id.fifthRoomField),
            findViewById(R.id.sixthRoomField)
        )
        var nameFields: Array<EditText> = arrayOf(
            findViewById(R.id.firstNameField),
            findViewById(R.id.secondNameEditField),
            findViewById(R.id.thirdNameField),
            findViewById(R.id.fourthNameField),
            findViewById(R.id.fifthNameField),
            findViewById(R.id.sixthNameField)
        )
        val prevDayButton : ImageButton = findViewById(R.id.leftEditButton)
        val nextDayButton : ImageButton = findViewById(R.id.rightEditButton)
        val dayLabel : TextView = findViewById(R.id.editorDayLabel)
        val changeWeekButton : TextView = findViewById(R.id.changeEditWeekButton)
        val saveButton : Button = findViewById(R.id.editorSaveButton)
        val moreActionsButton : Button = findViewById(R.id.moreActionsButton)
        val group = intent.getStringExtra("group")

        fun saveInfo(){
            for (i in 0..nameFields.count()-1){
                controllerData.weeks[nowWeek].Days[today].paras[i].Name = nameFields[i].text.toString()
                controllerData.weeks[nowWeek].Days[today].paras[i].Aud = roomFields[i].text.toString()
                controllerData.weeks[nowWeek].Days[today].paras[i].StartTime = startFields[i].text.toString()
                controllerData.weeks[nowWeek].Days[today].paras[i].StopTime = stopFields[i].text.toString()
            }
        }

        fun updateInfo(){
            dayLabel.text = days[today]
            for (i in 0..nameFields.count()-1){
                if (controllerData.weeks[nowWeek].Days[today].paras[i].Name != "null"){
                    nameFields[i].setText(controllerData.weeks[nowWeek].Days[today].paras[i].Name)
                }
                if (controllerData.weeks[nowWeek].Days[today].paras[i].Aud != "null"){
                    roomFields[i].setText(controllerData.weeks[nowWeek].Days[today].paras[i].Aud)
                }
                if (controllerData.weeks[nowWeek].Days[today].paras[i].StartTime != "null"){
                    startFields[i].setText(controllerData.weeks[nowWeek].Days[today].paras[i].StartTime)
                }
                if (controllerData.weeks[nowWeek].Days[today].paras[i].StopTime != "null"){
                    stopFields[i].setText(controllerData.weeks[nowWeek].Days[today].paras[i].StopTime)
                }
            }
        }

        saveInfo()
        updateInfo()


        prevDayButton.setOnClickListener {
            if (today > 0){
                saveInfo()
                today --
                updateInfo()
            }
        }

        nextDayButton.setOnClickListener {
            if (today < 5){
                saveInfo()
                today ++
                updateInfo()
            }
        }

        changeWeekButton.setOnClickListener {
            saveInfo()
            if (nowWeek == 0){
                changeWeekButton.text = "Нечетная"
                nowWeek = 1
            } else {
                changeWeekButton.text = "Четная"
                nowWeek = 0
            }
            updateInfo()
        }

        fun postTheTimeTable(){
            val queue = Volley.newRequestQueue(this)
            val url = "http://194.58.97.17/post.php"

            val request = object : StringRequest(Method.POST, url, Response.Listener<String> { response ->
                Log.i("Респонс", response)
                Toast.makeText(this, getString(R.string.Расписание_загружено), Toast.LENGTH_SHORT).show()
            }, Response.ErrorListener { error ->
                Toast.makeText(this, getString(R.string.Ошибка_при_загрузке), Toast.LENGTH_SHORT).show()
                Log.d("Оно не сработало", error.toString())
            }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["data"] = controllerData.toString()
                    params["name"] = "$group.txt"
                    return params
                }
            }
            queue.add(request)
        }

        saveButton.setOnClickListener {
            saveInfo()
            postTheTimeTable()
//            dbHandler.insertTimeTable(controllerData)
            Toast.makeText(this, getString(R.string.Расписание_сохранено), Toast.LENGTH_SHORT).show()
            Log.i("Кнопка:", "сохранила")
        }



        fun loadTimeTable(){
            val cursor = dbHandler.getAllRow()
            cursor!!.moveToFirst()
            val loadedArray = ArrayList<DisTime>()

            while (cursor?.isAfterLast == false){
//                val name = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME))
//                val room = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_ROOM))
//                val startTime = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_STARTTIME))
//                val stopTime = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_STOPTIME))
//                val id = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
//                Log.i("ID ячейки", id)
//                loadedArray.add(DisTime(name, room, startTime, stopTime))
//                cursor.moveToNext()
            }

            controllerData = arrayToTimeTable(loadedArray)
            updateInfo()
        }

        fun cleartheDay(){
            controllerData.weeks[nowWeek].Days[today] = Day()
            updateInfo()
        }

        fun copyFromAnotherWeek(){
            var otherWeek : Int
            if (nowWeek == 0){
                otherWeek = 1
            } else {otherWeek = 0}

            controllerData.weeks[otherWeek].Days[today].copyTo(controllerData.weeks[nowWeek].Days[today])
            updateInfo()
        }

        moreActionsButton.setOnClickListener {
            val dialog = Dialog(this)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.more_actions_popup)
            val loadButton : Button = dialog.findViewById(R.id.loadTimeTableButton)
            val copyButton : Button = dialog.findViewById(R.id.copyDayButton)
            val deleteButton : Button = dialog.findViewById(R.id.deleteTheDayButton)
            val cancelButton : Button = dialog.findViewById(R.id.backToEditButton)
            loadButton.setOnClickListener {
                loadTimeTable()
                dialog.dismiss()
            }
            copyButton.setOnClickListener {
                copyFromAnotherWeek()
                dialog.dismiss()
            }
            deleteButton.setOnClickListener {
                cleartheDay()
                dialog.dismiss()
            }
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        for (i in 0..roomFields.count()-1){
            roomFields[i].addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = roomFields[i].text.toString()
                    if (text == ""){
                        startFields[i].setText("")
                        stopFields[i].setText("")
                    } else if (text[0].toString() == "6"){
                        startFields[i].setText(sStartTimes[i])
                        stopFields[i].setText(sStopTimes[i])
                    } else if (text[0].toString() == "1" || text[0].toString() == "2" || text[0].toString() == "3" || text[0].toString() == "4" || text[0].toString() == "5"){
                        startFields[i].setText(mainStartTimes[i])
                        stopFields[i].setText(mainStopTimes[i])
                    }
                }
            })
        }
    }



}
