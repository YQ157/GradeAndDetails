[下载链接](https://wwxe.lanzoub.com/iI01C2kc4nwj)

![下载二维码](https://s1.imagehub.cc/images/2025/01/08/138e227f697b638cc855602215a7c4bc.png)
# 项目原理
1. 教务系统获取成绩是构造一个 http 请求，这个请求需要的关键参数是 cookie 值`__managerid__`
2. 查询平时分，是`https://jwxt.cumtb.edu.cn/eams-student-grade-app/index.html`这个网页构造的请求的响应中，附带了平时成绩信息(疑似不小心)，这个请求的关键参数是`X-Id-Token`
3. 通过 Webview 和 js 注入，模拟访问并登录`https://jwxt.cumtb.edu.cn/student/home`和`https://jwxt.cumtb.edu.cn/eams-student-grade-app/index.html`两个网站，生成并获取`__managerid__`和`X-Id-Token`两个Cookie值
4. 构造相应 http 请求获取 json 响应，解析绘制视图
# 为什么要 Webview 模拟登录？
1. 身份认证页面有 js 要加载
2. 登录并非明文密码，而是有一个`encrypt.js`负责动态加密
# 为什么不把模拟登录部分放到服务器解决？
1. 保护隐私，不设立云服务器处理信息，更安全
2. 云服务器如果频繁登录不同账号，可能被 ban 掉 IP

~~# 为什么一直提示“学号或密码错误”？~~

~~1. 可能真的存在错误~~

~~2. 前几次输错了，登录就需要验证码了，所以模拟登录无法通过，等待几分钟即可~~
# 为什么有时候没有记住密码？
1. 如果密码错误多次，要输入验证码，就切换到网页登录了，此时不保存密码
2. 之后正常通过 app 登录，密码就可以在本地保存
# 推荐
[成绩闹钟](https://github.com/YQ157/GradeAlarm/tree/main)：有新科目出成绩时第一时间收到邮件
