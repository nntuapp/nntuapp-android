package com.alexxingplus.nntuandroid.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alexxingplus.nntuandroid.MoreRoomsActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.getDefaults
import com.alexxingplus.nntuandroid.ui.setDefaults
import com.jsibbold.zoomage.ZoomageView
import kotlin.concurrent.thread


class HomeFragment : Fragment() {

    private var floor : Int = 1
    private var buiding : Int = 6
    private var minFloor : Int = 0
    private var maxFloor : Int = 5
    private var FloorOrBuiding : Boolean = true //true - этаж, false - корпус

    fun getImage (c: Context, imgname: String) : Drawable {
        return c.resources.getDrawable(c.resources.getIdentifier(imgname, "mipmap", c.packageName), c.theme)
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        requireContext().setTheme(R.style.AppTheme)
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

//        val userDefaults = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root
        val mode = getDefaults("mode", requireContext())
        if (mode == -1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (mode == 0){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        //vals from xml
        val editableTextView : TextView = root.findViewById(R.id.editText)
        val searchButton : Button = root.findViewById(R.id.searchButton)
        val buidingFloor : Button = root.findViewById(R.id.buidingFloor)
        val prevButton : ImageButton = root.findViewById(R.id.prevButton)
        var nextButton : ImageButton = root.findViewById(R.id.nextButton)
        val floorText : TextView = root.findViewById(R.id.textView)
        val theImage : ZoomageView = root.findViewById(R.id.theImage)
        val moreRooms : Button = root.findViewById(R.id.moreRoomsButton)

        //funs for vals
        fun updateNumber(){
            if (FloorOrBuiding == true) {
                floorText.text = floor.toString()
            } else {
                floorText.text = buiding.toString()
            }
        }

        fun updateBuildingFloors () {
            when (this.buiding){
                1 -> {
                    maxFloor = 3
                    minFloor = 1
                }
                2 -> {
                    maxFloor = 3
                    minFloor = 1
                }
                3 -> {
                    maxFloor = 3
                    minFloor = 1
                }
                4 -> {
                    maxFloor = 4
                    minFloor = 1
                }
                5 -> {
                    maxFloor = 4
                    minFloor = 1
                }
                6 -> {
                    maxFloor = 5
                    minFloor = 0
                }
            }
            if (floor > maxFloor){
                floor = maxFloor
                updateNumber()
            }
        }

        fun checkButtons(){
            nextButton.isClickable = true
            prevButton.isClickable = true
            nextButton.alpha = 1F
            prevButton.alpha = 1F
            if (FloorOrBuiding == true){
                if (floor == maxFloor){
                    nextButton.isClickable = false
                    nextButton.alpha = 0.4F
                }
                if (floor == minFloor){
                    prevButton.isClickable = false
                    prevButton.alpha = 0.4F
                }
                if (floor < minFloor){
                    floor = minFloor
                    checkButtons()
                } else if (floor > maxFloor){
                    floor = maxFloor
                    checkButtons()
                }
            } else {
                updateBuildingFloors()
                if (buiding == 6){
                    nextButton.isClickable = false
                    nextButton.alpha = 0.4F
                }
                if (buiding == 1){
                    prevButton.isClickable = false
                    prevButton.alpha = 0.4F
                }
                if (buiding < 1){
                    buiding = 0
                    checkButtons()
                } else if (buiding > 6){
                    buiding = 6
                    checkButtons()
                }
            }
        }

        fun showEmptyFloors() {
            try {
                val name : String = "b" + floor.toString() + "level_non_active_" + buiding.toString()
                val newImage : Drawable = getImage(requireContext(), name)
                theImage.setImageDrawable(newImage)
            } catch (e : Exception) {
                Log.i("Кнопки этажей", "Не удалось получить картинку этажа " + e.toString())
            }
        }

        fun showTheImage (room : String){
            try {
                val name = "b" + room
                val newImage : Drawable = getImage(requireContext(), name)
                theImage.setImageDrawable(newImage)
                theImage.reset(true)
            } catch (e: Exception){
                if (editableTextView.text.toString() == room) {
                    editableTextView.setText("")
                    editableTextView.hint = getString(R.string.Аудитория_не_найдена)
                }
                Log.i("Кнопка поиска", "Нет картинки для аудитории " + "b" + room + " " + e.toString())
            }
        }

        fun updateImage () {
            //Узнаём этаж и строение из ввода
            val input = editableTextView.text.toString()
            var inputFloor : Int = -1
            var inputBuiding : Int = 0
            if (input.length > 0) {
                inputBuiding = input[0].toString().toInt()
                if (input.length > 1) {
                    inputFloor = input[1].toString().toInt()
                    if (input.toInt() >= 6103 && input.toInt() <= 6110){inputFloor = 0}
                }
            }

            if (buiding == inputBuiding && inputFloor != -1){
                if (buiding == 1){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_1")
                    } else {showEmptyFloors()}
                }
                else if (buiding == 2){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if (inputFloor == 1 && floor == 1){
                        showTheImage("2level_down_2")
                    } else if (inputFloor > floor){
                        showTheImage(floor.toString() + "level_up_2")
                    } else {showEmptyFloors()}
                }
                else if (buiding == 3){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_3")
                    } else {showEmptyFloors()}
                }
                else if (buiding == 4){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if ((floor < inputFloor) && ((input.toInt() > 4400 && input.toInt() < 4410) || (input.toInt() > 4303 && input.toInt() < 4313))){
                        showTheImage(floor.toString() + "level_up_sec_4")
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_4")
                    } else {showEmptyFloors()}
                }
                else if (buiding == 5){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if ((floor < inputFloor) && ((inputFloor == 2 && floor == 1) || (input.toInt() == 5301 || input.toInt() == 5302 || input.toInt() == 5401 || input.toInt() == 5402))){
                        showTheImage(floor.toString() + "level_up_sec_5")
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_5")
                    } else {showEmptyFloors()}
                }
                else if (buiding == 6){
                    if (inputFloor == floor){
                        showTheImage(input)
                    }
                    if (inputFloor > 2 && floor < inputFloor && floor > 0){
                        showTheImage(floor.toString() + "level_up_6")
                    }
                    else if (floor > inputFloor && inputFloor != 0) {
                        showEmptyFloors()
                    }
                    else if (floor == 1){
                        if (input == "6243") { showTheImage("1level_up_6243_6")}
                        else if (input == "6244") { showTheImage("1level_up_6244_6")}
                        else if (input == "6245" || input == "6244") {showTheImage("1level_up_b3_6")}
                        else if (input.toInt() >= 6247 && input.toInt() <= 6257) {showTheImage("1level_up_b4_6")}
                        else if (input == "6258" || input == "6259") {showTheImage("1level_up_b5_6")}
                        else if (input.toInt() >= 6260 && input.toInt() <= 6269) {showTheImage("1level_up_b6_6")}
                        else if (input == "6270") {showTheImage("1level_up_b7_6")}
                        else if (input == "6020") {showTheImage("1level_down_6020_6")}
                        else if (input.toInt() >= 6103 && input.toInt() <= 6110){showTheImage("1level_down_b3_6")}
                        else if (inputFloor == 0) { showTheImage("1level_down_b4_b6_6") }

                        else {showTheImage("1level_up_6")}
                    } else if (inputFloor == 0 && floor != 0) {showEmptyFloors()}
                }

            }
            else {
                showEmptyFloors()
            }

        }

        //the code
        updateNumber()
        editableTextView.hint = getString(R.string.Введите_аудиторию__)


        prevButton.setOnClickListener{
            if (FloorOrBuiding == true){
                floor -= 1
            } else {
                buiding -= 1
            }
            updateNumber()
            checkButtons()
            updateImage()
        }

        nextButton.setOnClickListener {
            if (FloorOrBuiding == true){
                floor += 1
            } else {
                buiding += 1
            }
            checkButtons()
            updateNumber()
            updateImage()
        }

        buidingFloor.setOnClickListener {
            val input = editableTextView.text.toString()
            FloorOrBuiding = !FloorOrBuiding
            if (FloorOrBuiding == true){
                buidingFloor.text = getString(R.string.Этаж)
            } else {
                buidingFloor.text = getString(R.string.Корпус)
            }
            updateNumber()
            checkButtons()
        }

        searchButton.setOnClickListener {
            editableTextView.hint = getString(R.string.Введите_аудиторию__)
            val input = editableTextView.text.toString()
            if (input.length == 0){
                editableTextView.hint = getString(R.string.Аудитория_не_найдена)
                showEmptyFloors()
            } else {
                if (input[0].toString().toInt() > 6 || input[0].toString().toInt() < 1) {
                    editableTextView.setText("")
                    editableTextView.hint = getString(R.string.Ошибка)
                } else {
                    buiding = input[0].toString().toInt()
                    updateBuildingFloors()
                    if (input.length > 1) {
                        val inputFloor = input[1].toString().toInt()
                        if (inputFloor <= maxFloor && inputFloor >= minFloor){
                            floor = inputFloor
                            if (input.toInt() >= 6103 && input.toInt() <= 6110){floor = 0}
                            updateNumber()
                            checkButtons()
                        }
                    }
                    showTheImage(input)
                }
            }

        }

        val input = this.requireActivity().intent.getStringExtra("room")
        if (input != null){
            editableTextView.setText(input)
            searchButton.performClick()
            try {
                if (input[0].toString().toInt() != 0){
                    buiding = input[0].toString().toInt()
                }
                updateBuildingFloors()
                if (input.length > 1) {
                    val inputFloor = input[1].toString().toInt()
                    if (inputFloor <= maxFloor && inputFloor >= minFloor){
                        floor = inputFloor
                        if (input.toInt() >= 6103 && input.toInt() <= 6110){floor = 0}
                        updateNumber()
                        checkButtons()
                    }
                }
            } catch (e: Exception){
                Log.d("Перенос в навигацию", e.toString())
                Toast.makeText(context, getString(R.string.Аудитория_не_является_числом), Toast.LENGTH_SHORT).show()
            }
            showTheImage(input)
        }

        moreRooms.setOnClickListener {
            val goToIntent = Intent(requireContext(), MoreRoomsActivity::class.java)
            goToIntent.putExtra("nowBuilding", buiding)
            this.requireContext().startActivity(goToIntent)
        }

        if (getDefaults("isUpdating", context) ?: 0 == 0){
            setDefaults("isUpdating", 1, context)
        }

        return root
    }


}
