package com.alexxingplus.nntuandroid.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.AverageMarkActivity
import com.alexxingplus.nntuandroid.ui.OnSwipeTouchListener
import com.alexxingplus.nntuandroid.ui.SingleMarkActivity
import org.jsoup.Jsoup
import kotlin.math.roundToInt

class Mark
    (val disName : String?,
    val fkn : String?,
    val propFkn : String?,
    val skn : String?,
    val propSkn : String?,
    val type : String?,
    val result : String?){}

class Sem (
    var Marks: ArrayList<Mark> = ArrayList<Mark>()){}

fun String.intOrString(): Int{
    val v = toIntOrNull()
    return when(v){
        null -> 0
        else -> v
    }
}


fun makeADiploma(marks: ArrayList<Sem>): HashMap<String, Int>?{
    var howManyInSemester = ArrayList<Int>()
    var subjectNames = ArrayList<String>()
    var subjectMarks = ArrayList<Double>()
    var i = 0

    for (sem in marks){
        i += 1
        for (mark in sem.Marks){
            val result = mark.result?: ""
            val intResult: Int = result.intOrString()
            if (intResult > 0){
                if (subjectNames.contains(mark.disName)){
                    val index = subjectNames.indexOf(mark.disName ?: "")
                    howManyInSemester[index] += 1
                    subjectMarks[index] += intResult.toDouble()
                } else if (mark.disName != null && mark.disName != ""){
                    howManyInSemester.add(1)
                    subjectNames.add(mark.disName!!)
                    subjectMarks.add(intResult.toDouble())
                }
            }
        }
    }
    if (subjectMarks.size == 0) {return null}

    var output = HashMap<String, Int>()
    for (i in 0 until subjectMarks.size){
        if (subjectNames[i] == "Алгебра и геометрия"){
            print("Нашлось")
        }
        if (howManyInSemester[i] > 1){
            subjectMarks[i] = subjectMarks[i]/howManyInSemester[i]
        }
        output[subjectNames[i]] = subjectMarks[i].roundToInt()
    }
    return output
}

fun getAverageOverall(diploma: HashMap<String, Int>?): Double {
    if (diploma == null) {return 0.0}
    var howMany = 0
    var sum: Double = 0.0
    for ((_, mark) in diploma){
        howMany += 1
        sum += mark
    }
    return Math.round(sum*100/howMany)/100.toDouble()
}

fun averagePerSem(marks: ArrayList<Sem>): ArrayList<Double>{
    var i = 0
    var output = ArrayList<Double>()
    for (sem in marks){
        i += 1
        var semSum: Double = 0.0
        var howManySubj = 0

        for (mark in sem.Marks){
            val result = mark.result ?: ""
            val intResult = result.intOrString()
            if (intResult > 0){
                semSum += intResult
                howManySubj += 1
            }
        }

        output.add(Math.round(semSum*100/howManySubj)/100.toDouble())
    }
    return output
}


class DashboardFragment : Fragment() {

    fun String.upperBound (input: String) : Int {
        return intern().indexOf(input) + input.length
    }

    fun String.lowerBound (input: String) : Int {
        return intern().indexOf(input)
    }

    fun String.suffix (from: Int) : String {
        return intern().substring(startIndex = from, endIndex = intern().length)
    }

    fun String.prefix (upTo: Int) : String {
        return intern().substring(startIndex = 0, endIndex = upTo)
    }

    fun String.clean () : String {
        var output = intern()
        while (output.last().toString() == " "){
            output = output.dropLast(1)
        }
        while (output.first().toString() == " "){
            output = output.substring(1)
        }
        return output
    }

