package com.alexxingplus.nntuandroid.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.alexxingplus.nntuandroid.R
import java.lang.Exception
import com.alexxingplus.nntuandroid.ui.freeLesson

class singleEditorDBTT : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_editor_dbtt)
        setSupportActionBar(findViewById(R.id.toolbar))

        val indexToDelete = index
        index = -1

        val nameField: EditText = findViewById(R.id.nameFieldTT)
        val customTypeField: EditText = findViewById(R.id.otherTypeField)
        val roomField: EditText = findViewById(R.id.roomField)
        val customStartTimeField: EditText = findViewById(R.id.customStartField)
        val customStopTimeField: EditText = findViewById(R.id.customStopField)
        val customWeeksField: EditText = findViewById(R.id.customWeekField)
        val teacherField: EditText = findViewById(R.id.teacherField)
        val notesField: EditText = findViewById(R.id.notesField)

        val typeCards = arrayListOf<CardView>(findViewById(R.id.lectionCard), findViewById(R.id.practiceCard), findViewById(R.id.labCard), findViewById(R.id.otherTypeCard))
        val typeLabels = arrayListOf<TextView>(findViewById(R.id.lectionLabel),findViewById(R.id.practiceLabel),findViewById(R.id.labLabel))

        val timeCards = arrayListOf<CardView>(findViewById(R.id.firstParaCard),findViewById(R.id.secondParaCard),findViewById(R.id.thirdParaCard),findViewById(R.id.fourthParaCard),findViewById(R.id.fifthParaCard),findViewById(R.id.sixthParaCard),findViewById(R.id.seventhParaCard),findViewById(R.id.customParaCard))
        val startTimeLabels = arrayListOf<TextView>(findViewById(R.id.firstParaStart),findViewById(R.id.secondParaStart),findViewById(R.id.thirdParaStart),findViewById(R.id.fourthParaStart),findViewById(R.id.fifthParaStart),findViewById(R.id.sixthParaStart),findViewById(R.id.seventhParaStart))
        val stopTimeLabels = arrayListOf<TextView>(findViewById(R.id.firstParaStop),findViewById(R.id.secondParaStop),findViewById(R.id.thirdParaStop),findViewById(R.id.fourthParaStop),findViewById(R.id.fifthParaStop),findViewById(R.id.sixthParaStop),findViewById(R.id.seventhParaStop))

        val weekCards = arrayListOf<CardView>(findViewById(R.id.evenCard),findViewById(R.id.oddCard),findViewById(R.id.customWeekCard))
        val weekLabels = arrayListOf<TextView>(findViewById(R.id.evenLabel),findViewById(R.id.oddLabel))
        val doneButton: Button = findViewById(R.id.saveLessonButton)
        val deleteButton: Button = findViewById(R.id.deleteLessonButton)

        val labelColor = typeLabels[0].currentTextColor

