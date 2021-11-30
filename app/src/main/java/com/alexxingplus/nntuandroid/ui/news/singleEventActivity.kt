package com.alexxingplus.nntuandroid.ui.news

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.widget.ImageViewCompat
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.lowerBound
import com.alexxingplus.nntuandroid.ui.prefix
import com.alexxingplus.nntuandroid.ui.suffix
import com.alexxingplus.nntuandroid.ui.upperBound
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


val titleID = 0
val imageID = 1
val descID = 2
val calID = 3
val placeID = 4
val linkID = 5

fun getColorForText(backgroundColor: Int, context: Context): Int {
    val bColorTypedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(R.attr.cardBackgroundColor, bColorTypedValue, true)

    if (backgroundColor == bColorTypedValue.data){
        val textColorTypedValue = TypedValue()
        theme.resolveAttribute(R.attr.color, textColorTypedValue, true)
        return textColorTypedValue.data
    }

    if (isColorDark(backgroundColor)){
        return Color.WHITE
    } else {
        return Color.BLACK
    }
}

class singleEventActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_single_event)
        val list: ListView = findViewById(R.id.singleEventList)

        if (freeEvent != null){
            if (freeEvent!!.author == "Новости НГТУ"){
                val url = URL(freeEvent!!.description)
                freeEvent!!.description = "Загрузка новости..."
                list.adapter = singleEventAdapter(this, freeEvent!!)
                thread {
                    val html = url.readText()
                    freeEvent!!.description = findTheArticle(html)
                    this.runOnUiThread{
                        list.adapter = singleEventAdapter(this, freeEvent!!)
                    }
                }
            } else {
                list.adapter = singleEventAdapter(this, freeEvent!!)
            }
        }
    }

    private class singleEventAdapter(activity: AppCompatActivity, event: Event) : BaseAdapter(){

        val act: AppCompatActivity
        val event: Event
        val cellIDs: ArrayList<Int>
        var lastNonLink: Int = 0
        val backgroundColor: Int

        init{
            this.act = activity
            this.event = event
            this.cellIDs = getCellIDs(event)

            val bColorTypedValue = TypedValue()
            val theme = activity.theme
            theme.resolveAttribute(R.attr.cardBackgroundColor, bColorTypedValue, true)
            backgroundColor = bColorTypedValue.data
        }

        override fun getCount(): Int {
            return cellIDs.size
        }

        override fun getItem(position: Int): Any {
            return "haha"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val id = cellIDs[position]
            val inflater = LayoutInflater.from(act)
            if (id == titleID){
                val cell = inflater.inflate(R.layout.single_event_title_cell, parent, false)

                val time : TextView = cell.findViewById(R.id.singleTimeLabel)
                val author: TextView = cell.findViewById(R.id.singleAuthorLabel)
                val title : TextView = cell.findViewById(R.id.singleTitleLabel)

                if (event.startTime != null && event.type == "event"){
                    val date = Date(event.startTime!!)
                    val formatter = SimpleDateFormat("dd MMMM HH:mm", Locale.getDefault())
                    time.text = formatter.format(date).toUpperCase()
                    Log.i("hihi", time.text.toString())
                } else {
                    time.text = "Новость".toUpperCase()
                }

                author.text = event.author.toUpperCase()
                title.text = event.title

                val bColorTypedValue = TypedValue()
                val theme = act.theme
                theme.resolveAttribute(R.attr.cardBackgroundColor, bColorTypedValue, true)

                val bcIsDark = isColorDark(bColorTypedValue.data)
                val eventIsDark = isColorDark(event.color)

                if (bcIsDark != eventIsDark){
                    time.setTextColor(event.color)
                    author.setTextColor(event.color)
                    title.setTextColor(event.color)
                }
                return cell
            } else if (id == imageID){
                val cell = inflater.inflate(R.layout.single_event_image_cell, parent, false)
                val imageView: ImageView = cell.findViewById(R.id.singleEventImageView)
                Picasso.get().load(event.imageLink).into(imageView)
                return cell
            } else if (id == descID) {
                val cell = inflater.inflate(R.layout.single_event_desc_cell, parent, false)
                val text : TextView = cell.findViewById(R.id.singleEventDescription)
                text.text = event.description
                return cell
            } else if (id == calID) {
                val cell = inflater.inflate(R.layout.single_event_add_to_calendar_cell, parent, false)
                val card: CardView = cell.findViewById(R.id.eventCalendarCard)
                val label: TextView = cell.findViewById(R.id.eventCalendarLabel)
                val icon: ImageView = cell.findViewById(R.id.eventCalendarIcon)

                val color = event.color
                val textColor = getColorForText(color, act)

                card.setCardBackgroundColor(color)
                label.setTextColor(textColor)
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(textColor))

                card.setOnClickListener {
                    val intent = Intent(Intent.ACTION_EDIT)
                    intent.setType("vnd.android.cursor.item/event")
                    intent.putExtra(CalendarContract.Events.TITLE, event.title)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTime)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.stopTime)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                    if (event.links.size > 0){
                        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.links[0].second)
                    }
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.place)
                    act.startActivity(intent)
                }
                return cell
            } else if (id == placeID){
                val cell = inflater.inflate(R.layout.single_event_room_cell, parent, false)
                val room: TextView = cell.findViewById(R.id.singleEventRoom)
                val card: CardView = cell.findViewById(R.id.eventPlaceCard)
                val icon: ImageView = cell.findViewById(R.id.eventPlaceIcon)
                val desc: TextView = cell.findViewById(R.id.eventPlaceDesc)

                val id = act.resources.getIdentifier("b${event.place}", "mipmap", act.packageName)

                val color = if (id != 0) event.color else backgroundColor


                card.setCardBackgroundColor(color)
                val textColor = getColorForText(event.color, act)
                room.setTextColor(textColor)
                desc.setTextColor(textColor)
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(textColor))

                room.text = event.place

                if (id != 0){
                    desc.text = "Аудитория"
                    card.setOnClickListener {
                        val intent = Intent(act, MainActivity::class.java)
                        intent.putExtra("room", event.place)
                        act.startActivity(intent)
                    }
                } else {
                    desc.text = "Место"
                }
                return cell
            } else { //linkID
                val cell = inflater.inflate(R.layout.single_event_link_cell, parent, false)
                val link: TextView = cell.findViewById(R.id.singleLink)
                val desc: TextView = cell.findViewById(R.id.singleLinkDescription)
                val card: CardView = cell.findViewById(R.id.eventLinkCard)
                val icon: ImageView = cell.findViewById(R.id.eventLinkIcon)

                val color = event.color
                val textColor = getColorForText(color, act)

                card.setCardBackgroundColor(color)
                link.setTextColor(textColor)
                desc.setTextColor(textColor)
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(textColor))

                val data = event.links[position - lastNonLink]
                desc.text = data.first
                link.text = data.second

                card.setOnClickListener {
                    try{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.second))
                        act.startActivity(intent)
                    } catch (e: Exception){
                        android.widget.Toast.makeText(act, "Произошла ошибка при загрузке", Toast.LENGTH_LONG).show()
                    }
                }

                return cell
            }
        }

        fun getCellIDs(event: Event): ArrayList<Int>{
            val output = ArrayList<Int>()
            output.add(titleID)
            if (event.imageLink != null){output.add(imageID)}
            output.add(descID)
            if (event.startTime != null && event.stopTime != null){output.add(calID)}
            if (event.place != null && event.place != "") {output.add(placeID)}
            lastNonLink = output.size
            for (i in 0 until event.links.size){
                output.add(linkID)
            }
            return output
        }
    }
}