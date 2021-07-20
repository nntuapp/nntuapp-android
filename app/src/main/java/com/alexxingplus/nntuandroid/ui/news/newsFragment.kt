package com.alexxingplus.nntuandroid.ui.news

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
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.ArticleActivity
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.net.URL
import kotlin.concurrent.thread


class NotificationsFragment : Fragment() {

    public class article(
        var preview: String?,
        var zag: String?,
        var href: String?,
        var text: String?,
        var hqimage: String?
    ) {

    }


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

    private fun getLatestNews() : String {
        //Эта функция должна вызываться в другом thread'e
        val urlString = "https://www.nntu.ru/news/all/vse-novosti"
        val url = URL(urlString)
        var webContent = String()
        try {
            webContent = url.readText()
        } catch (e: Exception) {
            Log.d("Все новости",  e.toString())
        }
        return webContent
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
//            Log.i("Заголовок", output[i].zag.toString())
//            Log.i("href", output[i].href.toString())
//            Log.i("Preview", output[i].preview.toString())
//            Log.i("Статья", output[i].text.toString())
//            Log.i("HQ Image", output[i].hqimage.toString())
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


    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        val pullToRefresh = root.findViewById<SwipeRefreshLayout>(R.id.pullToRefreshNews)
        val newsView = root.findViewById<ListView>(R.id.newsList)
        newsView.adapter = MyCustomAdapter(requireContext())


        thread {
            var newsString = getLatestNews()
            var tempNews = scrapeNews(newsString)
            this.activity?.runOnUiThread {
                var adapter = newsView.adapter as MyCustomAdapter
                adapter.news = tempNews
                adapter.notifyDataSetChanged()
            }
        }

        pullToRefresh.setOnRefreshListener {
            thread {
                var newsString = getLatestNews()
                var tempNews = scrapeNews(newsString)
                this.activity?.runOnUiThread {
                    var adapter = newsView.adapter as MyCustomAdapter
                    adapter.news = tempNews
                    adapter.notifyDataSetChanged()
                    pullToRefresh.isRefreshing = false
                }
            }
        }


//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    private class MyCustomAdapter (context : Context): BaseAdapter() {

        private val mContext : Context
        public var news = ArrayList<article>()

        init {
            mContext = context
        }

        override fun getCount () : Int {
            if (news.size != 0) return news.size
            return 8
        }

        override fun getItemId(position: Int) : Long {
            return position.toLong()
        }

        override fun getItem (position: Int) : Any {
            return "Test String"
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val cellRecource = R.layout.fragment_cell
            val news_cell : View = layoutInflater.inflate(cellRecource, parent, false)
            val zag = news_cell.findViewById<TextView>(R.id.newsZag)
            var img = news_cell.findViewById<ImageView>(R.id.newsImage)
            val card : CardView = news_cell.findViewById(R.id.cardView)




            if (news.count() >= position + 1){
                zag.text = news[position].zag
                Picasso.get().load(news[position].preview).into(img)
                card.setOnClickListener{
                    Log.i("Карточка работает", position.toString())
                    val openArticle : Intent = Intent(mContext, ArticleActivity::class.java)
                    openArticle.putExtra("zag", news[position].zag)
                    openArticle.putExtra("text", news[position].text)
                    openArticle.putExtra("image", news[position].preview)
                    openArticle.putExtra("href", news[position].href)
                    this.mContext.startActivity(openArticle)
                }
            }

            return news_cell
        }
    }




}
