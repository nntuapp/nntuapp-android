package com.alexxingplus.nntuandroid.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.alexxingplus.nntuandroid.MoreRoomsActivity
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.ui.getDefaults
import com.alexxingplus.nntuandroid.ui.setDefaults
import com.jsibbold.zoomage.ZoomageView
import com.alexxingplus.nntuandroid.MainActivity
import com.alexxingplus.nntuandroid.ui.news.loadLastID
import com.alexxingplus.nntuandroid.ui.news.setBadge
import kotlin.concurrent.thread
import com.alexxingplus.nntuandroid.ui.news.updateLastID


class HomeFragment : Fragment() {


    private var floor : Int = 1
    private var building : Int = 6
    private var minFloor : Int = 0
    private var maxFloor : Int = 5

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
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //vals from xml
        val editableTextView : TextView = root.findViewById(R.id.editText)
        val searchButton : Button = root.findViewById(R.id.nntuapp_searchButton)
        val prevFloorButton : ImageButton = root.findViewById(R.id.prevButton)
        var nextFloorButton : ImageButton = root.findViewById(R.id.nextButton)
        val floorText : TextView = root.findViewById(R.id.floorText)
        val theImage : ZoomageView = root.findViewById(R.id.theImage)
        val moreRooms : Button = root.findViewById(R.id.moreRoomsButton)
        val rotateButton : ImageButton = root.findViewById(R.id.rotateButton)

        val buildingText : TextView = root.findViewById(R.id.buildingText)
        val prevBuildingButton : ImageButton = root.findViewById(R.id.prevBuildingButton)
        val nextBuildingButton : ImageButton = root.findViewById(R.id.nextBuildingButton)



        //funs for vals
        fun updateNumber(){
            floorText.text = floor.toString()
            buildingText.text = building.toString()
        }

        fun updateBuildingFloors () {
            when (this.building){
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
            updateBuildingFloors()

            nextFloorButton.isClickable = true
            prevFloorButton.isClickable = true
            nextBuildingButton.isClickable = true
            prevBuildingButton.isClickable = true

            nextFloorButton.alpha = 1F
            prevFloorButton.alpha = 1F
            nextBuildingButton.alpha = 1F
            prevBuildingButton.alpha = 1F


            if (floor >= maxFloor){
                floor = maxFloor
                nextFloorButton.isClickable = false
                nextFloorButton.alpha = 0.4F
            }
            if (floor <= minFloor){
                floor = minFloor
                prevFloorButton.isClickable = false
                prevFloorButton.alpha = 0.4F
            }

            if (building >= 6){
                building = 6
                nextBuildingButton.isClickable = false
                nextBuildingButton.alpha = 0.4F
            }
            if (building <= 1){
                building = 1
                prevBuildingButton.isClickable = false
                prevBuildingButton.alpha = 0.4F
            }
        }

        fun showEmptyFloors() {
            try {
                val name : String = "b" + floor.toString() + "level_non_active_" + building.toString()
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
            var inputBuilding : Int = 0
            if (input.length > 0) {
                inputBuilding = input[0].toString().toInt()
                if (input.length > 1) {
                    inputFloor = input[1].toString().toInt()
                    if (input.toInt() >= 6103 && input.toInt() <= 6110){inputFloor = 0}
                }
            }

            if (building == inputBuilding && inputFloor != -1){
                if (building == 1){
                    val weirdBlock = arrayListOf(1280, 1281, 1361, 1362)
                    if (floor < inputFloor && weirdBlock.contains(input.toInt())){
                        showTheImage("${floor}level_up_weird_1")
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_1")
                    } else if (input.toInt() == 1161 && floor == 2){
                        showTheImage("2level_down_1")
                    } else if (inputFloor == floor){
                        showTheImage(input)
                    } else {showEmptyFloors()}
                }
                else if (building == 2){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if (inputFloor == 1 && floor == 1){
                        showTheImage("2level_down_2")
                    } else if (inputFloor > floor){
                        showTheImage(floor.toString() + "level_up_2")
                    } else {showEmptyFloors()}
                }
                else if (building == 3){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_3")
                    } else {showEmptyFloors()}
                }
                else if (building == 4){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if ((floor < inputFloor) && ((input.toInt() > 4400 && input.toInt() < 4410) || (input.toInt() > 4303 && input.toInt() < 4313))){
                        showTheImage(floor.toString() + "level_up_sec_4")
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_4")
                    } else {showEmptyFloors()}
                }
                else if (building == 5){
                    if (inputFloor == floor){
                        showTheImage(input)
                    } else if ((floor < inputFloor) && ((inputFloor == 2 && floor == 1) || (input.toInt() == 5301 || input.toInt() == 5302 || input.toInt() == 5401 || input.toInt() == 5402))){
                        showTheImage(floor.toString() + "level_up_sec_5")
                    } else if (floor < inputFloor){
                        showTheImage(floor.toString() + "level_up_5")
                    } else {showEmptyFloors()}
                }
                else if (building == 6){
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
        checkButtons()
        updateNumber()
        editableTextView.hint = getString(R.string.Введите_аудиторию__)

        prevFloorButton.setOnClickListener{
            floor -= 1
            checkButtons()
            updateNumber()
            updateImage()
        }

        nextFloorButton.setOnClickListener {
            floor += 1
            checkButtons()
            updateNumber()
            updateImage()
        }

        prevBuildingButton.setOnClickListener{
            building -= 1
            checkButtons()
            updateNumber()
            updateImage()
        }

        nextBuildingButton.setOnClickListener {
            building += 1
            checkButtons()
            updateNumber()
            updateImage()
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
                    building = input[0].toString().toInt()
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

//        val input = if (isAdded) this.requireActivity().intent.getStringExtra("room") else null
        val input = this.requireActivity().intent.getStringExtra("room")
        if (input != null){
            editableTextView.setText(input)
            searchButton.performClick()
            try {
                if (input[0].toString().toInt() != 0){
                    building = input[0].toString().toInt()
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
            goToIntent.putExtra("nowBuilding", building)
            this.requireContext().startActivity(goToIntent)
        }

        if (getDefaults("isUpdating", context) ?: 0 == 0){
            setDefaults("isUpdating", 1, context)
        }


        var imageRotation : Float = 0F

        val fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        val rotate = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_center)

        rotateButton.setOnClickListener {
            theImage.startAnimation(rotate)
            imageRotation += 90
            theImage.rotation = imageRotation
        }

        return root
    }


}