    fun getMarks (html: String) : ArrayList<Sem> {
        var output = ArrayList<Mark>()
        var Sems = ArrayList<Sem>()
        var tempSem = Sem()
        var webContent = html
        if (webContent.contains("найден") || webContent == "") {
            val text = getString(R.string.Не_удалось)
            val tempMark = Mark(disName = text, fkn = null, propFkn = null, skn = null, propSkn = null, type = null, result = null)
            val tempSem = Sem(arrayListOf(tempMark))
            return arrayListOf(tempSem)
        }

        //стартовые и стоповые слова
        val stopWord = """
            </td>
        """.trimIndent()
        val nameStartWord = """
            td width="300px">
        """.trimIndent()
        val disStopWord = """
            </tr>
        """.trimIndent()
        val fknStartWord = """
            mark_1_k_n_exists">
        """.trimIndent()
        val sknStartWord = """
            mark_2_k_n_exists">
        """.trimIndent()
        val propStartWord = """
            ="text-center ">
        """.trimIndent()
        val markStartWord = """
            text-center mark_exists">
        """.trimIndent()
        val typeStartWord = """
            ="text-center">
        """.trimIndent()
        val semestr = "семестр"


        var sems = ArrayList<String>()
        while (webContent.contains(semestr) == true){
            var stopSymbol = webContent.upperBound(semestr) - 1
            sems.add(webContent.prefix(stopSymbol))
            webContent = webContent.suffix(stopSymbol)
        }
        sems.add(webContent)
        sems.removeAt(0)
        for (i in 0..sems.count()-1){
            var semContent = sems[i]
            var prevSemContent = semContent
            while (prevSemContent.contains(nameStartWord) == true){
                var name = String()
                var type = String()
                var Fkn : String? = null
                var Skn : String? = null
                var PFkn : String? = null
                var PSkn : String? = null
                var res : String? = null

                semContent = prevSemContent

                //ищем начало дисциплины
                val nameStartIndex = semContent.upperBound(nameStartWord)
                semContent = semContent.suffix(nameStartIndex)
                val nameStopIndex = semContent.lowerBound(stopWord)
                name = semContent.prefix(nameStopIndex)
                semContent = semContent.suffix(nameStopIndex)

                //сохраняем все дисциплины, потому что дальше будем отделять одну от других
                prevSemContent = semContent

                //отделяем дисциплину
                val disStopIndex = semContent.upperBound(disStopWord)
                semContent = semContent.prefix(disStopIndex)

                //Первая и вторая кн, внутри них есть пропущенные часы. Вторая кн смотрится только при наличии первой
                if (semContent.contains(fknStartWord)){
                    //1 кн оценка
                    val fknStartIndex = semContent.upperBound(fknStartWord)
                    semContent = semContent.suffix(fknStartIndex)
                    val fknStopIndex = semContent.lowerBound(stopWord)
                    Fkn = semContent.prefix(fknStopIndex)
                    semContent = semContent.suffix(fknStopIndex)

                    //1кн пропущенные
                    val pFknStartIndex = semContent.upperBound(propStartWord)
                    semContent = semContent.suffix(pFknStartIndex)
                    val pFknStopIndex = semContent.lowerBound(stopWord)
                    PFkn = semContent.prefix(pFknStopIndex)
                    semContent = semContent.suffix(pFknStopIndex)

                    //смотрим 2 кн
                    if (semContent.contains(sknStartWord)){
                        val sknStartIndex = semContent.upperBound(sknStartWord)
                        semContent = semContent.suffix(sknStartIndex)
                        val sknStopIndex = semContent.lowerBound(stopWord)
                        Skn = semContent.prefix(sknStopIndex)
                        semContent.suffix(sknStopIndex)

                        //2кн пропущенные
                        val pSknStartIndex = semContent.upperBound(propStartWord)
                        semContent = semContent.suffix(pSknStartIndex)
                        val pSknStopIndex = semContent.lowerBound(stopWord)
                        PSkn = semContent.prefix(pSknStopIndex)
                        semContent = semContent.suffix(pSknStopIndex)
                    }
                }

                if (semContent.contains(markStartWord)){
                    val markStartIndex = semContent.upperBound(markStartWord)
                    semContent = semContent.suffix(markStartIndex)
                    val markStopIndex = semContent.lowerBound(stopWord)
                    res = semContent.prefix(markStopIndex)
                    semContent = semContent.suffix(markStopIndex)
                }

                type = semContent
                val doc = Jsoup.parse(type)
                type = doc.text()
                type = type.replace("<", "")

                if (type == null || type == ""){
                    type = "оценка"
                } else if (type == "з"){
                    type = "зачёт"
                } else if (type == "зо"){
                    type = "зачёт с оценкой"
                } else if (type == "эк"){
                    type = "экзамен"
                } else if (type == "о"){
                    type = "оценка"
                } else if (type == "кр"){
                    type = "курсовая работа"
                }

                if (name != "Дисциплина"){
                tempSem.Marks.add(Mark(disName = name.clean(), fkn = Fkn, propFkn = PFkn, skn = Skn, propSkn = PSkn, type = type, result = res))
                }
            }
            Sems.add(tempSem)
            tempSem = Sem()
        }

//        for (i in 0..sems.count()-1){
//            output.add(Mark(disName = "${i+1} семестр ", fkn = null, propFkn = null, skn = null, propSkn = null, type = "АХАХХАХАХАХХАХ", result = null))
//            for (j in 0..Sems[i].Marks.count()-1){
//                output.add(Sems[i].Marks[j])
//            }
//        }

        for (i in 0..output.count()-1){
//            Log.i("Название", output[i].disName.toString())
//            Log.i("1кн", output[i].fkn.toString())
//            Log.i("1кн пропущено", output[i].propFkn.toString())
//            Log.i("2кн", output[i].skn.toString())
//            Log.i("2кн пропущено", output[i].propSkn.toString())
//            Log.i("Тип", output[i].type.toString())
//            Log.i("Результат", output[i].result.toString())
            Log.i(output[i].disName.toString(), output[i].type.toString())
        }

        return Sems
    }


