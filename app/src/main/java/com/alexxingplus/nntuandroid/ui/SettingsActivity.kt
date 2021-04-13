package com.alexxingplus.nntuandroid.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.alexxingplus.nntuandroid.R
import java.lang.StringBuilder
import java.util.*
import kotlin.concurrent.thread

class CodeActivity : AppCompatActivity() {

    var switch: Switch? = null

    override fun onResume() {
        super.onResume()
        if (switch != null) {
            switch!!.isChecked = getDefaults("isUpdating", this) ?: 0 == 1
        }
    }

    fun getImage (c: Context, imgname: String) : Drawable {
        return c.resources.getDrawable(c.resources.getIdentifier(imgname, "drawable", c.packageName), c.theme)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_new_activity)

        val entered = intent.getBooleanExtra("entered", false)
        val group =  intent.getStringExtra("group") ?: ""

        val editorCard: CardView = findViewById(R.id.editorCard)
        val updateSwitch : Switch = findViewById(R.id.updateSwitch)
        val codeField: EditText = findViewById(R.id.codeField)
        val uploadButton: ImageButton = findViewById(R.id.uploadButton)
        val vkCard: CardView = findViewById(R.id.vkCard)
        val emailCard: CardView = findViewById(R.id.mailCard)

        val areAllActive: Switch = findViewById(R.id.areAllActiveSwitch)
        val calendarSwitch: Switch = findViewById(R.id.calendarSwitch)
        val darkButton: ImageButton = findViewById(R.id.darkModeButton)

        val calendarFooter: TextView = findViewById(R.id.smartFooter)

        val nightMode = AppCompatDelegate.getDefaultNightMode()

        if (nightMode == AppCompatDelegate.MODE_NIGHT_NO){
            val newImage : Drawable = getImage(this, "ic_sun")
            darkButton.setImageDrawable(newImage)
        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES){
            val newImage : Drawable = getImage(this, "ic_moon")
            darkButton.setImageDrawable(newImage)
        } else {
            val newImage : Drawable = getImage(this, "ic_auto")
            darkButton.setImageDrawable(newImage)
        }

        darkButton.setOnClickListener {
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            if (nightMode == AppCompatDelegate.MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setDefaults("mode", -1, this)
//                userDefaults.edit().putInt("mode", -1).apply()
            } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//                userDefaults.edit().putInt("mode", 0).apply()
                setDefaults("mode", 0, this)
                val newImage : Drawable = getImage(this, "ic_auto")
                darkButton.setImageDrawable(newImage)
                Toast.makeText(this, getString(R.string.Темная_тема), Toast.LENGTH_LONG).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val newImage : Drawable = getImage(this, "ic_sun")
                darkButton.setImageDrawable(newImage)
//                userDefaults.edit().putInt("mode", 1).apply()
                setDefaults("mode", 1, this)
            }
        }


        areAllActive.isChecked = getDefaults("areAllActive", this) ?: 0 == 1
        calendarSwitch.isChecked = getDefaults("calSync", this) ?: 0 == 1

        areAllActive.setOnCheckedChangeListener{_ , isChecked ->
            setDefaults("areAllActive", if(isChecked) 1 else -1, this)
        }

        fun deleteCalendarQuestion(){
            var dialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).create()
            dialog.setTitle("Удалить расписание из календаря?")
            dialog.setMessage("Вы отключили автоматический перенос занятий в календарь. В календаре всё ещё остаётся копия расписания. Нужно ли её удалять?")
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Оставить", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Удалить", DialogInterface.OnClickListener{ dialogInterface, i ->
                deleteACalendarWithPermission(this)
                dialogInterface.dismiss()
            })
            dialog.show()
        }

        fun updateFooter(){
            val isChecked = calendarSwitch.isChecked
            if (isChecked){
                calendarFooter.text = "Сейчас расписание переносится в календарь. Теперь вы можете планировать свои дела с учётом занятий. Также вы можете экспортировать этот календарь и загрузить куда хотите. Если вы хотите убрать расписание из календаря - выключите эту функцию."
            } else {
                calendarFooter.text = "Сейчас расписание не переносится в календарь. Если вы хотите, чтобы оно туда переносилось со всеми изменениями – включите эту функцию."
            }
        }

        calendarSwitch.setOnCheckedChangeListener{_ , isChecked ->
            setDefaults("calSync", if(isChecked) 1 else -1, this)
            if (!isChecked) {
                deleteCalendarQuestion()
            } else {
                thread {
                    val db = DBHelper(this, null)
                    val tt = db.loadTTfromSQLite()
                    addTTtoCalendar(this, tt)
                }
            }
            updateFooter()
        }

        updateFooter()


        switch = updateSwitch

        editorCard.setOnClickListener {
            val intent = Intent(this, DbttEditorActivity::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }

        vkCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/nntuapp"))
            startActivity(intent)
        }

        emailCard.setOnClickListener {
            val message = """
                Здравствуйте! Я бы хотел поменять расписание своей группы.
                Группа:
                Меня зовут:
            """.trimIndent()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_EMAIL, "nntuapp@inbox.ru")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Расписание")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(intent)
        }

        updateSwitch.setOnCheckedChangeListener{_ , isChecked ->
            setDefaults("isUpdating", if(isChecked) 1 else -1, this)
        }
        uploadButton.setOnClickListener {
            if (!entered){
                codeField.setText("")
                codeField.hint = "Вход не выполнен"
            } else {
                val input = codeField.text.toString().replace(encrypt("-"), "")
                val answer = encrypt(group.replace("-",""))
                if (input == answer){
                    codeField.hint = "Код"
                    thread {
                        val db = DBHelper(this, null)
                        val tt = db.loadTTfromSQLite()
                        uploadTT(tt, group, fun(success){
                            if (success){
                                Toast.makeText(this, "Расписание загружено", Toast.LENGTH_SHORT).show()
                                switch!!.isChecked = true
                                setDefaults("isUpdating", 1, this)
                            } else {
                                Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show()
                            }
                        }, this)
                    }
                } else if (input == ""){
                    codeField.setText("")
                    codeField.hint = "Введите код!"
                } else if (input.toLowerCase().contains("пасх")){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=G6pqAN8ALC8&t=33s"))
                    startActivity(intent)
                } else {
                    codeField.setText("")
                    codeField.hint = "Неверный код!"
                }
            }
        }




