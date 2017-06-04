Diamond -- 分布式配置中心
========================

# 一、简介
* Diamond是淘宝研发的分布式配置管理系统。使用Diamond可以让集群中的服务进程动态感知数据的变化，无需重启服务就可以实现配置数据的更新。
* 具有简单、可靠、易用等特点

# 二、使用方法
## 服务端搭建
## 1 准备工作
* 安装jdk
* 安装maven
* 安装tomcat
* 安装mysql
## 2 启动mysql并创建数据库和表

```
-- 创建Diamond数据库
CREATE DATABASE IF NOT EXISTS `diamond` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `diamond`;


-- 配置表
CREATE TABLE IF NOT EXISTS `config_info` (
  `id` bigint(64) unsigned NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) NOT NULL DEFAULT '',
  `group_id` varchar(128) NOT NULL DEFAULT '',
  `content` longtext NOT NULL,
  `md5` varchar(32) NOT NULL DEFAULT '',
  `src_ip` varchar(20) DEFAULT NULL,
  `src_user` varchar(20) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT '2010-05-05 00:00:00',
  `gmt_modified` datetime NOT NULL DEFAULT '2010-05-05 00:00:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_datagroup` (`data_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。


-- 组表
CREATE TABLE IF NOT EXISTS `group_info` (
  `id` bigint(64) unsigned NOT NULL AUTO_INCREMENT,
  `address` varchar(70) NOT NULL DEFAULT '',
  `data_id` varchar(255) NOT NULL DEFAULT '',
  `group_id` varchar(128) NOT NULL DEFAULT '',
  `src_ip` varchar(20) DEFAULT NULL,
  `src_user` varchar(20) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT '2010-05-05 00:00:00',
  `gmt_modified` datetime NOT NULL DEFAULT '2010-05-05 00:00:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_address` (`address`,`data_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```
## 3 下载源码
```
git clone https://github.com/gzllol/diamond.git
```
## 4 打包
1. 修改diamond-server/src/main/resources/system.properties文件，将diamond.server.addr的值换成Diamond服务器所在机器的ip地址
2. 修改diamond-server/src/main/resources/jdbc.properties文件，配置mysql服务器的url，用户名和密码
2. 在根目录执行打包命令

```
mvn clean package -Dmaven.test.skip=true
```

## 5 用tomcat加载diamond-server/target/diamond-server.war

## 客户端使用
## 1 将diamond-client jar包发布到maven仓库
```
mvn clean deploy -Dmaven.test.skip=true
```
## 2 在工程中引入jar包

```
<dependency>
    <groupId>com.taobao.diamond</groupId>
    <artifactId>diamond-client</artifactId>
    <version>2.0.5.4.taocode-SNAPSHOT</version>
</dependency>
```
## 3 使用例子

```
        DiamondManager manager = new DefaultDiamondManager("${your_config_data_id}", new ManagerListener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                System.out.println("receive config: " + configInfo);
            }
        });
```

## 4 在配置中心界面添加一个配置
1. 登陆配置中心（本机是127.0.0.1:8080），用户名abc，密码123
2. 点击左侧“配置信息管理”
3. 点击添加配置信息
4. 输入data_id（就是配置的id，3中的${your_config_data_id}）和配置内容
5. 点击“提交”
6. 在更新配置时，客户端会调用ManagerListener的回调函数receiveConfigInfo，参数就是最新的配置内容
