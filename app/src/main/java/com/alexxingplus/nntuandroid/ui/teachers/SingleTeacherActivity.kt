package com.alexxingplus.nntuandroid.ui.teachers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.alexxingplus.nntuandroid.R
import org.w3c.dom.Text

val nameCellID = 0
val experienceID = 1
val pairID = 2
val phoneID = 3
val emailID = 4

class SingleTeacherActivity : AppCompatActivity() {
    fun getPairs(tchr: Teacher): ArrayList<Pair<String, String>> {
        var result = ArrayList<Pair<String, String>>()
        if (tchr.branch != null) {result.add(Pair("Филиал", tchr.branch))}
        if (tchr.disciplines != null) {result.add(Pair("Преподаваемые дисциплины", tchr.disciplines))}
        if (tchr.educationLevel != null) {result.add(Pair("Уровень образования", tchr.educationLevel))}
        if (tchr.qualification != null) {result.add(Pair("Квалификация", tchr.qualification))}
        if (tchr.academicDegree != null) {result.add(Pair("Учёная степень", tchr.academicDegree))}
        if (tchr.academicRank != null) {result.add(Pair("Учёное звание", tchr.academicRank))}
        if (tchr.specialty != null) {result.add(Pair("Направление подготовки и (или) специальности", tchr.specialty))}
        if (tchr.moreEducation != null) {result.add(Pair("Повышение квалификации и (или) профессиональная переподготовка", tchr.moreEducation))}
        return result
    }

    fun getCellArray(tchr: Teacher, dataPairs: ArrayList<Pair<String, String>>? = null): ArrayList<Int>{
        val pairs = if (dataPairs != null){
            dataPairs
        } else {getPairs(tchr)}
        val result = ArrayList<Int>()
        result.add(nameCellID)
        if (tchr.experience != null && tchr.specExperience != null){
            result.add(experienceID)
        }
        for (pair in pairs){
            result.add(pairID)
        }
        if (tchr.phone != null){
            result.add(phoneID)
        }
        if (tchr.email != null){
            result.add(emailID)
        }
        return result
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_teacher)

        val list = findViewById<ListView>(R.id.singleTeacherList)
        var data: Teacher? = null

        fun updateList(data: Teacher? = null){
            if (data == null) {return}
            val pairs = getPairs(data!!)
            val cellArray = getCellArray(data!!, pairs)
            list.adapter = singleAdapter(this, data, cellArray, pairs)
        }

        if (freeTeacher != null){
            data = freeTeacher!!
            updateList(data)
        } else {
            Toast.makeText(this, "Данные преподавателя не были загружены", Toast.LENGTH_LONG).show()
        }
    }

    private class singleAdapter(act: AppCompatActivity, data: Teacher, cellsPositionArray: ArrayList<Int>, pairs: ArrayList<Pair<String, String>>) : BaseAdapter() {

        val act: AppCompatActivity
        val data: Teacher
        val cellsPositionArray: ArrayList<Int>
        val pairs: ArrayList<Pair<String, String>>

        var experienceLength = 0

        init {
            this.act = act
            this.data = data
            this.cellsPositionArray = cellsPositionArray
            this.pairs = pairs

            experienceLength = if (data.experience != null) 1 else 0
        }

        override fun getItem(position: Int): Any {
            return "hehe"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return cellsPositionArray.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(act)
            val cellID = cellsPositionArray[position]
            if (cellID == nameCellID) {
                val cell = inflater.inflate(R.layout.single_teacher_fio_cell, parent, false)!!
                val name = cell.findViewById<TextView>(R.id.teacherNameLabel)
                val pos = cell.findViewById<TextView>(R.id.teacherPositionLabel)
                name.text = data.name.replace(" ","\n")
                pos.text = data.position
                return cell
            } else if (cellID == experienceID){
                val cell = inflater.inflate(R.layout.single_teacher_experience_cell, parent, false)!!
                val exp = cell.findViewById<TextView>(R.id.teacherExperienceLabel)
                val specExp = cell.findViewById<TextView>(R.id.teacherSpecExperienceLabel)

                exp.text = data.experience
                specExp.text = data.specExperience
                return cell
            } else if (cellID == pairID){
                val cell = inflater.inflate(R.layout.single_teacher_data_pair, parent, false)!!
                val key = cell.findViewById<TextView>(R.id.teacherCellDataKey)
                val value = cell.findViewById<TextView>(R.id.teacherCellData)

                val pair = pairs[position - experienceLength - 1]
                key.text = pair.first
                value.text = pair.second
                return cell
            } else if (cellID == phoneID){
                val cell = inflater.inflate(R.layout.single_teacher_contact_cell, parent, false)!!
                val key = cell.findViewById<TextView>(R.id.teacherContactKey)
                val value = cell.findViewById<TextView>(R.id.teacherContactValue)
                val card = cell.findViewById<CardView>(R.id.teacherContactCard)

                key.text = "Контактный телефон"
                value.text = data.phone

                val phoneToCall = data.phone!!.replace("+7","8").replace(" ","").replace("(","").replace(")","")

                card.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + phoneToCall)
                    act.startActivity(intent)
                }

                return cell
            } else { // if (cellID == emailID)
                val cell = inflater.inflate(R.layout.single_teacher_contact_cell, parent, false)!!
                val key = cell.findViewById<TextView>(R.id.teacherContactKey)
                val value = cell.findViewById<TextView>(R.id.teacherContactValue)
                val card = cell.findViewById<CardView>(R.id.teacherContactCard)

                card.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_EMAIL, data.email)
                    act.startActivity(intent)
                }

                key.text = "Электронная почта"
                value.text = data.email
                return cell
            }
        }

    }
}