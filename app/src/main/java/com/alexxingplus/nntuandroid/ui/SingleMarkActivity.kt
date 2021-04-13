package com.alexxingplus.nntuandroid.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.alexxingplus.nntuandroid.R

class SingleMarkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_mark)

        val disNameBig = findViewById<TextView>(R.id.disNameBig)
        val fknLabel = findViewById<TextView>(R.id.FknLabel)
        val propFknLabel = findViewById<TextView>(R.id.propFknLabel)
        val sknLabel = findViewById<TextView>(R.id.sknLabel)
        val propSknLabel = findViewById<TextView>(R.id.propSknLabel)
        val typeLabel = findViewById<TextView>(R.id.typeLabelBig)
        val resultLabel = findViewById<TextView>(R.id.resultLabelBig)

        val disName = intent.getStringExtra("disName")
        val fkn = intent.getStringExtra("fkn")
        val propFkn = intent.getStringExtra("propFkn")
        val skn = intent.getStringExtra("skn")
        val propSkn = intent.getStringExtra("propSkn")
        val type = intent.getStringExtra("type")
        val result = intent.getStringExtra("result")

        disNameBig.text = disName
        fknLabel.text = fkn
        propFknLabel.text = propFkn
        sknLabel.text = skn
        propSknLabel.text = propSkn
        typeLabel.text = type
        resultLabel.text = result

        if (fkn == null){
            fknLabel.text = "0"
            fknLabel.alpha = 0.2F
            propFknLabel.text = "0"
            propFknLabel.alpha = 0.2F
        }
        if (skn == null){
            sknLabel.text = "0"
            sknLabel.alpha = 0.2F
            propSknLabel.text = "0"
            propSknLabel.alpha = 0.2F
        }

        if (result != null){
            typeLabel.text = typeLabel.text.toString() + ":"
        }

        if (result == "зачёт"){
            resultLabel.text = getString(R.string.сдан) +" 😎"
            resultLabel.setTextColor(Color.parseColor("#008000"))
        } else if (result == "5"){
            resultLabel.text = "5 😎"
            resultLabel.setTextColor(Color.parseColor("#008000"))
        }


    }
}
