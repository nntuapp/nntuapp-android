package com.alexxingplus.nntuandroid.ui

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.alexxingplus.nntuandroid.R

class AverageMarkActivity : AppCompatActivity() {

    fun consistencyAlert(){
        var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
        dialog.setTitle("Семестры пропущены!")
        dialog.setMessage("Данных о некоторых семестрах в приложении нет, поэтому средний балл по некоторым дисциплинам может вычисляться неточно, или вообще отсутсвовать.")
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", DialogInterface.OnClickListener { dialogInterface, _ ->
            dialogInterface.dismiss()
        })
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_average_mark)

        var diploma = HashMap<String, Int>()
        var averageSems = ArrayList<Double>()
        var consistent = false

        try {
            diploma = intent.getSerializableExtra("diploma") as HashMap<String, Int>
            averageSems = intent.getSerializableExtra("averageSems") as ArrayList<Double>
            consistent = intent.getBooleanExtra("consistent", false)
        } catch (e: Exception){
            Toast.makeText(this, "Не удалось вычислить средний балл. Свяжитесь с разработчиком", Toast.LENGTH_LONG).show()
        }

        if (!consistent){
            consistencyAlert()
        }

        val list: ListView = findViewById(R.id.averageMarksList)
        val averageOverall = intent.getDoubleExtra("averageOverall", 0.0)

//        diploma = intent.getSerializableExtra("diploma") as HashMap<String, Int>
//        val averageSems = intent.getSerializableExtra("averageSems") as? ArrayList<Double> ?: ArrayList<Double>()

        list.adapter = AverageAdapter(this, diploma, averageOverall, averageSems)
    }

    private class AverageAdapter(context: Context, diploma: HashMap<String, Int>, averageOverall: Double, averageSems: ArrayList<Double>) : BaseAdapter() {

        private val diploma: HashMap<String, Int>
        private val mContext: Context
        private val averageOverall: Double
        private val averageSems: ArrayList<Double>
        private val keys: List<String>

        init {
            this.mContext = context
            this.diploma = diploma
            this.averageOverall = averageOverall
            this.averageSems = averageSems
            this.keys = diploma.keys.sorted()
        }

        override fun getCount(): Int {
            return 1 + keys.size + averageSems.size
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
                val cell = inflater.inflate(R.layout.average_mark_cell_w_header, parent, false)
                val semNameLabel: TextView = cell.findViewById(R.id.semesterName)
                val averageMark: TextView = cell.findViewById(R.id.averageMark)
                val header: TextView = cell.findViewById(R.id.cellHeader)
                semNameLabel.text = "Средний балл"
                averageMark.text = averageOverall.toString()
                header.text = "Общий"
                return cell
            } else if (position == 1){
                val cell = inflater.inflate(R.layout.average_mark_cell_w_header, parent, false)
                val semNameLabel: TextView = cell.findViewById(R.id.semesterName)
                val averageMark: TextView = cell.findViewById(R.id.averageMark)
                val header: TextView = cell.findViewById(R.id.cellHeader)
                semNameLabel.text = keys[position - 1]
                averageMark.text = diploma[keys[position - 1]].toString()
                header.text = "По дисциплинам"
                return cell
            } else if (position <= keys.size){
                val cell = inflater.inflate(R.layout.average_mark_cell, parent, false)
                val semNameLabel: TextView = cell.findViewById(R.id.semesterName)
                val averageMark: TextView = cell.findViewById(R.id.averageMark)
                semNameLabel.text = keys[position - 1]
                averageMark.text = diploma[keys[position - 1]].toString()
                return cell
            } else if (position == keys.size + 1){
                val cell = inflater.inflate(R.layout.average_mark_cell_w_header, parent, false)
                val semNameLabel: TextView = cell.findViewById(R.id.semesterName)
                val averageMark: TextView = cell.findViewById(R.id.averageMark)
                val header: TextView = cell.findViewById(R.id.cellHeader)
                val cellSem = position - keys.size
                semNameLabel.text = "$cellSem семестр"
                if (!averageSems[position - 1 - keys.size].isNaN() && averageSems[position - 1 - keys.size] != 0.0){
                    averageMark.text = averageSems[position - 1 - keys.size].toString()
                } else {
                    averageMark.text = null
                }
                header.text = "По семестрам"
                return cell
            } else {
                val cell = inflater.inflate(R.layout.average_mark_cell, parent, false)
                val semNameLabel: TextView = cell.findViewById(R.id.semesterName)
                val averageMark: TextView = cell.findViewById(R.id.averageMark)
                val cellSem = position - keys.size
                semNameLabel.text = "$cellSem семестр"
                if (!averageSems[position - 1 - keys.size].isNaN() && averageSems[position - 1 - keys.size] != 0.0){
                    averageMark.text = averageSems[position - 1 - keys.size].toString()
                } else {
                    averageMark.text = null
                }
                return cell
            }
        }


    }
}