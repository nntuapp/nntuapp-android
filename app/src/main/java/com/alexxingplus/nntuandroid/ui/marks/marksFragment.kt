package com.alexxingplus.nntuandroid.ui.marks

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
    val result : String?)
{
    fun hasResult(): Boolean {
        return fkn != null || skn != null || result != null
    }
}

class Sem (
    var Marks: ArrayList<Mark> = ArrayList<Mark>(),
    var number: Int = 0)
{
    fun hasMarks(): Boolean {
        for (mark in Marks) {
            if (mark.hasResult()) {return true}
        }
        return false
    }

    fun isEmpty(): Boolean{
        return this.Marks.isEmpty()
    }
}

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

fun checkMarksConsistency(data: ArrayList<Sem>) : Boolean {
    var prevSemNumber = 0
    var prevSemHasMarks = true
    for (sem in data){
        if (prevSemNumber + 1 != sem.number) return false
        if (!prevSemHasMarks && sem.hasMarks()) return false

        prevSemNumber = sem.number
        prevSemHasMarks = sem.hasMarks()
    }
    return true
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
        val semestr = " семестр"


        var sems = ArrayList<String>()
        var semNumbers = ArrayList<Int>()
        while (webContent.contains(semestr)){
//            var stopSymbol = webContent.upperBound(semestr) - 1
//            sems.add(webContent.prefix(stopSymbol))
//            webContent = webContent.suffix(stopSymbol)

            var numberSymbol = webContent.lowerBound(semestr) - 1
            while (webContent[numberSymbol].isDigit()) {
                numberSymbol -= 1
            }
            numberSymbol += 1

            semNumbers.add(webContent.substring(numberSymbol..webContent.lowerBound(semestr) - 1).toInt())
            sems.add(webContent.prefix(webContent.upperBound(semestr)))
            webContent = webContent.suffix(webContent.upperBound(semestr))
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
            tempSem.number = semNumbers[i]
            if (!tempSem.isEmpty()) {
                Sems.add(tempSem)
            }
            tempSem = Sem()
        }

//        for (i in 0..sems.count()-1){
//            output.add(Mark(disName = "${i+1} семестр ", fkn = null, propFkn = null, skn = null, propSkn = null, type = "АХАХХАХАХАХХАХ", result = null))
//            for (j in 0..Sems[i].Marks.count()-1){
//                output.add(Sems[i].Marks[j])
//            }
//        }

//        for (i in 0..output.count()-1){
////            Log.i("Название", output[i].disName.toString())
////            Log.i("1кн", output[i].fkn.toString())
////            Log.i("1кн пропущено", output[i].propFkn.toString())
////            Log.i("2кн", output[i].skn.toString())
////            Log.i("2кн пропущено", output[i].propSkn.toString())
////            Log.i("Тип", output[i].type.toString())
////            Log.i("Результат", output[i].result.toString())
//            Log.i(output[i].disName.toString(), output[i].type.toString())
//        }

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
            if (sems.size > 0){
                semLabel.text = (sems[nowSem].number).toString() + " семестр"
            } else {
                semLabel.text = "1 семестр"
            }

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
                if (emptySemester) {
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
            intent.putExtra("consistent", checkMarksConsistency(sems))
            requireContext().startActivity(intent)
        }

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


    override fun getCount () : Int {
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
    }
}