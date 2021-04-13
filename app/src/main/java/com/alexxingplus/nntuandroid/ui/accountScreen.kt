package com.alexxingplus.nntuandroid.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible

import com.alexxingplus.nntuandroid.R
import me.grantland.widget.AutofitTextView

fun String.capitalize() : String{
    var output = intern()
    output = output.replace(" ", "-")
    output = output.toUpperCase()
    return output
}

class settings : Fragment() {

    fun getImage (c: Context, imgname: String) : Drawable {
        return c.resources.getDrawable(c.resources.getIdentifier(imgname, "drawable", c.packageName), c.theme)
    }

//    companion object {
//        fun newInstance() = settings()
//    }

    fun cleanString (input: String) : String {
        if (input == "") {return input}
        var output = input
        while (output.last().toString() == " "){
            output = output.dropLast(1)
        }
        while (output.first().toString() == " "){
            output = output.substring(1)
        }
        return output
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().setTheme(R.style.AppTheme)

        val root = inflater.inflate(R.layout.account_fragment, container, false)

        //userDefaults
        val userDefaults = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root

        //inputStack
        val inputStack : LinearLayout = root.findViewById(R.id.inputStack)
        val nameField : EditText = root.findViewById(R.id.nameField)
        val secondNameField : EditText = root.findViewById(R.id.secondNameField)
        val otchNameField : EditText = root.findViewById(R.id.OtchField)
        val nstudField : EditText = root.findViewById(R.id.nstudField)
        val groupField : EditText = root.findViewById(R.id.groupField)
        val enterButton : Button = root.findViewById(R.id.enterButton)

        //outputStack
        val outputStack : LinearLayout = root.findViewById(R.id.outputStack)
        val outputName : AutofitTextView = root.findViewById(R.id.outputName)
        val outputSecondName : AutofitTextView = root.findViewById(R.id.outputSecondName)
        val outputOtchestvo : AutofitTextView = root.findViewById(R.id.outputOtchestvo)
        val outputGroup : AutofitTextView = root.findViewById(R.id.outputGroup)
        val outputNstud : AutofitTextView = root.findViewById(R.id.outputNstud)
        val leaveButton : Button = root.findViewById(R.id.outButton)
        val uselessGroupLabel : TextView = root.findViewById(R.id.uselessGroupLabel)
        val nStudLinearLayout : LinearLayout = root.findViewById(R.id.nstudLinearLayout)
        val editButton: ImageButton = root.findViewById(R.id.editButton)

        //more buttons
        val addTimeTableButton : ImageButton = root.findViewById(R.id.settingsButtonFromAccount)
        val warningLabel : TextView = root.findViewById(R.id.warningLabel)
//        val darkButton : ImageButton = root.findViewById(R.id.darkModeButton)

//        val nightMode = AppCompatDelegate.getDefaultNightMode()

//        if (nightMode == AppCompatDelegate.MODE_NIGHT_NO){
//            val newImage : Drawable = getImage(requireContext(), "ic_sun")
//            darkButton.setImageDrawable(newImage)
//        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES){
//            val newImage : Drawable = getImage(requireContext(), "ic_moon")
//            darkButton.setImageDrawable(newImage)
//        } else {
//            val newImage : Drawable = getImage(requireContext(), "ic_auto")
//            darkButton.setImageDrawable(newImage)
//        }

//        darkButton.setOnClickListener {
//            val nightMode = AppCompatDelegate.getDefaultNightMode()
//            if (nightMode == AppCompatDelegate.MODE_NIGHT_NO){
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                userDefaults.edit().putInt("mode", -1).apply()
//            } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES){
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//                userDefaults.edit().putInt("mode", 0).apply()
//                val newImage : Drawable = getImage(requireContext(), "ic_auto")
//                darkButton.setImageDrawable(newImage)
//                Toast.makeText(requireContext(), getString(R.string.Темная_тема), Toast.LENGTH_LONG).show()
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                val newImage : Drawable = getImage(requireContext(), "ic_sun")
//                darkButton.setImageDrawable(newImage)
//                userDefaults.edit().putInt("mode", 1).apply()
//            }
//        }

        fun updateScreen (isEntered: Boolean) {
            if (isEntered == true){
                //hiding inputStack
                nameField.isEnabled = false
                secondNameField.isEnabled = false
                otchNameField.isEnabled = false
                nstudField.isEnabled = false
                groupField.isEnabled = false
                enterButton.isClickable = false

                inputStack.isVisible = false
                enterButton.isVisible = false

                //unhiding OutputStack
                outputStack.alpha = 1F
                leaveButton.isVisible = true
                leaveButton.isClickable = true

                //изменение от 13.02 - добавляем возможность редактирования
                editButton.isVisible = true
                editButton.isClickable = true

                addTimeTableButton.isVisible = true
                addTimeTableButton.isClickable = true

                //filling outputStarck
                val name = userDefaults.getString("name", null)
                val secondName = userDefaults.getString("secondName", null)
                val otchName = userDefaults.getString("otch", "")
                val group = userDefaults.getString("group", null)
                val nstud = userDefaults.getString("nstud", null)

                if (nstud == "" || nstud == null){
                    outputGroup.isVisible = false
                    nStudLinearLayout.isVisible = false
                    uselessGroupLabel.text = getString(R.string.Преподаватель)
                } else {
                    outputGroup.isVisible = true
                    nStudLinearLayout.isVisible = true
                    uselessGroupLabel.text = getString(R.string.Группа_)
                }
//                var size : Float = 10.toFloat()
//
//                fun updateSizes(){
//                    outputName.textSize = size
//                    outputSecondName.textSize = size
//                    outputOtchestvo.textSize = size
//                }

                if (name != null && secondName != null){

//                    updateSizes()
//                    val width = outputStack.clipBounds.width()
//                    while (outputName.clipBounds.width() < width - 30 && outputSecondName.clipBounds.width() < width - 30 && outputOtchestvo.clipBounds.width() < width - 30){
//                        size ++
//                        updateSizes()
//                    }
                    outputName.text = name
                    outputSecondName.text = secondName
                    outputOtchestvo.text = otchName
                    if (group != null){
                        outputGroup.text = group
                    }
                    if (nstud != null){
                        outputNstud.text = nstud
                    }
                }
            } else {
                //unhiding inputStack
                nameField.isEnabled = true
                secondNameField.isEnabled = true
                otchNameField.isEnabled = true
                nstudField.isEnabled = true
                groupField.isEnabled = true
                enterButton.isClickable = true

                inputStack.isVisible = true
                enterButton.isVisible = true


                //unhiding OutputStack
                outputStack.alpha = 0F
                leaveButton.isVisible = false
                leaveButton.isClickable = false

                //изменение от 13.02 - добавляем возможность редактирования
                editButton.isVisible = false
                editButton.isClickable = false

                addTimeTableButton.isVisible = false
                addTimeTableButton.isClickable = false

            }
        }

        var Entered = userDefaults.getBoolean("entered", false)
        updateScreen(Entered)

        enterButton.setOnClickListener{
            fun save(){
                warningLabel.alpha = 0F
                with (userDefaults.edit()){
                    putString("name", cleanString(nameField.text.toString()))
                    putString("secondName", cleanString(secondNameField.text.toString()))
                    putString("otch", cleanString(otchNameField.text.toString()))
                    putString("nstud", cleanString(nstudField.text.toString()))
                    putString("group", cleanString(groupField.text.toString()).capitalize())
                    putBoolean("entered", true)
                    commit() // можно коммит, но apply работает в фоне
                }
                Entered = true
                updateScreen(Entered)
            }

            val dialog = Dialog(requireContext())
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.enter_popup)

            val bakButton = dialog.findViewById<Button>(R.id.bakButton)
            val magButton = dialog.findViewById<Button>(R.id.magButton)
            val prepButton = dialog.findViewById<Button>(R.id.prepButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

            val checkForStudents = nameField.text.toString() == "" || secondNameField.text.toString() == "" || nstudField.text.toString() == "" || groupField.text.toString() == ""
            val checkForPreps = nameField.text.toString() == "" || secondNameField.text.toString() == ""

            bakButton.setOnClickListener {
                if (checkForStudents){
                    if (!Entered){
                        warningLabel.alpha = 1F
                    }
                } else {
                    save()
                    userDefaults.edit().putString("userType", "bak_spec").apply()
                }
                dialog.dismiss()
            }

            magButton.setOnClickListener {
                if (checkForStudents){
                    if (!Entered){
                        warningLabel.alpha = 1F
                    }
                } else {
                    save()
                    userDefaults.edit().putString("userType", "mag").apply()
                }
                dialog.dismiss()
            }

            prepButton.setOnClickListener {
                if (checkForPreps){
                    if (!Entered){
                        warningLabel.alpha = 1F
                    }
                } else {
                    save()
                    with(userDefaults.edit()){
                        putString("userType", "prepod").apply()
                        putString("nstud", "")
                        putString("group", (cleanString(secondNameField.text.toString()) +  "-" + cleanString(nameField.text.toString()) + "-" + cleanString(otchNameField.text.toString())).capitalize())
                        apply()
                    }
                }
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        leaveButton.setOnClickListener {
            //изменение от 13.02 - добавляем возможность редактирования
            nameField.setText("")
            secondNameField.setText("")
            otchNameField.setText("")
            nstudField.setText("")
            groupField.setText("")

            val dbHandler = DBHelper(requireContext(), null)
            dbHandler.clear()
            Entered = false
            with (userDefaults.edit()){
                putString("name", null)
                putString("secondName", null)
                putString("otch", null)
                putString("nstud", null)
                putString("group", null)
                putBoolean("entered", false)
                putString("userType", null)
                commit() // можно коммит, но apply работает в фоне
            }
            updateScreen(false)
        }

        //изменение от 13.02 - добавляем возможность редактирования
        editButton.setOnClickListener {
            Entered = false
            nameField.setText(userDefaults.getString("name", ""))
            secondNameField.setText(userDefaults.getString("secondName", ""))
            otchNameField.setText(userDefaults.getString("otch", ""))
            nstudField.setText(userDefaults.getString("nstud", ""))
            groupField.setText(userDefaults.getString("group", ""))
            updateScreen(false)
        }

        addTimeTableButton.setOnClickListener {
            val intent = Intent(requireContext(), CodeActivity::class.java)
            intent.putExtra("entered", Entered)
            intent.putExtra("group", userDefaults.getString("group", "").toString())
            requireContext().startActivity(intent)
        }

        var prevNstud = 0
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nstudText = nstudField.text.toString()
//                Log.d("Введено", nstudText)
                if (nstudText.length == 2 && nstudText.toInt() >= 20){
                    if (prevNstud < nstudText.length){
                        nstudField.setText(nstudText + "-")
                        //такого в свифте делать не надо, а тут курсор скачет
                        nstudField.setSelection(nstudField.text.length)
                    } else {
                        nstudField.setText(nstudText.dropLast(1))
                        nstudField.setSelection(nstudField.text.length)
                    }
//                    if (nstudText.last() == '-'){
//                        nstudField.setText(nstudText.dropLast(1))
//                        nstudField.setSelection(nstudField.text.length)
//                    }
                }
                prevNstud = nstudText.length
            }
        }

        nstudField.addTextChangedListener(watcher)

//        darkModeButton.setOnClickListener {
//            this.activity!!.setTheme(R.style.AppTheme_Dark)
//        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }



}
