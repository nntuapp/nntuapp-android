package com.alexxingplus.nntuandroid.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.news.updateLastID
import java.util.*
import kotlin.concurrent.thread


fun String.upperBound (input: String) : Int {
    return intern().indexOf(input) + input.length
}

fun String.lowerBound (input: String) : Int {
    return intern().indexOf(input)
}

fun String.suffix (from: Int) : String {
    try {
        return intern().substring(startIndex = from, endIndex = intern().length)
    } catch (e: Exception){
        Log.d("Ошибка в suffix", e.toString())
        return ""
    }
}


fun String.prefix (upTo: Int) : String {
    try {
        return intern().substring(startIndex = 0, endIndex = upTo)
    } catch (e: Exception){
        Log.d("Ошибка в prefix", e.toString())
        return ""
    }
}

fun arrayToTimeTable (arraylist: ArrayList<DisTime>): TimeTable {
    var output = TimeTable()
    var index = 0
    for (i in 0..1){
        for (j in 0..5){
            for (k in 0..5){
                index = k + j*6 + i*36
                if (index < arraylist.size){
                    output.weeks[i].Days[j].paras[k] = arraylist[index]
                } else {
                    output.weeks[i].Days[j].paras[k] = DisTime(null, null, null, null)
                }
            }
        }
    }
    return output
}

fun getActualWeek () : Int {
    val date = GregorianCalendar.getInstance(Locale.GERMANY)
    var actualWeek = date.get(Calendar.WEEK_OF_YEAR) - startWeek + additionalWeek
    return actualWeek
}


val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
val shortDaysOfWeek = arrayOf("вс", "пн", "вт", "ср", "чт", "пт", "сб")
val daysOfWeek = arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
val keys = arrayOf("+\$!ME99qQHX*?_JzxSm@","k=FPy-Jkn8fJBP@ANWd5", "7j5R_yJJg=2Z*fd=F=#R", "U?q\$=sed^XX^S2w!7sj9", "y4q&dy!qLEaM+%9fFJnn", "-W+w9w%VwfgZTHPXj2hu", "L_cH@4j+^R-&nw@AcnTy")

open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {


        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }


    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}
}

class timeTableFragment : Fragment() {

    var areAllActive: Boolean = false
    private var tt = ArrayList<Lesson>()
    private var adapterToUpdate: TTadapter? = null
    var autoUpdate = false

    fun filterWeek(tt: ArrayList<Lesson>, week: Int) : ArrayList<Lesson>{
        var output = ArrayList<Lesson>()
        for (lesson in tt){
            if (lesson.weeks.contains(week)){
                output.add(lesson)
            } else if (lesson.weeks.contains(-2) && week%2 == 0){
                output.add(lesson)
            } else if (lesson.weeks.contains(-1) && week%2 == 1){
                output.add(lesson)
            }
        }
        return output
    }

    fun filterDay(tt: ArrayList<Lesson>, day: Int): ArrayList<Lesson>{
        var output = ArrayList<Lesson>()
        for (lesson in tt){
            if (lesson.day == day){
                output.add(lesson)
            }
        }
        output.sortBy { timeFromString(it.startTime) }
        return output
    }

    fun getTTForView(tt: ArrayList<Lesson>, week: Int): ArrayList<Lesson>{
        var weeks = filterWeek(tt, week)
        var output = ArrayList<Lesson>()
        for (i in 0..6){
            val day = filterDay(weeks, i)
            if (day.size > 0){
                var spacerLesson = emptyLesson()
                spacerLesson.name = keys[i]
                output.add(spacerLesson)
                output.addAll(day)
            }
        }
        return output
    }

    fun getNowWeek(): Int{
        val date = Calendar.getInstance(Locale.GERMANY)
        return date.get(Calendar.WEEK_OF_YEAR) - startWeek + additionalWeek
    }

    fun getNowDayWeek(): Int {
        val date = Calendar.getInstance(Locale.GERMANY)
        return date.get(Calendar.DAY_OF_WEEK)
    }

    fun getEstTime(): Int {
        val date = Calendar.getInstance(Locale.GERMANY)
        return date.get(Calendar.HOUR)*100 + date.get(Calendar.MINUTE)
    }

