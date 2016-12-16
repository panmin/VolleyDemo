# Volley && okHttp

### Volley
* 简介
Volley是Google提供的网络通信库，能使网络通信更快、更简单、更健壮。

* 优缺点
  * 优点：
     1. 可以和Activity和生命周期的联动（Activity结束时同时取消所有网络请求）
     2. 有基于队列的网络请求
     3. 使用简单，适用于大频率小数据量的请求
     4. 便于扩展：Volley是官方出的，volley在设计的时候是将具体的请求客户端做了下封装：HurlStack，也就是说可以支持HttpUrlConnection, HttpClient, OkHttp，相当于模版模式吧，这样解耦还是非常方便的，可以随意切换，如果你之前使用过Volley，并习惯使用，那直接写个OkHttp扩展就行了
  * 缺点：
     1. 没有同步
     2. 不能post大数据，所以不适合用来上传大文件（同时上传多个文件时会出内存溢出，现在项目里已经改成用okhttp来上传了，妥妥的解决问题）
* 与afinal对比
  其实FinalHttp也挺好的，但是afinal是一个聚合型的框架，越是大而全，越容易牵一发而动全身，而且本人以前的项目中没用过afinal，就设计了小而精的volley到新的项目中，
  为了以前项目组的成员能快速上手，我采用了FinalHttp的模式构建的中间层，在使用方式上他们没感觉到更以前有什么区别，不过底层确实是换了。
* 踩过的坑（文件上传内存溢出等，统一错误处理，服务器header处理）
  1. 文件上传服务器时出现了 java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Try again at java.lang.Thread.nativeCreate(Native Method) at java.lang.Thread.start(Thread.java:1063) at com.android.volley.RequestQueue.start(Unknown Source)
  最终改成了用okHttp来上传文件，后面会把volley和okHttp结合起来使用。
  2. 服务器返回会出现各种各样的问题，所以最终用了VolleyErrorHelper来统一处理错误信息，可以控制错误信息toast输出到界面上。

### okHttp
* 简介
okHttp是基于http协议封装的一套http客户端，它跟HttpClient和HttpUrlConnection的职责是一样的。不要拿它和Volley相提并论。
而且android6.0之后就没有HttpClient了，因为6.0把org.apache.http.legacy这个包去掉，所以api23时gradle会用useLibrary 'org.apache.http.legacy'。
我们可以使用Volley作为http框架，把okHttp当做http实现的底层使用。
okhttp是高性能的http库，支持同步、异步，而且实现了spdy、http2、websocket协议，api很简洁易用，和volley一样实现了http协议的缓存。picasso就是利用okhttp的缓存机制实现其文件缓存，实现的很优雅，很正确，反例就是UIL（universal image loader），自己做的文件缓存，而且不遵守http缓存机制。

* 优点：
  1. 支持 SPDY ，共享同一个Socket来处理同一个服务器的所有请求
  2. 如果SPDY不可用，则通过连接池来减少请求延时
  3. 无缝的支持GZIP来减少数据流量
  4. 缓存响应数据来减少重复的网络请求