    private lateinit var dashboardViewModel: DashboardViewModel


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val text = getString(R.string.Не_удалось)
        val secondText = getString(R.string.Вход_не_выполнен)
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val markList = root.findViewById<ListView>(R.id.markList)
        val pullToRefresh = root.findViewById<SwipeRefreshLayout>(R.id.pullToRefreshMarks)

        val nextSemButton = root.findViewById<ImageButton>(R.id.nextSemButton)
        val prevSemButton = root.findViewById<ImageButton>(R.id.prevSemButton)
        val statsButton = root.findViewById<ImageButton>(R.id.statsButton)
        val semLabel = root.findViewById<TextView>(R.id.semLabel)
        markList.adapter = MyCustomAdapter(requireContext(), ArrayList<Mark>())
//        markList.adapter = MyCustomAdapter(requireContext(), text, secondText)

        val userDefaults = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root
        val name = userDefaults.getString("name", null).toString()
        val secondName = userDefaults.getString("secondName", null).toString()
        val otchName = userDefaults.getString("otch", "").toString()
//        val group = userDefaults.getString("group", null).toString()
        val nstud = userDefaults.getString("nstud", null).toString()
        val Entered = userDefaults.getBoolean("entered", false)
        val userType = userDefaults.getString("userType", "").toString()

        var nowSem = 0
        var sems = ArrayList<Sem>()

        fun changeButton(button: ImageButton, enabled: Boolean){
            button.isEnabled = enabled
            button.isClickable = enabled
            button.alpha = if (enabled) 1F else 0.5F
        }

