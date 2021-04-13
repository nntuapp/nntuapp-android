package com.alexxingplus.nntuandroid.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.R
import java.util.*
import kotlin.collections.ArrayList

class ShowALessonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_a_lesson)

        val dataList: ListView = findViewById(R.id.lessonDataList)
        if (freeLesson != emptyLesson()){
            dataList.adapter = LessonAdapter(this, freeLesson)
            freeLesson = emptyLesson()
        } else {
            Toast.makeText(this, "Данные не найдены. Сообщите об этом разработчику", Toast.LENGTH_LONG)
        }
    }
    private class LessonAdapter (context : Context, lesson: Lesson): BaseAdapter() {

        val mContext: Context
        val lesson: Lesson
        var roomsAvaible = ArrayList<String>()
        var images = ArrayList<Drawable>()

        fun getImage (c: Context, imgname: String) : Drawable {
            return c.resources.getDrawable(c.resources.getIdentifier(imgname, "mipmap", c.packageName), c.theme)
        }

        init {
            mContext = context
            this.lesson = lesson
            for (room in lesson.rooms){
                try {
                    val name = "b" + room.replace(" ", "")
                    val tempImage : Drawable = getImage(mContext, name)
                    images.add(tempImage)
                    roomsAvaible.add(room.replace(" ", ""))
                } catch (e: Exception){
                    Log.i("Картинка не найдена", "b" + room.replace(" ", ""))
                }
            }
        }


        override fun getCount () : Int {
            return roomsAvaible.count() + 1
        }

        override fun getItemId(position: Int) : Long {
            return position.toLong()
        }

        override fun getItem (position: Int) : Any {
            return "Test String"
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(mContext)
            if (position == 0){
                val cell : View = inflater.inflate(R.layout.lesson_show_data_cell_dbtt, parent, false)
                val nameLabel: TextView = cell.findViewById(R.id.lessonNameLabel)
                val typeAndNotesLabel: TextView = cell.findViewById(R.id.typeAndNotesLabel)
                val dayLabel: TextView = cell.findViewById(R.id.weekDayLabel)
                val timeLabel: TextView = cell.findViewById(R.id.timeLabel)
                val weeksLabel: TextView = cell.findViewById(R.id.weeksLabel)
                val teacherLabel: TextView = cell.findViewById(R.id.teacherLabel)
                val roomLabel: TextView = cell.findViewById(R.id.roomLabel)
                var tempString = ""

                dayLabel.text = daysOfWeek[lesson.day]
                timeLabel.text = "${lesson.startTime} – ${lesson.stopTime}"
                teacherLabel.text = lesson.teacher
                roomLabel.text = stringFromRooms(lesson.rooms).replace(",", ", ")

                nameLabel.text = lesson.name
                tempString = lesson.type
                if (lesson.notes != ""){
                    if (lesson.type != ""){
                        tempString += ", "
                    }
                    tempString += lesson.notes
                }
                typeAndNotesLabel.text = tempString

                tempString = ""

                with(lesson, {
                    if (weeks.contains(-2)){
                        tempString += "<font color = #0072bc>Четные недели</font>"
                    }
                    if (weeks.contains(-1)){
                        if (weeks.contains(-2)){
                            tempString += " +\n"
                        }
                        tempString += "<font color = #9e280e>Нечетные недели</font>"
                    }
                    val isRegular = weeks.contains(-2) || weeks.contains(-1)
                    var added = 0
                    for (week in weeks){
                        if (week > 0){
                            if (isRegular && added == 0){
                                tempString += " +\n"
                            }
                            added += 1
                            tempString += week.toString() + ", "
                        }
                    }
                    if (added > 0){
                        tempString = tempString.dropLast(2)
                        if (added > 1){
                            tempString += " недели"
                        } else {
                            tempString += " неделя"
                        }

                    }
                })
                weeksLabel.setText(HtmlCompat.fromHtml(tempString, HtmlCompat.FROM_HTML_MODE_LEGACY))

                return cell
            } else {
                val cell : View = inflater.inflate(R.layout.image_room_cell, parent, false)
                val roomName: TextView = cell.findViewById(R.id.roomImageLabel)
                val image: ImageView = cell.findViewById(R.id.roomImage)

                roomName.text = roomsAvaible[position - 1]
                image.setImageDrawable(images[position - 1])
                cell.setOnClickListener {
                    val intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("room", roomsAvaible[position - 1])
                    mContext.startActivity(intent)
                }
                return cell
            }
        }
    }

}