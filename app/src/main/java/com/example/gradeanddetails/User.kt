package com.example.gradeanddetails

import android.content.Context

object User {
    private var user = YaoApplication.context.getSharedPreferences("user", Context.MODE_PRIVATE)
    fun stuId() = user.getString("stuId", "")!!
    fun stuPwd() = user.getString("stuPwd", "")!!
    fun studentId() = user.getString("studentId", "")!!
    fun stuId(id:String)
    {
        user.open { putString("stuId", id) }
    }
    fun stuPwd(pwd:String)
    {
        user.open { putString("stuPwd", pwd) }
    }
    fun studentId(id:String)
    {
        user.open { putString("studentId", id) }
    }
}