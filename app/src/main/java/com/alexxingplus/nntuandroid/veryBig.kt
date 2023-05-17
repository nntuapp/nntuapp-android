package com.alexxingplus.nntuandroid

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import com.alexxingplus.nntuandroid.ui.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
 * Implementation of App Widget functionality.
 */
class veryBig : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateBigWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateBigWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.very_big)

    val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
    val shortDaysOfWeek = arrayOf("вс", "пн", "вт", "ср", "чт", "пт", "сб")
    val DaysOfWeek = arrayOf("воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота")

    var arrangedTT = ArrayList<Lesson>()
    var data = ArrayList<Lesson>()

    // очень большая функция, которая на вход принимает массив пар и просто их выводит в виджет
    // она в каждой паре проверяет, есть ли имя, и если есть, то он её рисует, и наоборот
    fun updateLabels(input: ArrayList<Lesson>){
        if (input[0].name != ""){
            views.setTextViewText(R.id.firstStartLabelWidget, input[0].startTime)
            views.setTextViewText(R.id.firstStopLabelWidget, input[0].stopTime)
            views.setTextViewText(R.id.firstNameLabelWidget, input[0].name)
            if (input[0].rooms.count() > 0){
                views.setTextViewText(R.id.firstRoomButtonWidget, input[0].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.firstStartLabelWidget, "")
            views.setTextViewText(R.id.firstStopLabelWidget, "")
            views.setTextViewText(R.id.firstNameLabelWidget, "")
            views.setTextViewText(R.id.firstRoomButtonWidget, "")
        }


        if (input[1].name != ""){
            views.setTextViewText(R.id.secondStartLabelWidget, input[1].startTime)
            views.setTextViewText(R.id.secondStopLabelWidget, input[1].stopTime)
            views.setTextViewText(R.id.secondNameLabelWidget, input[1].name)
            if (input[1].rooms.count() > 0){
                views.setTextViewText(R.id.secondRoomButtonWidget, input[1].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.secondStartLabelWidget, "")
            views.setTextViewText(R.id.secondStopLabelWidget, "")
            views.setTextViewText(R.id.secondNameLabelWidget, "")
            views.setTextViewText(R.id.secondRoomButtonWidget, "")
        }


        if (input[2].name != ""){
            views.setTextViewText(R.id.thirdStartLabelWidget, input[2].startTime)
            views.setTextViewText(R.id.thirdStopLabelWidget, input[2].stopTime)
            views.setTextViewText(R.id.thirdNameLabelWidget, input[2].name)
            if (input[2].rooms.count() > 0){
                views.setTextViewText(R.id.thirdRoomButtonWidget, input[2].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.thirdStartLabelWidget, "")
            views.setTextViewText(R.id.thirdStopLabelWidget, "")
            views.setTextViewText(R.id.thirdNameLabelWidget, "")
            views.setTextViewText(R.id.thirdRoomButtonWidget, "")
        }


        if (input[3].name != ""){
            views.setTextViewText(R.id.fourthStartLabelWidget, input[3].startTime)
            views.setTextViewText(R.id.fourthStopLabelWidget, input[3].stopTime)
            views.setTextViewText(R.id.fourthNameLabelWidget, input[3].name)
            if (input[3].rooms.count() > 0){
                views.setTextViewText(R.id.fourthRoomButtonWidget, input[3].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.fourthStartLabelWidget, "")
            views.setTextViewText(R.id.fourthStopLabelWidget, "")
            views.setTextViewText(R.id.fourthNameLabelWidget, "")
            views.setTextViewText(R.id.fourthRoomButtonWidget, "")
        }

        if (input[4].name != ""){
            views.setTextViewText(R.id.fifthStartLabelWidget, input[4].startTime)
            views.setTextViewText(R.id.fifthStopLabelWidget, input[4].stopTime)
            views.setTextViewText(R.id.fifthNameLabelWidget, input[4].name)
            if (input[4].rooms.count() > 0){
                views.setTextViewText(R.id.fifthRoomButtonWidget, input[4].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.fifthStartLabelWidget, "")
            views.setTextViewText(R.id.fifthStopLabelWidget, "")
            views.setTextViewText(R.id.fifthNameLabelWidget, "")
            views.setTextViewText(R.id.fifthRoomButtonWidget, "")
        }

        if (input[5].name != ""){
            views.setTextViewText(R.id.sixthStartLabelWidget, input[5].startTime)
            views.setTextViewText(R.id.sixthStopLabelWidget, input[5].stopTime)
            views.setTextViewText(R.id.sixthNameLabelWidget, input[5].name)
            if (input[5].rooms.count() > 0){
                views.setTextViewText(R.id.sixthRoomButtonWidget, input[0].rooms[0])
            }
        } else {
            views.setTextViewText(R.id.sixthStartLabelWidget, "")
            views.setTextViewText(R.id.sixthStopLabelWidget, "")
            views.setTextViewText(R.id.sixthNameLabelWidget, "")
            views.setTextViewText(R.id.sixthRoomButtonWidget, "")
        }

    }

    val day = Calendar.getInstance(Locale.GERMANY)
    var nowDay = day.get(Calendar.DAY_OF_WEEK)
    var nowWeek = day.get(Calendar.WEEK_OF_YEAR) - startWeek + additionalWeek
    val nowHour = day.get(Calendar.HOUR_OF_DAY)
    val nowMinute = day.get(Calendar.MINUTE)

    var actualLesson = emptyLesson()
    var nextLesson = emptyLesson()

    if (nowDay == 1) {
        nowDay = 7
    }
    else {nowDay -= 1}

    nowDay -= 1

    views.setTextViewText(R.id.dayLabelBigWidget, "Сегодня ${day.get(Calendar.DATE)} ${months[day.get(Calendar.MONTH)]}, ${shortDaysOfWeek[day.get(Calendar.DAY_OF_WEEK)-1]}")

    fun fillIn(){
        var actualLesson = emptyLesson()
        var dayData = ArrayList<Lesson>()

        val todayLessons = filterDayWeek(arrangedTT, nowDay, nowWeek)
        val time = nowHour*100 + nowMinute

        for (lesson in todayLessons){
            if (estTimeFromString(lesson.stopTime) > time){
                actualLesson = lesson
                break
            }
        }
        if (actualLesson.name == "" && actualLesson.weeks == ArrayList<Int>()){
            val nextDay = if (nowDay == 6) 0 else nowDay + 1
            val nextweek = if (nowDay == 6) nowWeek + 1 else nowWeek
            arrangedTT = if (nowDay == 6) getTTArrayforWidget(data, nextweek) else arrangedTT
            val nextDayLessons = filterDayWeek(arrangedTT, nextDay, nextweek)
            if (nextDayLessons.count() > 0){
                views.setTextViewText(R.id.dayLabelBigWidget, "Завтра")
                actualLesson = nextDayLessons[0]
            }
        }
        var output = ArrayList<Lesson>()
        if (actualLesson.name != "" && actualLesson.weeks != ArrayList<Int>()){
            for (lesson in arrangedTT){
                if (lesson.day == actualLesson.day){
                    output.add(lesson)
                }
            }
        }
        while (output.count() < 6){
            output.add(emptyLesson())
        }
        updateLabels(output)
    }


    thread {
        val dbHandler = DBHelper(context!!, null)
        data = dbHandler.loadTTfromSQLite()
        arrangedTT = getTTArrayforWidget(data, nowWeek)
        fillIn()
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


    //достаем из памяти все пары на отдельном треде с помощью DBHelper
//    thread{
//
//        val dbHandler = DBHelper(requireContext(), null)
//        val cursor = dbHandler.getAllRow()
//        val loadedArray = ArrayList<DisTime>()
//        // то же самое в TimetableFragment.kt
//        cursor!!.moveToFirst()
//
//        //с помощью курсора он считывает данные со строк по определенным колонкам, и добавляет пару в массив пар
//        while (cursor?.isAfterLast == false){
////            val name = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME))
////            val room = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_ROOM))
////            val startTime = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_STARTTIME))
////            val stopTime = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_STOPTIME))
////            val id = cursor?.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
////            Log.i("ID ячейки", id)
////            loadedArray.add(DisTime(name, room, startTime, stopTime))
////            cursor.moveToNext()
//        }
//        dbHandler.close()
//
//        //достаем данные о сегодняшнем дне
//        val day = Calendar.getInstance(Locale.US)
//        val nowDay = day.get(Calendar.DAY_OF_WEEK)
//        val nowWeek = (day.get(Calendar.WEEK_OF_YEAR) + 1)%2
//        val nowHour = day.get(Calendar.HOUR_OF_DAY)
//        val nowMinute = day.get(Calendar.MINUTE)
//
//
//        // 1 это индекс воскресенья, в тот день пар точно нет и при случае равенства 1 алгоритм не может вести себя корректно,
//        // потому что 1-2 это число отрицательное, значит позиция будет вычисляться неправильно, что не очень круто
//        if (nowDay != 1 && loadedArray.count() != 0){
//            var startPosition = (nowDay - 2)*6 + nowWeek*36
//            //переменная аound отвечает за то, найдена пара или нет
//            // могла быть булевой (потому что принимает значения только 0 или 1), но была взята из smol виджета
//            var found = 0
//            // работаем в значениях 0 1 2 3 4 5
//            for (i in startPosition..startPosition + 5) {
//
//                val para = loadedArray[i]
//
//                if (para.StartTime != null && found == 0 && para.StartTime != ""){
//                    var cleanTime = para.StopTime!!
//
//                    //работаем со строкой StopTime, очищаем её от двоеточих и точек, а также лишних символов
//                    if (para.StopTime == null) {cleanTime = "0"}
//                    cleanTime = cleanTime.replace(":", "")
//                    cleanTime = cleanTime.replace(".", "")
//                    val intStop = cleanTime.toInt()
//                    val intTime = nowHour*100 + nowMinute
//
//                    //если пара еще не закончилась, мы создем массив пар, соответствующий дню, и выводим его
//                    //при помощи updateLabels()
//                    if (intTime <= intStop){
//                        views.setTextViewText(R.id.dayLabelBigWidget, "Расписание на сегодня")
//                        found = 1
//                        val dayTime = ArrayList<DisTime>()
//                        for (j in startPosition..startPosition + 5){
//                            dayTime.add(loadedArray[j])
//                        }
//                        updateLabels(dayTime)
//                    }
//                }
//            }
//
//            //если ни одной пары сегодня не найдено, мы смотрим следующий день
//            if (found == 0){
//                //если сейчас суббота нечетной недели, то следующий учебный день - понедельник четной
//                if (startPosition == 66){
//                    startPosition = 0
//                }
//                //иначе просто передвигаемся на день (6 пар) вперед
//                else {
//                    startPosition += 6
//                }
//                // если позиция 0 или 36, то сейчас суббота, и он будет писать не "расписание на завтра", а
//                // "расписание на понедельник"
//                if (startPosition == 0 || startPosition == 36){
//                    views.setTextViewText(R.id.dayLabelBigWidget, "Расписание на понедельник")
//                } else {views.setTextViewText(R.id.dayLabelBigWidget, "Расписание на завтра")}
//                for (i in  startPosition..startPosition + 5){
//                    val para = loadedArray[i]
//
//                    //выводим пару точно так же, как делали это выше
//                    if (para.Name != null && found == 0 && para.Name != ""){
//                        found = 1
//                        val dayTime = ArrayList<DisTime>()
//                        for (j in startPosition..startPosition + 5){
//                            dayTime.add(loadedArray[j])
//                        }
//                        updateLabels(dayTime)
//                    }
//                }
//
//                // если все равно ничего не найдено, создаем массив из пустых пар и выводим его
//                if (found == 0){
//                    val dayTime = ArrayList<DisTime>()
//                    val emptyThing = DisTime(null, null, null, null)
//                    for (j in 0..5){
//                        dayTime.add(emptyThing)
//                    }
//                    updateLabels(dayTime)
//                }
//            }
//        }
//        // не забываем про воскресенье, там создаем точно такие же позиции и ищем пары, как мы их искали чуть выше
//        else if (loadedArray.count() != 0){
//            var innerWeek = nowWeek
//            if (nowWeek == 0) {innerWeek = 1} else {innerWeek = 0}
//            var startPosition = 5*6 + innerWeek*36
//
//            var found = 0
//            if (startPosition == 66){
//                startPosition = 0
//            } else {
//                startPosition += 6
//            }
//            // проходим цикл и если что-то находим, то выводим
//            for (i in  startPosition..startPosition + 5){
//                val para = loadedArray[i]
//                if (para.Name != null && found == 0 && para.Name != ""){
//                    found = 1
//                    val dayTime = ArrayList<DisTime>()
//                    for (j in startPosition..startPosition + 5){
//                        dayTime.add(loadedArray[j])
//                    }
//                    updateLabels(dayTime)
//                }
//            }
//            //если ничего не нашли, то выводим пустоту
//            if (found == 0){
//                val dayTime = ArrayList<DisTime>()
//                val emptyThing = DisTime(null, null, null, null)
//                for (j in 0..5){
//                    dayTime.add(emptyThing)
//                }
//                updateLabels(dayTime)
//            }
//        }

        // обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetId, views)

    // Instruct the widget manager to update the widget

}