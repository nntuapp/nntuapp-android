package com.alexxingplus.nntuandroid

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.alexxingplus.nntuandroid.ui.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
 * Implementation of App Widget functionality.
 *  ахаххахха а как сделать так чтобы виджет работал хоть как-то....
 */
class smol : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them

        for (appWidgetId in appWidgetIds) {
            updateSmolWidget(context, appWidgetManager, appWidgetId)
        }

    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Toast.makeText(context, "what", Toast.LENGTH_SHORT)
    }
}

fun filterDayWeek(tt: ArrayList<Lesson>, day: Int, week: Int): ArrayList<Lesson>{
    var output = ArrayList<Lesson>()
    for (lesson in tt){
        val weekCompatible = lesson.weeks.contains(week) || (lesson.weeks.contains(-2) && week%2 == 0) || (lesson.weeks.contains(-1) && week%2 == 1)
        if (lesson.day == day && weekCompatible){
            output.add(lesson)
        }
    }
    output.sortBy { timeFromString(it.startTime) }
    return output
}

fun getTTArrayforWidget(tt: ArrayList<Lesson>, week: Int): ArrayList<Lesson>{
    var output = ArrayList<Lesson>()
    for (i in 0..6){
        output.addAll(filterDayWeek(tt, i, week))
    }
    return output
}


internal fun updateSmolWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    // это мы будем использовать для того, чтобы собирать дату в виджете
    val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
    val shortDaysOfWeek = arrayOf("вс", "пн", "вт", "ср", "чт", "пт", "сб")
    val DaysOfWeek = arrayOf("воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота")

    var data = ArrayList<Lesson>()
    var arrangedTT = ArrayList<Lesson>()


    val views = RemoteViews(context.packageName, R.layout.smol)
