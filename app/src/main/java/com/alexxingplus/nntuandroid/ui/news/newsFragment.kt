package com.alexxingplus.nntuandroid.ui.news

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.ArticleActivity
import com.alexxingplus.nntuandroid.ui.eventsURL
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import retrofit2.http.GET
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


const val newsURL = "https://www.nntu.ru/news/all/vse-novosti"

public class article(
    var preview: String?,
    var zag: String?,
    var href: String?,
    var text: String?,
    var hqimage: String?
) {
    fun toEvent(context: Context): Event{
        val tV = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.cardBackgroundColor, tV, true)

        return Event(
            color = tV.data,
            author = "Новости НГТУ",
            type = "article",
            title = this.zag.toString(),
            description = this.href.toString(),
            startTime = null,
            stopTime = null,
            place = null,
            imageLink = this.preview,
            links = arrayListOf(android.util.Pair("Новость на сайте НГТУ", this.href.toString()))
        )
    }
}

fun isColorDark(color: Int): Boolean {
    val darkness: Double = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return if (darkness < 0.5) {
        false // It's a light color
    } else {
        true // It's a dark color
    }
}

class NotificationsFragment : Fragment() {
    fun String.upperBound (input: String) : Int {
        return intern().indexOf(input) + input.length
    }

    fun String.lowerBound (input: String) : Int {
        return intern().indexOf(input)
    }

    fun String.suffix (from: Int) : String {
        return intern().substring(startIndex = from, endIndex = intern().length-1)
    }

    fun String.prefix (upTo: Int) : String {
        return intern().substring(startIndex = 0, endIndex = upTo)
    }

