## 题目：Github 用户搜索**

#### Github 提供了获取用户数据的RESTful API：

1. 查询用户：https://api.github.com/search/users?q=test

2. 查询用户的Repos：https://api.github.com/users/test/repos

请编写一个Android App，在输入查询关键字后，能够实时显示出结果列表，
列表每一项对应用户的信息，
包括：用户名，用户头像，用户的编程语言偏好（需要统计使用最多的一种语言，可以从用户Repos中获取到）。


如遇鉴权问题等请查阅GitHub文档解决。

**要求：**
* 功能完整正确，符合题目要求
* 程序架构清晰，体现分层思想，建议使用MVP架构
* 代码尽量整洁，
不包含无用代码，可以使用github上的开源代码
* 代码必须使用github托管
* 请使用Android Studio开发