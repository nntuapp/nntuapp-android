package com.alexxingplus.nntuandroid.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.alexxingplus.nntuandroid.R
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.net.URL
import kotlin.concurrent.thread

class ArticleActivity : AppCompatActivity() {

    private fun findTheArticle (html: String) : String {
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val zag = findViewById<TextView>(R.id.articleZag)
        val text = findViewById<TextView>(R.id.articleText)
        val image = findViewById<ImageView>(R.id.articleImage)

        val zagText = intent.getStringExtra("zag")
        val articleText = intent.getStringExtra("text")
        val previewHref = intent.getStringExtra("image")
        val href = intent.getStringExtra("href")

        thread {
                if (href != null){
                    val url = URL(href.toString())
                var webcontent = String()
                try {
                    webcontent = url.readText()
                } catch (e: Exception){
                    Log.d("Отдельно не загрузилась", e.toString())
                }
                this.runOnUiThread {
                    text.text = findTheArticle(webcontent)
                }
            }
        }

        if (zagText != null) {
            zag.text = zagText
        }
        if (articleText != null){
            text.text = articleText
        }
        if (articleText == ""){
            text.text = getString(R.string.Не_удалось_новость)
        }
        if (previewHref != null){
            Picasso.get().load(previewHref).into(image)

        }

    }
}
