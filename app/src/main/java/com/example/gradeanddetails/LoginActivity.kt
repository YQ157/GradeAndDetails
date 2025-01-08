package com.example.gradeanddetails

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradeanddetails.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.login.setOnClickListener {
            if(binding.stuId.text!!.isBlank())
            {
                binding.id.error = "Please enter your student ID"
            }
            if(binding.stuPwd.text!!.isBlank())
            {
                binding.pwd.error = "Please enter your password"
            }
            else
            {
                inLogin()
                login(binding.stuId.text.toString(),binding.stuPwd.text.toString())
            }
        }
        binding.stuId.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                binding.id.error = null
            } else {
                if(binding.stuId.text!!.isBlank())
                {
                    binding.id.error = "Please enter your student ID"
                }
            }
        }
        binding.stuPwd.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                binding.pwd.error = null
            } else {
                if(binding.stuPwd.text!!.isBlank())
                {
                    binding.pwd.error = "Please enter your password"
                }
            }
        }
        binding.webView.settings.javaScriptEnabled = true
        autoLogin()
    }

    override fun onStart() {
        super.onStart()
        binding.webView.loadUrl("about:blank")
    }

    private fun inLogin(){
        binding.login.visibility = View.GONE
        binding.circular.visibility = View.VISIBLE
        binding.circular.show()
    }
    private fun outLogin(){
        binding.login.visibility = View.VISIBLE
        binding.circular.visibility = View.GONE
        binding.circular.hide()
    }
    private fun autoLogin() {
        if(User.stuId().isNotBlank()){
            binding.stuId.setText(User.stuId())
            if(User.stuPwd().isNotBlank()){
                binding.stuPwd.setText(User.stuPwd())
            }
        }
        if(User.stuId().isNotBlank() and User.stuPwd().isNotBlank() and User.studentId().isNotBlank()){
            msg("尝试自动登录")
            inLogin()
            login(User.stuId(),User.stuPwd(),true)
            outLogin()
        }
    }
    private fun login(stuId:String,stuPwd:String,flag:Boolean = false) {
//        val cookieManager = CookieManager.getInstance()
//        cookieManager.removeAllCookies(null)
//        cookieManager.flush()
        binding.webView.run{
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    msg(url.toString())
                    when{
                        url?.startsWith("https://jwxt.cumtb.edu.cn/student/login?refer=",ignoreCase = true) == true ->{
                            loadUrl("https://auth.cumtb.edu.cn/authserver/login?service=https%3A%2F%2Fjwxt.cumtb.edu.cn%2Fstudent%2Fsso%2Flogin")
                        }
                        url == "https://auth.cumtb.edu.cn/authserver/login?service=https%3A%2F%2Fjwxt.cumtb.edu.cn%2Fstudent%2Fsso%2Flogin"->{
                            loadUrl("javascript:(function() { document.querySelector(\"#mobileUsername\").value = '${stuId}'; })()")
                            loadUrl("javascript:(function() { document.querySelector(\"#mobilePassword\").value = '${stuPwd}'; })()")
                            loadUrl("javascript:(function() { document.querySelector(\"#rememberMe\").checked=true; })()")
                            loadUrl("javascript:(function() { document.querySelector(\"#load\").click(); })()")
                            msg("尝试登录...")
                            Handler(Looper.getMainLooper()).postDelayed({
                                if(view?.url == "https://auth.cumtb.edu.cn/authserver/login?service=https%3A%2F%2Fjwxt.cumtb.edu.cn%2Fstudent%2Fsso%2Flogin"){
                                    binding.pwd.error = "请检查学号或密码"
                                    outLogin()
                                }
                            },1000)
                        }
                        url == "https://jwxt.cumtb.edu.cn/student/home"->{
                            msg("登陆成功")
                            User.stuId(binding.stuId.text.toString())
                            User.stuPwd(binding.stuPwd.text.toString())
                            msg("保存学号和密码在本地")
                            loadUrl("https://jwxt.cumtb.edu.cn/student/for-std/grade/sheet")
                            msg("加载成绩页面获取studentId")
                        }
                        url?.startsWith("https://jwxt.cumtb.edu.cn/student/for-std/grade/sheet",ignoreCase = true) == true ->{
                            val studentId = Regex("^[0-9]+").find(url.substringAfterLast('/'))?.value?.toInt() ?: 0
                            if(studentId != 0){
                                msg("studentId获取成功${studentId}")
                                User.studentId(studentId.toString())
                                loadUrl("https://jwxt.cumtb.edu.cn/eams-student-grade-app/index.html")
                            }
                        }
                        url?.startsWith("https://jwxt.cumtb.edu.cn/eams-student-grade-app/index.html",ignoreCase = true) == true ->{
                            val cookies = CookieManager.getInstance().getCookie("https://jwxt.cumtb.edu.cn/student/home")
                            val intent = Intent()
                            var flag1 = true
                            intent.putExtra("cookies",cookies)
                            cookies?.split(";")?.forEach{ cookie->
                                val trimmedCookie = cookie.trim()
                                val parts = trimmedCookie.split("=")
                                if (parts.size == 2) {
                                    val cookieName = parts[0]
                                    val cookieValue = parts[1]
                                    msg("Cookie name: $cookieName, value: $cookieValue")
                                    intent.putExtra(cookieName, cookieValue)
                                    if(cookieName == "X-Id-Token")flag1 = false
                                }
                            }
                            if(flag1)
                            {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    outLogin()
                                    val cookies1 = CookieManager.getInstance().getCookie("https://jwxt.cumtb.edu.cn/student/home")
                                    val intent1 = Intent()
                                    intent1.putExtra("cookies",cookies1)
                                    var flag2 = true
                                    cookies1?.split(";")?.forEach{ cookie->
                                        val trimmedCookie = cookie.trim()
                                        val parts = trimmedCookie.split("=")
                                        if (parts.size == 2) {
                                            val cookieName = parts[0]
                                            val cookieValue = parts[1]
                                            msg("Cookie name: $cookieName, value: $cookieValue")
                                            intent1.putExtra(cookieName, cookieValue)
                                            if(cookieName == "X-Id-Token")flag2 = false
                                        }
                                    }
                                    if(flag2){
                                        msg("获取X-Id-Token失败")
                                        Toast.makeText(YaoApplication.context,"请重新登录",Toast.LENGTH_SHORT).show()
                                    } else {
                                        setResult(RESULT_OK,intent1)
                                        finish()
                                    }
                                },1000)
                            } else {
                                setResult(RESULT_OK,intent)
                                finish()
                            }
                        }
                    }
                }
            }
            if(!flag){
                loadUrl("https://auth.cumtb.edu.cn/authserver/login?service=https%3A%2F%2Fjwxt.cumtb.edu.cn%2Fstudent%2Fsso%2Flogin")
            } else {
                loadUrl("https://jwxt.cumtb.edu.cn/student/home")
            }

        }
    }
}