//        setContentView(R.layout.activity_code)
//        val codeTextField : EditText = findViewById(R.id.codeTextField)
//        val enterCodeButton : Button = findViewById(R.id.enterCodeButton)
//        val emailButton : Button = findViewById(R.id.emailButton)
//        val vkButton : Button = findViewById(R.id.vkButton)
//        val upperText : TextView = findViewById(R.id.upperText)
//        val infoButton : Button = findViewById(R.id.info_button)
//

//
//
//
//
//        enterCodeButton.setOnClickListener {
//            val intent = Intent(this, DbttEditorActivity::class.java)
////                    intent.putExtra("group", group)
//            startActivity(intent)
//            if (entered == false){
//                upperText.text = getString(R.string.Вход_не_выполнен)
//                upperText.setTextColor(Color.RED)
//            } else {
//                val input = codeTextField.text.toString()
//                if (encrypt(group) == input){
//                    upperText.text = getString(R.string.code1)
////                    val intent = Intent(this, DbttEditorActivity::class.java)
//////                    intent.putExtra("group", group)
////                    startActivity(intent)
//                } else if (codeTextField.text.toString() == "" ){
//                    upperText.text = getString(R.string.Введите_код_)
//                } else {
//                    if (codeTextField.text.toString().toLowerCase() == "с пасхой"){
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=G6pqAN8ALC8&t=33s"))
//                        startActivity(intent)
//                    } else {
//                        upperText.text = getString(R.string.Неверный_код)
//                    }
//                }
//            }
//        }
//
//        emailButton.setOnClickListener {
//            val message = """
//                Здравствуйте! Я бы хотел поменять расписание своей группы.
//                Группа:
//                Меня зовут:
//            """.trimIndent()
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.type = "text/plain"
//            intent.putExtra(Intent.EXTRA_EMAIL, "nntuapp@inbox.ru")
//            intent.putExtra(Intent.EXTRA_SUBJECT, "Расписание")
//            intent.putExtra(Intent.EXTRA_TEXT, message)
//            startActivity(intent)
//        }
//
//        vkButton.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/nntuapp"))
//            startActivity(intent)
//        }
//
//        infoButton.setOnClickListener {
//            val dialog = Dialog(this)
//            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            dialog.setCancelable(true)
//            dialog.setContentView(R.layout.info_popup)
//
//            val dismissButton : Button = dialog.findViewById(R.id.OKButton)
//
//            dismissButton.setOnClickListener {
//                dialog.dismiss()
//            }
//
//            dialog.show()
//        }

    }
}
