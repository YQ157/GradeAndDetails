package com.example.gradeanddetails

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

fun SharedPreferences.open(block:SharedPreferences.Editor.()->Unit){
    val editor = edit()
    editor.block()
    editor.apply()
}
var debug = true
fun Context.msg(msg:String){
    if(debug){
        Log.d(this.javaClass.simpleName,msg)
    }
}