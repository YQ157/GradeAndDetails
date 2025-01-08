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
# 为什么一直提示“学号或密码错误”？
1. 可能真的存在错误
2. 前几次输错了，登录就需要验证码了，所以模拟登录无法通过，等待几分钟即可