        fun updateScreen(){
            changeButton(nextSemButton, true)
            changeButton(prevSemButton, true)
            changeButton(statsButton, true)
            if (sems.count() > 0){
                if (nowSem >= sems.size - 1) {
                    nowSem = sems.size - 1
                    changeButton(nextSemButton, false)
                }
                else if (nowSem <= 0) {
                    nowSem = 0
                    changeButton(prevSemButton, false)
                }
                changeButton(statsButton, true)
                val adapter = markList.adapter as MyCustomAdapter
                adapter.marks = sems[nowSem].Marks
                adapter.notifyDataSetChanged()
            } else {
                changeButton(statsButton, false)
                changeButton(nextSemButton, false)
                changeButton(prevSemButton, false)
            }
            semLabel.text = (nowSem + 1).toString() + " семестр"
        }

        fun nextSem(){
            nowSem += 1
            updateScreen()
        }

        fun prevSem(){
            nowSem -= 1
            updateScreen()
        }

        fun actualSem(){
            var i = 0
            for (sem in sems) {
                var emptySemester = true
                for (subject in sem.Marks) {
                    val emptySubject =
                        !(subject.fkn != null || subject.skn != null || subject.result != null)
                    emptySemester = emptySubject && emptySemester
                }
                if (emptySemester == true) {
                    if (i != 0) {
                        if (sem.Marks.count() > 0) {
                            nowSem = i - 1
                            break
                        }
                        emptySemester = false
                    }
                }
                if (i == sems.count() - 1 && emptySemester == false) {
                    nowSem = i
                }
                i += 1
            }
        }

        fun updateMarkInfo (){
            var output = String()
            val queue = Volley.newRequestQueue(this.context)
            val url = "https://www.nntu.ru/frontend/web/student_info.php"

            val request = object : StringRequest(com.android.volley.Request.Method.POST, url, Response.Listener<String> { response ->
                output = response
                Log.d("html", output)
                sems = getMarks(output)
                this.activity?.runOnUiThread {
                    var adapter = markList.adapter as MyCustomAdapter
                    if (Entered){
                        adapter.marks = sems[nowSem].Marks
                        adapter.notifyDataSetChanged()
                        actualSem()
                        updateScreen()
                    } else {
                        Toast.makeText(context, getString(R.string.Вход_не_выполнен), Toast.LENGTH_LONG).show()
                    }
                }
                pullToRefresh.isRefreshing = false
            }, Response.ErrorListener {error ->
                Log.d("Оно не сработало", "$error")
                Toast.makeText(context, "Проблемы с подключением к интернету", Toast.LENGTH_LONG).show()} )
            {
                @Throws(AuthFailureError::class)
                override fun getParams(): MutableMap<String, String> {
                    val params2 = HashMap<String, String>()
                    params2["last_name"] = secondName
                    params2["first_name"] = name
                    params2["otc"] = otchName
                    params2["n_zach"] = nstud
                    params2["learn_type"] = userType
                    return params2
                }
            }
            queue.add(request)
        }



        nextSemButton.setOnClickListener {
            nextSem()
        }
        prevSemButton.setOnClickListener {
            prevSem()
        }

        updateScreen()

        pullToRefresh.setProgressViewOffset(false, 80, 150)
        pullToRefresh.isRefreshing = true
        updateMarkInfo()


        pullToRefresh.setOnRefreshListener {
            updateMarkInfo()
        }

        statsButton.setOnClickListener {
            val intent = Intent(requireContext(), AverageMarkActivity::class.java)
            val diploma = makeADiploma(sems)
            val averageOverall = getAverageOverall(diploma)
            val averageSems = averagePerSem(sems)
            intent.putExtra("diploma", diploma)
            intent.putExtra("averageOverall", averageOverall)
            intent.putExtra("averageSems", averageSems)
            requireContext().startActivity(intent)
        }


//        val viewForSwipes = root.findViewById<View>(R.id.markList)
//        val swiper = object : OnSwipeTouchListener(requireActivity()){
//            override fun onSwipeLeft() {
//                super.onSwipeLeft()
////                val adapter = markList.adapter as MyCustomAdapter
////                if (adapter.marks.count() > 0){
////                    adapter.increaseNowSem()
////                    adapter.notifyDataSetChanged()
////                }
//                nextSem()
//            }
//
//            override fun onSwipeRight() {
//                super.onSwipeRight()
////                val adapter = markList.adapter as MyCustomAdapter
////                if (adapter.marks.count() > 0){
////                    adapter.decreaseNowSem()
////                    adapter.notifyDataSetChanged()
////                }
//                prevSem()
//            }
//        }
//
//        viewForSwipes.setOnTouchListener(swiper)