//        if (freeLesson.name != "" && freeLesson.stopTime != "" && freeLesson.stopTime != "" && freeLesson.weeks != ArrayList<Int>()){
//
//        }




        fun colorizeLabel(text: TextView,active: Boolean){
            if (active){
                text.setTextColor(ContextCompat.getColor(this, R.color.NNTUblue))
            } else {
                text.setTextColor(labelColor)
            }
        }

        fun colorizeTime(index: Int, active: Boolean){
            if (index < startTimeLabels.size){
                colorizeLabel(startTimeLabels[index], active)
                colorizeLabel(stopTimeLabels[index], active)
            }
        }

        fun updateTimes(main: Boolean){
            if (main){
                for (i in (0..startTimeLabels.size - 1)){
                    startTimeLabels[i].text = mainStartTimes[i]
                    stopTimeLabels[i].text = mainStopTimes[i]
                }
            } else {
                for (i in (0..startTimeLabels.size - 1)){
                    startTimeLabels[i].text = sStartTimes[i]
                    stopTimeLabels[i].text = sStopTimes[i]
                }
            }
        }

        updateTimes(false)

        fun colorizeField(field: EditText, active: Boolean){
            if (active){
                field.setTextColor(ContextCompat.getColor(this, R.color.NNTUblue))
            } else {
                field.setTextColor(labelColor)
            }
        }

        fun fillIn(lesson: Lesson){
            nameField.setText(lesson.name)
            teacherField.setText(lesson.teacher)
            notesField.setText(lesson.notes)
            roomField.setText(stringFromRooms(lesson.rooms))

            if (lesson.rooms.size > 0){
                try {
                    val building = Character.getNumericValue(lesson.rooms[0].first())
                    updateTimes(building in 1..5)
                } catch (e: Exception){
                    Log.d("Ошибка в парсе fillIn()", e.toString())
                }
            }

            if (lesson.type == "Лекция"){
                colorizeLabel(typeLabels[0], true)
            } else if (lesson.type == "Практика"){
                colorizeLabel(typeLabels[1], true)
            } else if (lesson.type == "Лаб.работа"){
                colorizeLabel(typeLabels[2], true)
            } else {
                customTypeField.setText(lesson.type)
                colorizeField(customTypeField, true)
            }

            if (sStartTimes.contains(lesson.startTime) && sStopTimes.contains(lesson.stopTime)){
                var isCard = true
                val startIndex = sStartTimes.indexOf(lesson.startTime)
                val stopIndex = sStopTimes.indexOf(lesson.stopTime)
                isCard = isCard && (startIndex == stopIndex)
                isCard = isCard && (startIndex < 7)
                if (isCard){
                    colorizeTime(startIndex, true)
                } else {
                    colorizeField(customStartTimeField, true)
                    colorizeField(customStopTimeField, true)
                    customStartTimeField.setText(lesson.startTime)
                    customStopTimeField.setText(lesson.stopTime)
                }
            } else if (mainStartTimes.contains(lesson.startTime) && mainStopTimes.contains(lesson.stopTime)){
                var isCard = true
                val startIndex = mainStartTimes.indexOf(lesson.startTime)
                val stopIndex = mainStopTimes.indexOf(lesson.stopTime)
                isCard = isCard && (startIndex == stopIndex)
                isCard = isCard && (startIndex < 7)
                if (isCard){
                    colorizeTime(startIndex, true)
                } else {
                    colorizeField(customStartTimeField, true)
                    colorizeField(customStopTimeField, true)
                    customStartTimeField.setText(lesson.startTime)
                    customStopTimeField.setText(lesson.stopTime)
                }
            } else {
                colorizeField(customStartTimeField, true)
                colorizeField(customStopTimeField, true)
                customStartTimeField.setText(lesson.startTime)
                customStopTimeField.setText(lesson.stopTime)
            }

            var tempWeeks = ArrayList<Int>()
            for (week in lesson.weeks) {
                tempWeeks.add(week)
            }
            if (tempWeeks.contains(-2)){
                colorizeLabel(weekLabels[0], true)
                tempWeeks.remove(-2)
            }
            if (tempWeeks.contains(-1)){
                colorizeLabel(weekLabels[1], true)
                tempWeeks.remove(-1)
            }
            if (tempWeeks.size > 0){
                colorizeField(customWeeksField, true)
                customWeeksField.setText(stringFromWeeks(tempWeeks).replace(",", ", "))
            }
        }

        if (freeLesson.name != "" && freeLesson.stopTime != "" && freeLesson.stopTime != "" && freeLesson.weeks != ArrayList<Int>()){
            fillIn(freeLesson)
        }




        //Действия по клику по карточке типа

        for (i in (0..typeCards.size - 1)){
            if (i != typeCards.size - 1){
                typeCards[i].setOnClickListener {
                    if (freeLesson.type != typeLabels[i].text.toString()){
                        freeLesson.type = typeLabels[i].text.toString()
                        for (j in (0..typeCards.size - 2)){
                            if (i == j){
                                colorizeLabel(typeLabels[j], true)
                            } else {
                                colorizeLabel(typeLabels[j], false)
                            }
                        }
                        colorizeField(customTypeField, false)
                    }
                }
            } else {
                typeCards[i].setOnClickListener{
                    if (customTypeField.text.toString() != ""){
                        colorizeField(customTypeField, true)
                        for (label in typeLabels){
                            colorizeLabel(label, false)
                        }
                        freeLesson.type = customTypeField.text.toString()
                    }
                }
            }
        }



        //Действия по клику по карточке времени

        for (i in 0..timeCards.size - 1){
            if (i != timeCards.size - 1){
                timeCards[i].setOnClickListener {
                    if (freeLesson.startTime != startTimeLabels[i].text.toString()){
                        freeLesson.startTime = startTimeLabels[i].text.toString()
                        freeLesson.stopTime = stopTimeLabels[i].text.toString()

                        freeLesson.startTime = freeLesson.startTime.replace("-", ":")
                        freeLesson.startTime = freeLesson.startTime.replace("–", ":")
                        freeLesson.startTime = freeLesson.startTime.replace("/", ":")
                        freeLesson.startTime = freeLesson.startTime.replace("_", ":")
                        freeLesson.startTime = freeLesson.startTime.replace(" ", "")

                        freeLesson.stopTime = freeLesson.stopTime.replace("-", ":")
                        freeLesson.stopTime = freeLesson.stopTime.replace("–", ":")
                        freeLesson.stopTime = freeLesson.stopTime.replace("/", ":")
                        freeLesson.stopTime = freeLesson.stopTime.replace("_", ":")
                        freeLesson.stopTime = freeLesson.stopTime.replace(" ", "")

                        for (j in (0..timeCards.size - 2)){
                            if (i != j){
                                colorizeTime(j, false)
                            } else {
                                colorizeTime(j, true)
                            }
                        }
                        colorizeField(customStartTimeField, false)
                        colorizeField(customStopTimeField, false)
                    }
                }
            } else {
                if (customStartTimeField.text.toString() != "" || customStopTimeField.text.toString() != ""){
                    colorizeField(customStartTimeField, false)
                    colorizeField(customStopTimeField, false)
                    for (j in (0..timeCards.size - 2)){
                        colorizeLabel(startTimeLabels[j], false)
                        colorizeLabel(stopTimeLabels[j], false)
                    }
                    freeLesson.startTime = customStartTimeField.text.toString()
                    freeLesson.stopTime = customStopTimeField.text.toString()

                    freeLesson.startTime = freeLesson.startTime.replace("-", ":")
                    freeLesson.startTime = freeLesson.startTime.replace("–", ":")
                    freeLesson.startTime = freeLesson.startTime.replace("/", ":")
                    freeLesson.startTime = freeLesson.startTime.replace("_", ":")
                    freeLesson.startTime = freeLesson.startTime.replace(" ", "")

                    freeLesson.stopTime = freeLesson.stopTime.replace("-", ":")
                    freeLesson.stopTime = freeLesson.stopTime.replace("–", ":")
                    freeLesson.stopTime = freeLesson.stopTime.replace("/", ":")
                    freeLesson.stopTime = freeLesson.stopTime.replace("_", ":")
                    freeLesson.stopTime = freeLesson.stopTime.replace(" ", "")
                }
            }
        }


        //Действия по клику по карточкам недель

        weekCards[0].setOnClickListener {
            if (freeLesson.weeks.contains(-2) == false){
                freeLesson.weeks.add(-2)
                colorizeLabel(weekLabels[0], true)
            } else {
                freeLesson.weeks.remove(-2)
                colorizeLabel(weekLabels[0], false)
            }
        }

        weekCards[1].setOnClickListener {
            if (freeLesson.weeks.contains(-1) == false){
                freeLesson.weeks.add(-1)
                colorizeLabel(weekLabels[1], true)
            } else {
                freeLesson.weeks.remove(-1)
                colorizeLabel(weekLabels[1], false)
            }
        }

        roomField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    var tempString = roomField.text.toString()
                    tempString = tempString.replace(";", ",")
                    tempString = tempString.replace(".", ",")
                    tempString = tempString.replace("_", ",")
                    freeLesson.rooms = if (roomsFromString(tempString) != arrayListOf("")) roomsFromString(tempString) else ArrayList<String>()
                    if (freeLesson.rooms.size == 0) {return}
                    val room = Character.getNumericValue(freeLesson.rooms[0].first())
                    if (room == 6){
                        if (mainStartTimes.contains(freeLesson.startTime) && mainStopTimes.contains(freeLesson.stopTime)){
                            val index = mainStartTimes.indexOf(freeLesson.startTime)
                            if (index < startTimeLabels.size){
                                freeLesson.startTime = sStartTimes[index]
                                freeLesson.stopTime = sStopTimes[index]
                            }
                        }
                        updateTimes(false)
                    } else if (room > 0 && room < 6){
                        if (sStartTimes.contains(freeLesson.startTime) && sStopTimes.contains(freeLesson.stopTime)){
                            val index = sStartTimes.indexOf(freeLesson.startTime)
                            if (index < startTimeLabels.size){
                                freeLesson.startTime = mainStartTimes[index]
                                freeLesson.stopTime = mainStopTimes[index]
                            }
                        }
                        updateTimes(true)
                    }

                } catch (e: Exception) {
                    Log.d("Оно не сработало", e.toString())
                }
            }
        })

        customTypeField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (customTypeField.text.toString() != ""){
                    for (label in typeLabels){
                        colorizeLabel(label, false)
                    }
                    colorizeField(customTypeField, true)
                    freeLesson.type = customTypeField.text.toString()
                }

            }
        })

        customWeeksField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (customWeeksField.text.toString() != ""){
                    colorizeField(customWeeksField, true)
                    val newWeeks = ArrayList<Int>()
                    if (freeLesson.weeks.contains(-2)){
                        newWeeks.add(-2)
                    }
                    if (freeLesson.weeks.contains(-1)){
                        newWeeks.add(-1)
                    }
                    var tempString = customWeeksField.text.toString()
                    tempString = tempString.replace(" ", "")
                    tempString = tempString.replace(";", ",")
                    tempString = tempString.replace(".", ",")
                    tempString = tempString.replace("_", ",")

                    val addition = weeksFromString(tempString)
                    for (week in addition){
                        newWeeks.add(week)
                    }
                    newWeeks.remove(0)
                    freeLesson.weeks = newWeeks
                }
            }
        })

        customStartTimeField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (customStartTimeField.text.toString() != ""){
                    for (i in (0..timeCards.size - 1)){
                        colorizeTime(i, false)
                    }
                    freeLesson.startTime = customStartTimeField.text.toString()
                }
            }
        })

        customStopTimeField.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (customStopTimeField.text.toString() != ""){
                    for (i in (0..timeCards.size - 1)){
                        colorizeTime(i, false)
                    }
                    freeLesson.stopTime = customStopTimeField.text.toString()
                }
            }
        })

        doneButton.setOnClickListener {
            index = indexToDelete
            var done = true
            if (freeLesson.rooms == arrayListOf("")){
                freeLesson.rooms = ArrayList<String>()
            }
            if (nameField.text.toString() == ""){
                Toast.makeText(this, "Название не указано", Toast.LENGTH_LONG).show()
                done = false
            }
            if (freeLesson.startTime == ""){
                Toast.makeText(this, "Укажите начальное время занятия", Toast.LENGTH_LONG).show()
                done = false
            }
            if (freeLesson.stopTime == ""){
                Toast.makeText(this, "Укажите конечное время занятия", Toast.LENGTH_LONG).show()
                done = false
            }
            if (done){
                if (freeLesson.weeks == ArrayList<Int>()){
                    freeLesson.weeks = arrayListOf(-2,-1)
                }
                freeLesson.name = nameField.text.toString()
                freeLesson.teacher = teacherField.text.toString()
                freeLesson.notes = notesField.text.toString()
                finish()
            }
        }

        deleteButton.setOnClickListener {
            index = indexToDelete
            freeLesson = emptyLesson()
            finish()
        }
    }
}