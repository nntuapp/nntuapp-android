package com.alexxingplus.nntuandroid.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import java.util.*


fun addTTtoCalendar(activity: Activity, tt: ArrayList<Lesson>){
    val context = activity.applicationContext
    checkPermission(42, activity, android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)
    try {
        clearActualCalendar(context)
        val calendar = getCalendar(context, context.contentResolver)
        for (lesson in tt){
            addLessonToCalendar(lesson, context, calendar)
        }
    } catch (e: Exception){
        Log.d("Календарь сломан", e.toString())
        Toast.makeText(activity, "Не удаётся получить доступ к календарю, проверьте настройки устройства", Toast.LENGTH_LONG).show()
    }
}

fun deleteACalendarWithPermission(activity: Activity){
    val context = activity.applicationContext
    checkPermission(42, activity, android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)
    try{
        clearActualCalendar(context)
    } catch (e: Exception){
        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
    }
}


fun checkPermission(
    callbackId: Int,
    activity: Activity,
    vararg permissionsId: String
) {
    var permissions = true
    for (p in permissionsId) {
        permissions =
            permissions && ContextCompat.checkSelfPermission(activity, p) == PERMISSION_GRANTED
    }
    if (!permissions) ActivityCompat.requestPermissions(activity, permissionsId, callbackId)
}

fun addLessonToCalendar(lesson: Lesson, context: Context, calendarID: Long){
    val isEveryWeek = lesson.weeks.contains(-1) && lesson.weeks.contains(-2)
    for (week in lesson.weeks) {
        val startMillis = getStartLong(lesson, isEveryWeek, week)
        val stopMillis = getStopLong(lesson, isEveryWeek, week)

        var description = lesson.type
        if (lesson.teacher != ""){
            if (description != "") {description += ", "}
            description += lesson.teacher
        }
        if (lesson.notes != ""){
            if (description != "") {description += ", "}
            description += lesson.notes
        }

        val interval = if (isEveryWeek) 1 else 2
        val rule = "FREQ=WEEKLY;INTERVAL=$interval;UNTIL=20210531"

        val contentResolver = context.contentResolver

        val event = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, stopMillis)
            put(CalendarContract.Events.TITLE, lesson.name)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, stringFromRooms(lesson.rooms))
            put(CalendarContract.Events.RRULE, rule)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Moscow")
            put(CalendarContract.Events.CALENDAR_ID, calendarID)
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, event)

        if (isEveryWeek) {break}
    }
}

fun getStartLong(lesson:Lesson, isEveryWeek: Boolean, week: Int): Long {
    var tempDay = lesson.day + 1
    if (tempDay == 7) {tempDay = 1}
    else {tempDay += 1}

    var weekOfYear = 0
    if (isEveryWeek || lesson.weeks.contains(-2)){
        weekOfYear = 4 + 1
    } else if (lesson.weeks.contains(-1)){
        weekOfYear = 5 + 1
    } else {weekOfYear = week + 4 + 1}

    val startArray = hMfromString(lesson.startTime)

    return Calendar.getInstance(Locale.GERMANY).run{
        set(Calendar.YEAR, 2021)
        set(Calendar.WEEK_OF_YEAR, weekOfYear)
        set(Calendar.DAY_OF_WEEK, tempDay)
        set(Calendar.HOUR_OF_DAY, startArray[0])
        set(Calendar.MINUTE, startArray[1])
        timeInMillis
    }
}

fun getStopLong(lesson:Lesson, isEveryWeek: Boolean, week: Int): Long {
    var tempDay = lesson.day + 1
    if (tempDay == 7) {tempDay = 1}
    else {tempDay += 1}

    var weekOfYear = 0
    if (isEveryWeek || lesson.weeks.contains(-2)){
        weekOfYear = 4 + 1
    } else if (lesson.weeks.contains(-1)){
        weekOfYear = 5 + 1
    } else {weekOfYear = week + 4 + 1}

    val stopArray = hMfromString(lesson.stopTime)

    return Calendar.getInstance(Locale.GERMANY).run{
        set(Calendar.YEAR, 2021)
        set(Calendar.WEEK_OF_YEAR, weekOfYear)
        set(Calendar.DAY_OF_WEEK, tempDay)
        set(Calendar.HOUR_OF_DAY, stopArray[0])
        set(Calendar.MINUTE, stopArray[1])
        timeInMillis
    }
}

fun hMfromString(input: String): ArrayList<Int>{
    val index = input.indexOf(":")
    val hours = input.prefix(index).toInt()
    val minutes = input.suffix(index + 1).toInt()
    return arrayListOf(hours, minutes)
}

fun clearActualCalendar(context: Context){
    val calID = getDefaultsLong("nntuCal", context)
    if (calID != null && calID != 0.toLong()){
        val accountName = "com.nntu"
        var uri = CalendarContract.Calendars.CONTENT_URI
        uri = uri.buildUpon()
            .appendPath(calID.toString())
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        val contentResolver = context.contentResolver
        contentResolver.delete(uri, null, null)
        setDefaults("nntuCal", 0.toLong(), context)
    }
}



fun getCalendar(context: Context, contentResolver: ContentResolver): Long {
    val calID = getDefaultsLong("nntuCal", context)
    if (calID == null || calID == 0.toLong()){
//
//        val manager = AccountManager.get(context)
//        val accounts = manager.getAccountsByType("com.google")
//        for (account in accounts){
//            accountName = account.name
//            break
//        }

//        val ACCOUNT_NAME = "NNTU"
//        val ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL
//        val PROVIDER = "com.nntu.provider"
//
//        val appAccount = Account(ACCOUNT_NAME, ACCOUNT_TYPE)
//        val accountManager = AccountManager.get(context)
//        if (accountManager.addAccountExplicitly(appAccount, null, null)) {
//            ContentResolver.setIsSyncable(appAccount, PROVIDER, 1)
//            ContentResolver.setMasterSyncAutomatically(true)
//            ContentResolver.setSyncAutomatically(appAccount, PROVIDER, true)
//        }

        val accountName = "com.nntu"
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, "НГТУ")
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "НГТУ")
            put(CalendarContract.Calendars.CALENDAR_COLOR, Color.parseColor("#0072BC"))
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Europe/Moscow")
//            put(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        }

        var uri: Uri = CalendarContract.Calendars.CONTENT_URI
        uri = uri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()

        val result = contentResolver.insert(uri, values)

        val newID = result!!.lastPathSegment!!.toLong()

        setDefaults("nntuCal", newID, context)
        return newID

    } else return calID
}