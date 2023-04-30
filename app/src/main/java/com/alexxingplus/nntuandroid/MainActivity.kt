package com.alexxingplus.nntuandroid

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

class MainActivity : AppCompatActivity() {

    var navView: BottomNavigationView? = null
    var eventsBadge: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_notifications, R.id.navigation_dashboard, R.id.navigation_timetable, R.id.navigation_tasks, R.id.navigation_settings))
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView!!.setupWithNavController(navController)
    }

    //Функция создает View, который будет отображаться поверх нижнего бара
    private fun getBadge(): View {
        if (eventsBadge != null) {return eventsBadge!!}
        val bottomBarContainer = navView?.getChildAt(0) as BottomNavigationMenuView
        eventsBadge = LayoutInflater.from(this).inflate(R.layout.nntuapp_event_badge, bottomBarContainer, false)
        return eventsBadge!!
    }

    //Функция добавляет значение в бейдж, и либо добавляет, либо убирает этот самый бейдж
    //Бейдж обязательно удаляется, иначе выдается ошибка
    //Если значение == 0, то бейдж просто убирается и не добавляется

    fun setBadge(value: Int){
        try {
            getBadge()
            val label : TextView? = eventsBadge?.findViewById(R.id.nntuapp_eventBagdeText)
            if (value < 10) {
                label?.text = value.toString()
            } else {
                label?.text = "9+"
            }
            navView?.removeView(eventsBadge)
            if (value != 0) {
                navView?.addView(eventsBadge)
            }
        } catch (e:Exception){
            Log.d("setBadge()", "Can't add view")
        }
    }
}
