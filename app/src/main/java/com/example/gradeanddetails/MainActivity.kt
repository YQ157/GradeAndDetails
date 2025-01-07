package com.example.gradeanddetails

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gradeanddetails.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.mainFab.setOnClickListener {
            if (binding.mainFab.isOpened) {
                binding.mainFab.close(true)
            } else {
                binding.mainFab.open(true)
            }
        }
        binding.relogin.setOnClickListener {
            binding.mainFab.close(true)
            User.stuId("")
            User.stuPwd("")
            User.studentId("")
            Toast.makeText(this,"已退出登录",Toast.LENGTH_SHORT).show()
            login()
        }
        binding.about.setOnClickListener {
            binding.mainFab.close(true)
            val dialog = AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage(
                    SpannableStringBuilder().apply{
                        append("作者:")
                        append(SpannableString("Yao").apply{
                            setSpan(StyleSpan(Typeface.BOLD),0, length,3)
                        })
                        append("\n\n项目仓库:\nhttps://github.com/YQ157/GradeAndDetails\n\n")
                        append("账号密码仅在本地保存，仅访问教务系统，未访问任何第三方服务器\n\n")
                        append(SpannableString("主界面下拉可以刷新").apply{
                            setSpan(StyleSpan(Typeface.BOLD),0, length,3)
                        })
                    }
                )
                .setPositiveButton("OK"){ dialog,_->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }
        binding.refresh.setOnRefreshListener {
            getGrade()
        }
        login()
    }
    private lateinit var cookies:String
    private lateinit var xIdToken:String
    private val requestDataLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==RESULT_OK){
            val data:Intent?=it.data
            if(data!=null){
                cookies=data.getStringExtra("cookies")!!
                xIdToken=data.getStringExtra("X-Id-Token")!!
                binding.cir.show()
                binding.progress.text = "1/4 登录成功，开始读取成绩..."
                getGrade()
            }
        }
    }
    private fun login(){
        val intent= Intent(this,LoginActivity::class.java)
        requestDataLauncher.launch(intent)
    }
    private var gradeList = listOf<Grade>()
    private val gradeTemp = mutableMapOf<String,Grade>()
    private fun getGrade(){
        if(!::cookies.isInitialized)login()
        thread {
            val client = OkHttpClient.Builder()
                .cookieJar(object : CookieJar {
                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        cookies.forEach {
                            CookieManager.getInstance().setCookie(url.toString(), it.toString())
                        }
                    }
                    override fun loadForRequest(url: HttpUrl): List<Cookie> =
                        if(::cookies.isInitialized)
                            cookies.split(";").map { Cookie.parse(url, it.trim())!! }
                        else
                            cookies.split(";").map { Cookie.parse(url, it.trim())!! }

                })
                .build()
            val request = Request.Builder()
                .url("https://jwxt.cumtb.edu.cn/student/for-std/grade/sheet/get-grade-data/${User.studentId()}?semesterId=")
                //.url("https://jwxt.cumtb.edu.cn/student/for-std/grade/sheet/info/${studentID}")
                .build()
            val response = client.newCall(request).execute()
            if(response.isSuccessful){
                val body = response.body?.string()?:"空"
                msg(body)
                if(body.contains("studentGradeList"))
                {
                    val semesterId = JSONObject(body).getJSONObject("studentGradeList").getJSONObject("semesterId2studentGrades")
                    msg(semesterId.toString())
                    val type = object : TypeToken<Map<String, Data>>() {}.type
                    val data : Data = Gson().fromJson<Map<String, Data>>(body, type)["studentGradeList"]!!
                    for(i in data.semesterId2studentGrades)
                    {
                        for(j in i.value)
                        {
                            //gradeList.add(Grade(j.course.code,j.course.nameZh,j.gaGrade,j.course.credits.toString()))
                            gradeTemp[j.course.code] = Grade(j.course.code,j.course.nameZh,j.gaGrade,j.course.credits.toString(),i.key)
                        }
                    }
                }
//                gradeList.forEach {
//                    msg("${it.name},${it.score}")
//                }
                runOnUiThread {
                    binding.progress.text = "2/4 成绩读取成功，开始读取平时分..."
                }
                val request2 = Request.Builder()
                    .url("https://jwxt.cumtb.edu.cn/eams-micro-server/api/v1/grade/student/grades")
                    .addHeader("Accept", "application/json")  // 必要的请求头：接受JSON格式的响应
                    .addHeader("Content-Type", "application/json;charset=UTF-8")  // 必要的请求头：请求体类型
                    .addHeader("Cookie",cookies)
                    .addHeader("X-Id-Token",xIdToken)
                    .build()
                val response2 = client.newCall(request2).execute()
                if(response2.isSuccessful){
                    val body2 = response2.body?.string()?:"空"
                    msg(body2)
                    val type2 = object : TypeToken<Data2>() {}.type
                    val data2 = Gson().fromJson<Data2>(body2, type2).data
                    data2.forEach {
                        gradeTemp[it.courseCode]?.detail = it.gradeDetail
                    }
                    gradeList = gradeTemp.values.toList().sortedByDescending { it.semester }
                    runOnUiThread {
                        binding.progress.text = "3/4 平时分读取成功，开始绘制视图..."
                        binding.gradeList.adapter = GradeAdapter(gradeList)
                        binding.gradeList.layoutManager = LinearLayoutManager(this)
                        binding.progress.text = "4/4 加载完成"
                        binding.cir.hide()
                        binding.loading.visibility = View.GONE
                        binding.refresh.visibility = View.VISIBLE
                        binding.refresh.isRefreshing = false
                        Toast.makeText(this,"刷新成功",Toast.LENGTH_SHORT)
                    }
//                    gradeList.forEach {
//                        msg("${it.name},${it.score},${it.details}")
//                    }
                }
            }
        }
    }
}