        return root
    }


}

private class MyCustomAdapter (context : Context, marks : ArrayList<Mark>): BaseAdapter() {

    private val mContext : Context
    public var marks: ArrayList<Mark>

    init {
        mContext = context
        this.marks = marks
    }

//    public fun updateSem(){
//        if (nowSem == -1){
//            if (marks.count() > 0){
//                maxSem = marks.count() - 1
//            }
//            for (i in 0..marks.count() - 1){
//                var emptySemester = true
//                for (j in 0..marks[i].Marks.count() - 1){
//                    val subject = marks[i].Marks[j]
//                    val emptySubject = !(subject.fkn != null || subject.skn != null || subject.result != null)
//                    emptySemester = emptySubject && emptySemester
//                }
//                if (emptySemester == true){
//                    if (i != 0) {
//                        if (marks[i - 1].Marks.count() != 0) {
//                            nowSem = i - 1
//                            break
//                        }
//                        emptySemester = false
//                    }
//                }
//                if (i == marks.count() - 1 && emptySemester == false){
//                    nowSem = i
//                }
//            }
//        }
//
//        if (nowSem > maxSem) {nowSem = maxSem}
//        else if (nowSem < minSem) {nowSem = minSem}
//    }

    override fun getCount () : Int {
//        if (marks.count() > 0 && nowSem >= 0){
//            return marks[nowSem].Marks.count() + 1
//        }
//        return 1
        return marks.count()
    }

    override fun getItemId(position: Int) : Long {
        return position.toLong()
    }

