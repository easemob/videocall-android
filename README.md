# videocall-android

简介
本Demo展示了怎么使用环信SDK创建一个简易多人音视频Demo，可以进行音频、视频会话，上麦、下麦互动。
本分支是最新的分支，集成了简单的白板功能，使用WebView显示白板页面
使用的WebView组件是集成了开源的腾讯TBS浏览器中的X5WebView,大家可以参考：https://x5.tencent.com/

demo运行

安装Android Sudio 直接运行项目

代码结构

多人音视频Demo主要包含的类如下：

MainActivity 加入房间页面

ConferenceActivity 会议展示页面

SettingActivity 个人设置页面

RoomSettingActivity 房间设置页面

TalkerListActivity 主播列表页面

MultiMemberView  会议视频小窗口类

WhiteBoardTbsActivity 白板显示窗口

ConferenceSession 会议管理类

集成文档
多人音视频集成文档参见官方文档：http://docs-im.easemob.com/rtc/conference/android
