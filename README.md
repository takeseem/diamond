diamond
=======

来自淘宝diamond：http://code.taobao.org/p/diamond/src/

diamond设计上的一些问题？
==
1. com.taobao.diamond.common.Constants.CONFIG_HTTP_URI_FILE，获取ServerAddress的值，当前没实现他们的，所以必须自己配置
1. 目前必须在：~/diamond/ServerAddress文件中配置diamond的服务器地址
1. 原来很多都写在Constants中是final的，所以提供了：-D > env > diamond.properties方式来配置一些值

client使用
==
* classpath:diamond.properties
 1. HTTP_URI_FILE=http://10.200.51.105:9900/diamond-server/config.co 配置获取dataId数据的url