    private fun getLatestNews(context: Context, success: (String) -> Unit, error: () -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val request = object: StringRequest(Method.GET, newsURL, Response.Listener<String> { str ->
            success(str)
        }, Response.ErrorListener { error ->
            Log.d("request@newLoadEvents", error.toString())
            error()
        }){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept-Encoding"] = "gzip, deflate, br"
                headers["connection"] = "keep-alive"
                headers["Accept-Language"] = "en-GB,en;q=0.9"
                headers["Accept"] = "*/*"
                headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                return headers
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var output: String? = ""
                try {
                    val gStream = GZIPInputStream(ByteArrayInputStream(response.data))
                    val reader = InputStreamReader(gStream)
                    val `in` = BufferedReader(reader)
                    var read: String?
                    while (`in`.readLine().also { read = it } != null) {
                        output += read
                    }
                    reader.close()
                    `in`.close()
                    gStream.close()
                } catch (e: IOException) {
                    return Response.error(ParseError())
                }
                return Response.success(output, HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        queue.add(request)
    }

    private fun scrapeNews(input: String) : ArrayList<article> {
        //ключевые слова html страницы
        val fpreviewStartWord = """
            <img alt=""
        """.trimIndent()
        val spreviewStartWord = """
            src="
        """.trimIndent()
        val previewStopWord = """
            "/>
        """.trimIndent()
        val fhrefStartWord = """
            <a class="item-link is-popover"
        """.trimIndent()
        val shrefStartWord = """
            href="
        """.trimIndent()
        val hrefStopWord = """
            "\n
        """.trimIndent()
        val zagStartWord = """
            data-content="">
        """.trimIndent()
        val zagStopWord = """
            </a>
        """.trimIndent()

        //сам скрейпинг
        var webContent = input
        var output : ArrayList<article> = ArrayList<article>()
        var tempArticle : article = article(null, null, null, null, null)

        while (webContent.contains(fpreviewStartWord) == true){
            val wContentStartIndex = webContent.upperBound(fpreviewStartWord)
            webContent = webContent.suffix(wContentStartIndex)
            //preview
            try {
                val previewStartIndex = webContent.upperBound(spreviewStartWord)
                webContent = webContent.suffix(previewStartIndex)
                val previewStopIndex = webContent.lowerBound(previewStopWord)
                tempArticle.preview = webContent.prefix(previewStopIndex)
                webContent = webContent.suffix(previewStopIndex)
            } catch (e: Exception) {
                Log.d("Preview не было найдено", e.toString())
            }

            //href
            try {
                val fhrefStartIndex = webContent.upperBound(fhrefStartWord)
                webContent = webContent.suffix(fhrefStartIndex)
                val shrefStartIndex = webContent.upperBound(shrefStartWord)
                webContent = webContent.suffix(shrefStartIndex)
                val hrefStopIndex = webContent.lowerBound(zagStartWord) // тут индекс = -1
//                val hrefStopIndex = webContent.lowerBound(hrefStopWord)
                tempArticle.href = webContent.prefix(hrefStopIndex)
                webContent = webContent.suffix(hrefStopIndex)
            } catch (e: Exception) {
                Log.d("href не был найден", e.toString())
            }

            //zag
            try {
                val zagStartIndex = webContent.upperBound(zagStartWord)
                webContent = webContent.suffix(zagStartIndex)
                val zagStopIndex = webContent.lowerBound(zagStopWord)
                tempArticle.zag = webContent.prefix(zagStopIndex)
                webContent = webContent.suffix(zagStopIndex)
            } catch (e: Exception) {
                Log.d("Заголовок не был найден", e.toString())
            }

            if (tempArticle.href != null){
                val a = tempArticle.href!!.lowerBound("\"")
                tempArticle.href = tempArticle.href!!.prefix(a)
                tempArticle.href = "https://www.nntu.ru" + tempArticle.href
                Log.i("Последний элемент", tempArticle.href!!.last().toString())
            }
            if (tempArticle.preview != null){
                tempArticle.preview = "https://www.nntu.ru" + tempArticle.preview
            }

//            Log.i("Заголовок", tempArticle.zag.toString())
//            Log.i("href", tempArticle.href.toString())
//            Log.i("Preview", tempArticle.preview.toString())


            output.add(tempArticle)
            tempArticle = article(null, null, null, null, null)

        }

        return output
    }

    public fun fillWithArticles (input: ArrayList<article>) : ArrayList<article> {
        var output = input
        var i = 0
        for (i in 0..7) {
            val url = URL(input[i].href)
            var webContent = String()
            try {
                webContent = url.readText()
                output[i].text = findTheArticle(webContent)
                output[i].hqimage = findHQImage(webContent)
            } catch (e: Exception){
                Log.d("Отдельные новости", e.toString())
                return output
            }
        }
        return output
    }

    public fun findTheArticle (html: String) : String {
        var output = String()
        var webContent = html

        val startWord = """
            text-align: justify;">
        """.trimIndent()
        val stopWord = """
        </p>
        """.trimIndent()

        while (webContent.contains(startWord) == true){
            try {
                val startIndex = webContent.upperBound(startWord)
                webContent = webContent.suffix(startIndex)
                val stopIndex = webContent.lowerBound(stopWord)
                output += webContent.prefix(stopIndex)
                webContent = webContent.suffix(stopIndex)
            } catch (e: Exception){
                Log.d("Поиск статьи", e.toString())
            }
        }
        val doc = Jsoup.parse(output)
        output = doc.text()
        return output
    }

    public fun findHQImage (html: String) : String {
        var output = String()
        var webContent = html

        val startWord = """
            img alt="" src="
        """.trimIndent()
        val stopWord = """
            " />
        """.trimIndent()

        try {
            val startIndex = webContent.upperBound(startWord)
            webContent = webContent.suffix(startIndex)
            val stopIndex = webContent.lowerBound(stopWord)
            output = webContent.prefix(stopIndex)
            webContent = webContent.suffix(stopIndex)
        } catch (e: Exception){
            Log.d("Поиск HQ картинки", e.toString())
        }
        return output
    }

    fun loadEventsAndArticles(context: Context, callback: (ArrayList<Event>) -> Unit){
        thread {
            getLatestNews(context, success = { html ->
                val oldNews = scrapeNews(html).map {it.toEvent(context)}
                newLoadEvents(context, success = { events ->
                    callback(ArrayList((events.sorted() + oldNews)))
                }, error = {
                    callback(ArrayList(oldNews))
                })
            }, error = {
                newLoadEvents(context, success = { events ->
                    callback(ArrayList((events.sorted())))
                }, error = {
                    callback(ArrayList())
                })
            })
        }
    }

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        var events = ArrayList<Event>()

        resetBadge(activity as MainActivity?, requireContext())

        val pullToRefresh = root.findViewById<SwipeRefreshLayout>(R.id.pullToRefreshNews)
        val newsView = root.findViewById<ListView>(R.id.newsList)
        newsView.adapter = eventsAdapter(requireContext(), ArrayList<Event>())

        fun loadEvents(){
            loadEventsAndArticles(requireContext()){ loaded ->
                events = loaded
                if(isAdded){
                    requireActivity().runOnUiThread {
                        val adapter = newsView.adapter as eventsAdapter
                        adapter.events = events
                        adapter.notifyDataSetChanged()
                        pullToRefresh.isRefreshing = false
                    }
                }
            }
        }

        if (isAdded){
            loadEvents()
        }

        pullToRefresh.isRefreshing = true
        pullToRefresh.setOnRefreshListener {
            loadEvents()
        }
        return root
    }

    private class eventsAdapter(context: Context, events: ArrayList<Event>): BaseAdapter(){

        val context: Context
        public var events: ArrayList<Event>

        init {
            this.context = context
            this.events = events
        }

        override fun getCount(): Int {
            return events.size
        }

        override fun getItem(position: Int): Any {
            return "i am event"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(context)
            val cell = inflater.inflate(R.layout.event_cell, parent, false)

            val card : CardView = cell.findViewById(R.id.eventCard)
            val time : TextView = cell.findViewById(R.id.eventTimeLabel)
            val author : TextView = cell.findViewById(R.id.eventAuthorLabel)
            val title : TextView = cell.findViewById(R.id.eventTitleLabel)
            val image : ImageView = cell.findViewById(R.id.eventImage)
            var titleBackground: LinearLayout = cell.findViewById(R.id.eventCardTitle)

            card.setOnClickListener {
                val intent = Intent(context, singleEventActivity::class.java)
                freeEvent = events[position]
                context.startActivity(intent)
            }

            if (events[position].startTime != null && events[position].type == "event"){
                val date = Date(events[position].startTime!!)
                val formatter = SimpleDateFormat("dd MMMM HH:mm", Locale.getDefault())
                time.text = formatter.format(date).toUpperCase()
            } else {
                time.text = "Новость".toUpperCase()
            }

            author.text = events[position].author.toUpperCase()
            title.text = events[position].title

            Picasso.get().load(events[position].imageLink).into(image)

            val textColor = if (isColorDark(events[position].color)) Color.WHITE else Color.BLACK
            titleBackground.setBackgroundColor(events[position].color)
            time.setTextColor(textColor)
            author.setTextColor(textColor)
            title.setTextColor(textColor)


            return cell
        }
    }
}