//    val updateIntent = Intent (context, smol::class.java)
//    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
//    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, smol::class.java))
//    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
//    val pintent = PendingIntent.getService(context, 0, updateIntent, 0)
//    views.setOnClickPendingIntent(R.id.disNameWidget, pintent)
//    Toast.makeText(context, "Update happened", Toast.LENGTH_SHORT)

    val day = Calendar.getInstance(Locale.GERMANY)
    var nowDay = day.get(Calendar.DAY_OF_WEEK)
    var nowWeek = day.get(Calendar.WEEK_OF_YEAR) - 5
    val nowHour = day.get(Calendar.HOUR_OF_DAY)
    val nowMinute = day.get(Calendar.MINUTE)
    var actualLesson = emptyLesson()
    var nextLesson = emptyLesson()

    if (nowDay == 1) {
        nowDay = 7
//        nowWeek -= 1
    }
    else {nowDay -= 1}

    nowDay -= 1

    fun fillIn(){
        //пишем дату
        views.setTextViewText(R.id.dayLabelWidget, "Сегодня ${day.get(Calendar.DATE)} ${months[day.get(Calendar.MONTH)]}, ${shortDaysOfWeek[day.get(Calendar.DAY_OF_WEEK)-1]}")


        val time = nowHour*100 + nowMinute

        val todayLessons = filterDayWeek(arrangedTT, nowDay, nowWeek)

        for (lesson in todayLessons){
            if (estTimeFromString(lesson.stopTime) > time){
                actualLesson = lesson
                if (todayLessons.indexOf(lesson) < todayLessons.size - 1){
                    nextLesson = todayLessons[todayLessons.indexOf(lesson) + 1]
                }
                break
            }
        }
        if (actualLesson.name == "" && actualLesson.weeks == ArrayList<Int>()){
            var nextweek = if (nowDay == 6) nowWeek + 1 else nowWeek
            val nextDay = if (nowDay == 6) 0 else nowDay + 1
            arrangedTT = if (nowDay == 6) getTTArrayforWidget(data, nextweek) else arrangedTT
            val nextDayLessons = filterDayWeek(arrangedTT, nextDay, nextweek)
            if (nextDayLessons.count() != 0){
                views.setTextViewText(R.id.dayLabelWidget, "Завтра")
                actualLesson = nextDayLessons[0]
                if (nextDayLessons.count() > 1){
                    nextLesson = nextDayLessons[1]
                }
            }
        }

        if (actualLesson.name != "" && actualLesson.weeks != ArrayList<Int>()){
            views.setTextViewText(R.id.startTimeLabelWidget, actualLesson.startTime)
            views.setTextViewText(R.id.stopTimeLabelWidget, actualLesson.stopTime)
            views.setTextViewText(R.id.disNameWidget, actualLesson.name + if (actualLesson.type != "") "(${actualLesson.type})" else "")
            if (nextLesson.name != "" && nextLesson.weeks != ArrayList<Int>()){
                views.setTextViewText(R.id.NextLabel, "Далее ${nextLesson.name} ${if (nextLesson.rooms.count() != 0) ", " + nextLesson.rooms[0] else ""} в ${nextLesson.startTime}")
            } else {
                views.setTextViewText(R.id.NextLabel, "Последняя пара! Хорошего дня ☀️")
            }
        } else {
            views.setTextViewText(R.id.dayLabelWidget, "Завтра")
            views.setTextViewText(R.id.disNameWidget, "Занятий нет")
            views.setTextViewText(R.id.NextLabel, "Можно отдохнуть 💅")
        }

    }


    thread {
        val dbHandler = DBHelper(context!!, null)
        data = dbHandler.loadTTfromSQLite()
        arrangedTT = getTTArrayforWidget(data, nowWeek)
        fillIn()
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


//    views.setTextViewText(R.id.NextLabel, "Прекол")
        //достаем дату и разбираем на части



//        var dayTime = Day()
        // 1 = воскресенье, в воскресенье пар точно нет
//        if (nowDay != 1){
//            //работаем с массивом, ищем пару, которая сейчас. позиции сегодняшнего дня - 6 пар
//            var startPosition = (nowDay - 2)*6 + nowWeek*36
//            // found = 0 - ничего не найдено, 1 - найдена одна, 2 - найдены обе
//            var found = 0
//            var i = startPosition
//            for (i in startPosition..startPosition+5) {
//                val para = loadedArray[i]
//                var pointRange = 0
//                Log.d(para.StartTime, para.StopTime + " " + para.Name)
//                if (para.StartTime != null && found == 0 && para.StartTime != "") {
//
//                    var cleanTime = para.StopTime!!
//                    if (para.StopTime == null) {cleanTime = "0"}
//                    cleanTime = cleanTime.replace(":", "")
//                    cleanTime = cleanTime.replace(".", "")
//
//                    val intStop = cleanTime.toInt()
//
//                    val intTime = nowHour*100 + nowMinute
//                    //смотрим по условию, и если подходит, то меняем название и цифры
//                    if (intTime <= intStop){
//                        found = 1
//                        if (para.Aud != null && para.Aud != "null" && para.Aud != ""){
//                            views.setTextViewText(R.id.disNameWidget, para.Name + ", " + para.Aud)
//                        } else {
//                            views.setTextViewText(R.id.disNameWidget, para.Name)
//                        }
//                        views.setTextViewText(R.id.startTimeLabelWidget, para.StartTime)
//                        views.setTextViewText(R.id.stopTimeLabelWidget, para.StopTime)
//                    }
//                } else if (para.StartTime != null && found == 1 && para.StartTime != ""){
//                    //находим следующую пару
//                    found = 2
//                    views.setTextViewText(R.id.NextLabel, "Далее: " + para.Name + ", " + para.Aud + " в " + para.StartTime)
//                }
//            }
//            if (found == 1){
//                //если нашлась только одна
//                views.setTextViewText(R.id.NextLabel, "Последняя пара! Хорошего дня ☀️")
//            }
//            if (found == 0){
//                //если нет никаких, ищем пары на следующий день
//                // 30 - суббота четной недели (нулевой)
//                if (startPosition == 30){
//                    startPosition += 6
//                    views.setTextViewText(R.id.dayLabelWidget, "В понедельник");
//                }
//                // 66 - суббота нечетной недели (единичной)
//                else if (startPosition == 66){
//                    startPosition = 0
//                    views.setTextViewText(R.id.dayLabelWidget, "В понедельник");
//                } else {
//                    startPosition += 6
//                    views.setTextViewText(R.id.dayLabelWidget, "Завтра " + DaysOfWeek[day.get(Calendar.DAY_OF_WEEK)]);
//                }
//                //делаем по сути то же самое
//                i = startPosition
//                for (i in startPosition..startPosition+5) {
//                    val para = loadedArray[i]
////                    var pointRange = 0
//                    Log.d(para.StartTime, para.StopTime + " " + para.Name)
//                    if (para.StartTime != null && found == 0 && para.StartTime != "") {
//                        //условия по времени больше нет
//                        found = 1
//                        if (para.Aud != null && para.Aud != "null" && para.Aud != ""){
//                            views.setTextViewText(R.id.disNameWidget, para.Name + ", " + para.Aud)
//                        } else {
//                            views.setTextViewText(R.id.disNameWidget, para.Name)
//                        }
//                        views.setTextViewText(R.id.startTimeLabelWidget, para.StartTime)
//                        views.setTextViewText(R.id.stopTimeLabelWidget, para.StopTime)
//                    } else if (para.StartTime != null && found == 1 && para.StartTime != ""){
//                        //просто если есть еще одна пара, то добавляем без каких-либо условий
//                        found = 2
//                        views.setTextViewText(R.id.NextLabel, "Далее: " + para.Name + ", " + para.Aud + " в " + para.StartTime)
//                    }
//                }
//                // если нашлась только одна
//                if (found == 1){
//                    views.setTextViewText(R.id.NextLabel, "Первая и последняя 🎉")
//                }
//                // если ничего не нашлось ВООБЩЕ
//                else if (found == 0){
//                    views.setTextViewText(R.id.disNameWidget, "Занятий нет")
//                    views.setTextViewText(R.id.startTimeLabelWidget, "")
//                    views.setTextViewText(R.id.stopTimeLabelWidget, "")
//                    views.setTextViewText(R.id.NextLabel, "Можно отдохнуть")
//                }
//            }
//        }
//        // не забываем про воскресенье
//        else {
//            //добавлено 6 сентября
//            var innerWeek = nowWeek
//            if (nowWeek == 0) {innerWeek = 1} else {innerWeek = 0}
//            var startPosition = 5*6 + innerWeek*36
//            //
//            var found = 0
//            var i = startPosition
//
//            //не очень понимаю, сейчас же точно не суббота, что за бред, ну и ладно
//            // а может и понятно ахххахаха
//            if (startPosition == 30){
//                startPosition += 6
//                views.setTextViewText(R.id.dayLabelWidget, "В понедельник");
//            }
//            else if (startPosition == 66){
//                startPosition = 0
//                views.setTextViewText(R.id.dayLabelWidget, "В понедельник");
//            } else {
//                startPosition += 6
//                views.setTextViewText(R.id.dayLabelWidget, "Завтра " + DaysOfWeek[day.get(Calendar.DAY_OF_WEEK)]);
//            }
//            //ищем пары срочно
//            for (i in startPosition..startPosition+5) {
//                val para = loadedArray[i]
//                var pointRange = 0
//
//                if (para.StartTime != null && found == 0 && para.StartTime != "") {
//                    //никаких лишних условий
//                    found = 1
//                    if (para.Aud != null && para.Aud != "null" && para.Aud != ""){
//                        views.setTextViewText(R.id.disNameWidget, para.Name + ", " + para.Aud)
//                    } else {
//                        views.setTextViewText(R.id.disNameWidget, para.Name)
//                    }
//                    views.setTextViewText(R.id.startTimeLabelWidget, para.StartTime)
//                    views.setTextViewText(R.id.stopTimeLabelWidget, para.StopTime)
//
//                } else if (para.StartTime != null && found == 1 && para.StartTime != ""){
//                    //просто ищем еще одну
//                    found = 2
//                    views.setTextViewText(R.id.NextLabel, "Далее: " + para.Name + ", " + para.Aud + " в " + para.StartTime)
//                }
//            }
//            // если нашлась только одна
//            if (found == 1){
//                views.setTextViewText(R.id.NextLabel, "Первая и последняя 🎉")
//            }
//            //если ничего не нашлось(((
//            else if (found == 0){
//                views.setTextViewText(R.id.disNameWidget, "Занятий нет")
//                views.setTextViewText(R.id.startTimeLabelWidget, "")
//                views.setTextViewText(R.id.stopTimeLabelWidget, "")
//                views.setTextViewText(R.id.NextLabel, "Можно отдохнуть")
//            }
//        }
//
//        //после того, как все загрузилось, мы в треде обновляем виджет по красоте
//        appWidgetManager.updateAppWidget(appWidgetId, views)
}