    override fun getItem (position: Int) : Any {
        return "Test String"
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val cellRecource = R.layout.mark_cell
        val mark_cell : View = layoutInflater.inflate(cellRecource, parent, false)
        val disNameLabel : TextView = mark_cell.findViewById(R.id.disNameLabel)
        val typeNameLabel : TextView = mark_cell.findViewById(R.id.typeNameLabel)
        val resultLabel : TextView = mark_cell.findViewById(R.id.resultLabel)
        val card : CardView = mark_cell.findViewById(R.id.markCard)

        disNameLabel.text = marks[position].disName
        typeNameLabel.text = marks[position].type
        resultLabel.text = "пусто"
        resultLabel.alpha = 0.3F

        if (marks[position].fkn != null){
            resultLabel.text = "1кн: ${marks[position].fkn}"
            resultLabel.alpha = 1F
        }
        if (marks[position].skn != null){
            resultLabel.text = "2кн: ${marks[position].skn}"
            resultLabel.alpha = 1F
        }
        if (marks[position].result != null){
            resultLabel.text = marks[position].result
            resultLabel.alpha = 1F
        }
        if (marks[position].type == null){
            resultLabel.alpha = 0F
        } else if (resultLabel.text != "пусто"){
            resultLabel.alpha = 1F
        }

        card.setOnClickListener{
//                Log.i("Карточка работает", position.toString())
            val openMark : Intent = Intent(mContext, SingleMarkActivity::class.java)
            openMark.putExtra("disName", marks[position].disName)
            openMark.putExtra("fkn", marks[position].fkn)
            openMark.putExtra("propFkn", marks[position].propFkn)
            openMark.putExtra("skn", marks[position].skn)
            openMark.putExtra("propSkn", marks[position].propSkn)
            openMark.putExtra("type", marks[position].type)
            openMark.putExtra("result", marks[position].result)
            this.mContext.startActivity(openMark)
        }

        return mark_cell
//        val separatorResource = R.layout.separator_cell
//        val mark_cell : View = layoutInflater.inflate(cellRecource, parent, false)
//
//        if (position == 0){
//            val separator_cell : View = layoutInflater.inflate(separatorResource, parent, false)
//            val semLabel : TextView = separator_cell.findViewById(R.id.separator_text)
//            val prevButton : ImageButton = separator_cell.findViewById(R.id.prevSemButton)
//            val nextButton : ImageButton = separator_cell.findViewById(R.id.nextSemButton)
//            val statsButton: ImageButton = separator_cell.findViewById(R.id.statsButton)
//
//            if (nowSem >= 0){
//                statsButton.isEnabled = true
//                statsButton.isClickable = true
//                nextButton.isEnabled = true
//                nextButton.isClickable = true
//                prevButton.isEnabled = true
//                prevButton.isClickable = true
//                statsButton.alpha = 1F
//                nextButton.alpha = 1F
//                prevButton.alpha = 1F
//            } else {
//                statsButton.isEnabled = false
//                statsButton.isClickable = false
//                nextButton.isEnabled = false
//                nextButton.isClickable = false
//                prevButton.isEnabled = false
//                prevButton.isClickable = false
//                statsButton.alpha = 0.5F
//                nextButton.alpha = 0.5F
//                prevButton.alpha = 0.5F
//            }
//
//            if (nowSem >= maxSem && marks.size > 0){
//                nextButton.isEnabled = false
//                nextButton.isClickable = false
//                nextButton.alpha = 0.5F
//            } else {
//                nextButton.isEnabled = true
//                nextButton.isClickable = true
//                nextButton.alpha = 1F
//            }
//            if (nowSem <= minSem && marks.size > 0){
//                prevButton.isEnabled = false
//                prevButton.isClickable = false
//                prevButton.alpha = 0.5F
//            } else {
//                prevButton.isEnabled = true
//                prevButton.isClickable = true
//                prevButton.alpha = 1F
//            }
//            semLabel.text = (nowSem + 1).toString() + " семестр"
//
//            prevButton.setOnClickListener {
////                nowSem -= 1
////                if (nowSem > maxSem) {nowSem = maxSem}
////                else if (nowSem < minSem) {nowSem = minSem}
////                updateSem()
//                decreaseNowSem()
//                this.notifyDataSetChanged()
//            }
//
//            nextButton.setOnClickListener {
////                nowSem += 1
////                if (nowSem > maxSem) {nowSem = maxSem}
////                else if (nowSem < minSem) {nowSem = minSem}
////                updateSem()
//                increaseNowSem()
//                this.notifyDataSetChanged()
//            }
//
//            statsButton.setOnClickListener {
//                val intent = Intent(mContext, AverageMarkActivity::class.java)
//                val diploma = makeADiploma(marks)
//                val averageOverall = getAverageOverall(diploma)
//                val averageSems = averagePerSem(marks)
//                intent.putExtra("diploma", diploma)
//                intent.putExtra("averageOverall", averageOverall)
//                intent.putExtra("averageSems", averageSems)
//                mContext.startActivity(intent)
//            }
//
//            return separator_cell
//        } else {
//            val disNameLabel : TextView = mark_cell.findViewById(R.id.disNameLabel)
//            val typeNameLabel : TextView = mark_cell.findViewById(R.id.typeNameLabel)
//            val resultLabel : TextView = mark_cell.findViewById(R.id.resultLabel)
//            val card : CardView = mark_cell.findViewById(R.id.markCard)
//
//            disNameLabel.text = marks[nowSem].Marks[position - 1].disName
//            typeNameLabel.text = marks[nowSem].Marks[position - 1].type
//            resultLabel.text = "пусто"
//            resultLabel.alpha = 0.3F
//
//            if (marks[nowSem].Marks[position - 1].fkn != null){
//                resultLabel.text = "1кн: ${marks[nowSem].Marks[position - 1].fkn}"
//                resultLabel.alpha = 1F
//            }
//            if (marks[nowSem].Marks[position - 1].skn != null){
//                resultLabel.text = "2кн: ${marks[nowSem].Marks[position - 1].skn}"
//                resultLabel.alpha = 1F
//            }
//            if (marks[nowSem].Marks[position - 1].result != null){
//                resultLabel.text = marks[nowSem].Marks[position - 1].result
//                resultLabel.alpha = 1F
//            }
//            if (marks[nowSem].Marks[position - 1].type == null){
//                resultLabel.alpha = 0F
//            } else if (resultLabel.text != "пусто"){
//                resultLabel.alpha = 1F
//            }
//
//
//            card.setOnClickListener{
////                Log.i("Карточка работает", position.toString())
//                val openMark : Intent = Intent(mContext, SingleMarkActivity::class.java)
//                openMark.putExtra("disName", marks[nowSem].Marks[position - 1].disName)
//                openMark.putExtra("fkn", marks[nowSem].Marks[position - 1].fkn)
//                openMark.putExtra("propFkn", marks[nowSem].Marks[position - 1].propFkn)
//                openMark.putExtra("skn", marks[nowSem].Marks[position - 1].skn)
//                openMark.putExtra("propSkn", marks[nowSem].Marks[position - 1].propSkn)
//                openMark.putExtra("type", marks[nowSem].Marks[position - 1].type)
//                openMark.putExtra("result", marks[nowSem].Marks[position - 1].result)
//                this.mContext.startActivity(openMark)
//            }
//
//
//        }





//        if (marks.count() > 0) {
//            val disNameLabel : TextView = mark_cell.findViewById(R.id.disNameLabel)
//            val typeNameLabel : TextView = mark_cell.findViewById(R.id.typeNameLabel)
//            val resultLabel : TextView = mark_cell.findViewById(R.id.resultLabel)
//            val card : CardView = mark_cell.findViewById(R.id.markCard)
//
//            disNameLabel.text = marks[position].disName
//            typeNameLabel.text = marks[position].type
//            resultLabel.text = "пусто"
//            resultLabel.alpha = 0.3F
//
//            if (marks[position].fkn != null){
//                resultLabel.text = "1кн: ${marks[position].fkn}"
//                resultLabel.alpha = 1F
//            }
//            if (marks[position].skn != null){
//                resultLabel.text = "2кн: ${marks[position].skn}"
//            }
//            if (marks[position].result != null){
//                resultLabel.text = marks[position].result
//                resultLabel.alpha = 1F
//            }
//            if (marks[position].disName.toString().contains(" семестр ") || marks[position].disName.toString() == oneMoreError || marks[position].disName.toString() == errorText){
//                val separatorText = separator_cell.findViewById<TextView>(R.id.separator_text)
//                separatorText.text = marks[position].disName
//                separator_cell.isEnabled = false
//                return separator_cell
//            }
//
//            card.setOnClickListener{
//                Log.i("Карточка работает", position.toString())
//                val openMark : Intent = Intent(mContext, SingleMarkActivity::class.java)
//                openMark.putExtra("disName", marks[position].disName)
//                openMark.putExtra("fkn", marks[position].fkn)
//                openMark.putExtra("propFkn", marks[position].propFkn)
//                openMark.putExtra("skn", marks[position].skn)
//                openMark.putExtra("propSkn", marks[position].propSkn)
//                openMark.putExtra("type", marks[position].type)
//                openMark.putExtra("result", marks[position].result)
//                this.mContext.startActivity(openMark)
//            }
//        }

    }
//
//
//
//    public fun increaseNowSem(){
//        nowSem += 1
//        updateSem()
//    }
//
//    public fun decreaseNowSem(){
//        nowSem -= 1
//        updateSem()
//    }
}