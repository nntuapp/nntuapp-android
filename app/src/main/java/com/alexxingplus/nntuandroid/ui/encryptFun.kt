package com.alexxingplus.nntuandroid.ui

import java.lang.StringBuilder
import java.util.*

const val receiveKey = "C9h9UPcqMGUbrc3X"
const val postKey = "Vc4hdHaPxfYXUBwk"

const val teachersURL = "http://194.58.97.17/teachers.json"
const val lastIdURL = "http://194.58.97.17/events/lastID.json"
const val eventsURL = "http:/194.58.97.17:3001/"

fun encrypt (input: String) : String {
    val ABC = "-1234567890СДЧЖИЭЫЬВТЕЁЪЦКЩФЗАГЯЛЙОШУБПМРХНЮСДЧЖИЭЫЬВТЕЁЪЦКЩФЗАГЯЛЙОШУБПМРХНЮ"
    val smallABC = "-1234567890сдчжиэыьвтеёъцкщфзагялйошубпмрхнюсдчжиэыьвтеёъцкщфзагялйошубпмрню"
    val output = StringBuilder()
    val day = Calendar.getInstance().get(Calendar.DATE)
    for (i in 0 until input.count()){
        var j = 0
        if (ABC.contains(input[i])){
            while (ABC[j] != input[i]){
                j++
            }
            val symbol = ABC[j+day]
            output.append(symbol)
        } else if (smallABC.contains(input[i])){
            while (smallABC[j] != input[i]){
                j++
            }
            val symbol = smallABC[j+day]
            output.append(symbol)
        } else {
            output.append(input[i])
        }
    }
    return output.substring(0,output.length)
}