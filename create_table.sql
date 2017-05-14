-- 导出 diamond 的数据库结构
CREATE DATABASE IF NOT EXISTS `diamond` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `diamond`;


-- 导出  表 diamond.config_info 结构
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


-- 导出  表 diamond.group_info 结构
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