    override fun onResume() {
        super.onResume()
        if (adapterToUpdate != null) { 
            adapterToUpdate!!.areAllActive = getDefaults("areAllActive", requireContext()) ?: 0 == 1
            adapterToUpdate!!.notifyDataSetChanged()
            autoUpdate = getDefaults("isUpdating", context) ?: 0 == 1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val days : Array<String> = arrayOf(getString(R.string.Понедельник), getString(R.string.Вторник), getString(R.string.Среда), getString(R.string.Четверг), getString(R.string.Пятница), getString(R.string.Суббота))
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_time_table, container, false)

        //setup
        requireContext().setTheme(R.style.AppTheme)
        updateLastID(activity as MainActivity?, requireContext())
        val mode = getDefaults("mode", requireContext())
        if (mode == -1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (mode == 0){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val pullToRefresh = root.findViewById<SwipeRefreshLayout>(R.id.pullToRefreshTimeTable)
        val list : ListView = root.findViewById(R.id.timeTableList)
        val prevWeek: ImageButton = root.findViewById(R.id.prevWeekButton)
        val nextWeek: ImageButton = root.findViewById(R.id.nextWeekButton)
        val weekLabel: TextView = root.findViewById(R.id.weekLabel)
        val calendarButton: ImageButton = root.findViewById(R.id.calendarButton)
        val goToSettingsButton: ImageButton = root.findViewById(R.id.newSettingsButtonFromTT)

        val userDefaults = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root
        var i = 0
        val entered = userDefaults.getBoolean("entered", false)
        val group = userDefaults.getString("group", "") ?: ""
        if (!entered){
            Toast.makeText(context, "Вход не выполнен", Toast.LENGTH_LONG).show()
        }


        var nowWeek = getNowWeek()

        areAllActive = getDefaults("areAllActive", context) ?: 0 == 1

        val adapter = TTadapter(requireContext(), tt, nowWeek, areAllActive)
        list.adapter = adapter
        adapterToUpdate = list.adapter as TTadapter
        val db = DBHelper(requireContext(), null)

        fun updateScreen(){
            if (nowWeek <= 0){
                nowWeek = 0
                prevWeek.isClickable = false
                prevWeek.alpha = 0.5F
            } else {
                prevWeek.isClickable = true
                prevWeek.alpha = 1F
            }

            val boldStart = if (getActualWeek() == nowWeek) "<b>" else ""
            val boldEnd = if (getActualWeek() == nowWeek) "</b>" else ""

            val colorTag = if (nowWeek % 2 == 0) "<font color = #0072bc>" else "<font color = #9e280e>"

            val tempWeekLabel = "$colorTag$boldStart$nowWeek неделя $boldEnd</font>"

            weekLabel.setText(HtmlCompat.fromHtml(tempWeekLabel, HtmlCompat.FROM_HTML_MODE_LEGACY))
            adapter.tt = getTTForView(tt, nowWeek)
            adapter.week = nowWeek
            adapter.notifyDataSetChanged()
        }

        fun updateWeek(){
            nowWeek = getNowWeek()
            updateScreen()
        }

        updateWeek()

        fun loadLocalTT(){
            thread {
                tt = db.loadTTfromSQLite()
                this.activity?.runOnUiThread {
                    adapter.tt = getTTForView(tt, nowWeek)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        fun updateTT(){
            if (!isAdded) {return}
            thread {
                autoUpdate = getDefaults("isUpdating", context) ?: 0 == 1
                val calSync = getDefaults("calSync", context) ?: 0 == 1
                if (autoUpdate){
                    thread {
                        downloadTT(group, requireContext(), fun(lessons){
                            if (lessons.size != 0){
                                tt = lessons
                                db.saveTT(lessons)
                                if (calSync && isAdded){
                                    addTTtoCalendar(requireActivity(), tt)
                                }
                                this.activity?.runOnUiThread{
                                    adapter.tt = getTTForView(tt, nowWeek)
                                    adapter.notifyDataSetChanged()
                                    pullToRefresh.isRefreshing = false
                                }
                            } else {
                                this.activity?.runOnUiThread{
                                    Toast.makeText(requireContext(), "Расписание не было загружено", Toast.LENGTH_SHORT).show()
                                    adapter.tt = getTTForView(tt, nowWeek)
                                    adapter.notifyDataSetChanged()
                                    pullToRefresh.isRefreshing = false
                                }
                            }
                        })
                    }
                } else {
                    this.activity?.runOnUiThread{
                        adapter.tt = getTTForView(tt, nowWeek)
                        adapter.notifyDataSetChanged()
                        pullToRefresh.isRefreshing = false
                    }
                    if (calSync && isAdded){
                        addTTtoCalendar(requireActivity(), tt)
                    }
                }
            }
        }

        nextWeek.setOnClickListener {
            nowWeek += 1
            updateScreen()
        }

        prevWeek.setOnClickListener {
            nowWeek -= 1
            updateScreen()
        }

        loadLocalTT()
        thread {
            while (!isAdded) {
                Thread.sleep(2000)
            }
            updateTT()
        }

        pullToRefresh.setProgressViewOffset(false, 80, 150)

        pullToRefresh.setOnRefreshListener {
            loadLocalTT()
            updateTT()
        }

        calendarButton.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                weekLabel.setText("" + dayOfMonth + " " + monthOfYear.toString() + ", " + year)
                val moment = Calendar.getInstance(Locale.GERMANY)
                moment.set(year, monthOfYear, dayOfMonth)

                nowWeek = moment.get(Calendar.WEEK_OF_YEAR) - startWeek + additionalWeek
                if (nowWeek < 0){
                    Toast.makeText(requireContext(), "Выбрана неделя до начала семестра, вы точно этого хотите?", Toast.LENGTH_LONG).show()
                }
                updateScreen()

            }, year, month, day)

            dpd.show()
        }

        goToSettingsButton.setOnClickListener {
            val intent = Intent(requireContext(), CodeActivity::class.java)
            intent.putExtra("entered", userDefaults.getBoolean("entered", false))
            intent.putExtra("group", userDefaults.getString("group", "").toString())
            requireContext().startActivity(intent)
        }

        return root
    }

    private class TTadapter (context : Context, data: ArrayList<Lesson>, week: Int, areAllActive: Boolean): BaseAdapter() {

        public val mContext: Context
        public var tt: ArrayList<Lesson>
        public var week: Int
        public var areAllActive: Boolean
        var actualLesson = -1

        init {
            mContext = context
            tt = data
            this.week = week
            this.areAllActive = areAllActive
        }


        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            updateActualLesson()
        }

        fun updateActualLesson(){
            val date = GregorianCalendar.getInstance(Locale.GERMANY)
            val nowTime = date.get(Calendar.HOUR_OF_DAY)*100 + date.get(Calendar.MINUTE)
            var nowWeek = date.get(Calendar.WEEK_OF_YEAR) - startWeek + additionalWeek
            var day = date.get(Calendar.DAY_OF_WEEK)

            if (day == 1) {
                day == 7
                nowWeek -= 1
                nowWeek -= 1
            }
            else {day -= 1}

            if (nowWeek != week) {
                actualLesson = -1
                return
            }
            day -= 1
            var startIndex = -1
            print(day.toString())
            print(nowTime.toString())
            print(day.toString())
            for (lesson in tt){
                if (lesson.name == keys[day]){
                    startIndex = tt.indexOf(lesson)
                } else if (startIndex != -1){
                    if (timeFromString(lesson.stopTime) > nowTime){
                        actualLesson = tt.indexOf(lesson)
                        return
                    }
                    if (keys.contains(lesson.name)){
                        actualLesson = tt.indexOf(lesson) - 1
                        return
                    }
                }
            }
            if (actualLesson == -1){
                actualLesson = tt.size - 1
            }
        }


        override fun getCount () : Int {
            return tt.size
        }

        override fun getItemId(position: Int) : Long {
            return position.toLong()
        }

        override fun getItem (position: Int) : Any {
            return "Test String"
        }

        override fun isEnabled(position: Int): Boolean {
            return !keys.contains(tt[position].name)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(mContext)
            val cellData = tt[position]
            if (keys.contains(cellData.name)){
                val cell : View = inflater.inflate(R.layout.day_separator, parent, false)
                var text = cell.findViewById<TextView>(R.id.daySeparator)
                val index = keys.indexOf(cellData.name)
                text.text = daysOfWeek[index]
                cell.isClickable = false
                return cell
            }
            else if (position == actualLesson || areAllActive){
                val cell : View = inflater.inflate(R.layout.new_editor_cell_dbtt, parent, false)
                var upperLabel = cell.findViewById<TextView>(R.id.upperLabelEditor)
                var nameLabel = cell.findViewById<TextView>(R.id.lessonNameLabelEditor)
                var lowerLabel = cell.findViewById<TextView>(R.id.lowerLabelEditor)
                var startTime = cell.findViewById<TextView>(R.id.startTimeLabelEditor)
                var stopTime = cell.findViewById<TextView>(R.id.stopTimeLabelEditor)
                val card = cell.findViewById<CardView>(R.id.LessonCard)

                startTime.text = tt[position].startTime
                stopTime.text = tt[position].stopTime
                nameLabel.text = tt[position].name

                val i = position
                var tempUpperLabel: String = ""

                if (tt[position].rooms == arrayListOf("")){
                    tt[position].rooms = ArrayList<String>()
                }

                if (tt[i].weeks.contains(-2)) {
                    tempUpperLabel += "<font color = #0072bc>ЧН</font>"
                    if (tt[i].weeks.contains(-1)) {
                        tempUpperLabel += " + "
                    }
                }
                if (tt[i].weeks.contains(-1)){
                    tempUpperLabel += "<font color = #9e280e>НЧ</font>"
                }
                var added = false
                var tempweeks = ""
                for (week in tt[i].weeks){
                    if (week != -1 && week != -2){
                        added = true
                        tempweeks += week.toString() + ", "
                    }
                }
                if (added){
                    tempweeks = tempweeks.dropLast(2)
                    if (tt[i].weeks.contains(-1) || tt[i].weeks.contains(-2)){
                        tempUpperLabel += ", "
                    }
                    tempUpperLabel += tempweeks
                }

                if (tt[i].type != "") {
                    tempUpperLabel += ", " + tt[i].type
                }
                if (tt[i].notes != ""){
                    tempUpperLabel += ", " + tt[i].notes
                }
                upperLabel.setText(HtmlCompat.fromHtml(tempUpperLabel, HtmlCompat.FROM_HTML_MODE_LEGACY))

                var tempLowerLabel: String = ""
                if (tt[i].rooms.size > 0){
                    tempLowerLabel += stringFromRooms(tt[i].rooms).replace(",", ", ")
                    if (tt[i].teacher != ""){
                        tempLowerLabel += "; " + tt[i].teacher
                    }
                } else if (tt[i].teacher != ""){
                    tempLowerLabel += tt[i].teacher
                }
                lowerLabel.text = tempLowerLabel

                card.setOnClickListener {
                    val intent = Intent(mContext, ShowALessonActivity::class.java)
                    freeLesson = tt[i]
                    mContext.startActivity(intent)
                }

                return cell
            } else {
                val cell : View = inflater.inflate(R.layout.compact_lesson_cell, parent, false)
                var nameLabel = cell.findViewById<TextView>(R.id.lessonNameLabel)
                var lowerLabel = cell.findViewById<TextView>(R.id.lowerLabel)
                var startTime = cell.findViewById<TextView>(R.id.startTimeLabel)
                var stopTime = cell.findViewById<TextView>(R.id.stopTimeLabel)
                val card = cell.findViewById<CardView>(R.id.LessonCard)


                startTime.text = tt[position].startTime
                stopTime.text = tt[position].stopTime
                nameLabel.text = tt[position].name

                var tempString = ""

                if (tt[position].rooms == arrayListOf("")){
                    tt[position].rooms = ArrayList<String>()
                }

                if (tt[position].type != ""){
                    tempString += tt[position].type
                    if (tt[position].rooms.size != 0){
                        tempString += ", "
                    }
                }


                if (tt[position].rooms.size > 0){
                    tempString += stringFromRooms(tt[position].rooms)
                }
                lowerLabel.text = tempString

                card.setOnClickListener {
                    val intent = Intent(mContext, ShowALessonActivity::class.java)
                    freeLesson = tt[position]
                    mContext.startActivity(intent)
                }

                return cell
            }
        }
    }
}
