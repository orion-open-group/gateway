/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- 导出 gateway 的数据库结构
CREATE DATABASE IF NOT EXISTS `gateway` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `gateway`;

-- 导出  表 gateway.app_info 结构
CREATE TABLE IF NOT EXISTS `app_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_id` varchar(128) DEFAULT NULL COMMENT 'appId',
  `app_name` varchar(255) DEFAULT NULL COMMENT 'APP名字',
  `app_alias` varchar(255) DEFAULT NULL COMMENT '应用别名-服务透传以此名为准',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `is_deleted` varchar(255) DEFAULT NULL COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `creator` varchar(512) DEFAULT NULL COMMENT '创建人',
  `owner` varchar(512) NOT NULL COMMENT '所有人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `alias_name` (`app_alias`),
  UNIQUE KEY `app_id` (`app_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。

-- 导出  表 gateway.service_info 结构
CREATE TABLE IF NOT EXISTS `service_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `service_alias` varchar(96) DEFAULT NULL COMMENT '服务别名，保持唯一性',
  `service_type` varchar(96) DEFAULT NULL COMMENT '服务类型，目前只有DUBBO，后续可增加RMB',
  `service_version` varchar(96) DEFAULT NULL COMMENT '服务版本号，外部调用时需带入',
  `service_config` varchar(4096) DEFAULT NULL COMMENT '服务配置',
  `description` varchar(512) DEFAULT NULL COMMENT '服务描述',
  `app_id` varchar(128) DEFAULT NULL COMMENT '所属应用ID',
  `app_alias` varchar(128) DEFAULT NULL COMMENT '所属应用名称',
  `is_valid` tinyint(20) DEFAULT NULL COMMENT '是否有效',
  `is_deleted` tinyint(20) DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `path` varchar(512) DEFAULT NULL COMMENT '匹配路径-未启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_alias_is_deleted_idx` (`service_alias`,`service_version`,`app_id`) USING BTREE,
  UNIQUE KEY `path` (`path`)
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。

-- 导出  表 gateway.service_invoke_log 结构
CREATE TABLE IF NOT EXISTS `service_invoke_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app` varchar(255) DEFAULT NULL COMMENT '服务域',
  `service` varchar(255) DEFAULT NULL COMMENT '服务',
  `method` varchar(255) DEFAULT NULL COMMENT '方法',
  `token` varchar(255) DEFAULT NULL COMMENT '用户token',
  `user_id` varchar(96) DEFAULT NULL COMMENT '用户id',
  `invoke_time` datetime DEFAULT NULL COMMENT '日志打印时间',
  `use_time` int(11) DEFAULT NULL COMMENT '耗时',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `device_info` varchar(512) DEFAULT NULL COMMENT '设备识别信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8298 DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。

-- 导出  表 gateway.service_method_info 结构
CREATE TABLE IF NOT EXISTS `service_method_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_id` bigint(20) DEFAULT NULL COMMENT 'rpc调用中dubbo服务的别名',
  `method_alias` varchar(128) DEFAULT NULL COMMENT '所能调用的方法的别名',
  `method_name` varchar(128) DEFAULT NULL COMMENT '方法的名字',
  `param_fields` varchar(4096) DEFAULT NULL COMMENT '用json表达方法中传递的参数的数据类型和参数名',
  `return_config` varchar(1024) DEFAULT NULL COMMENT '返回值映射',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `is_valid` tinyint(1) DEFAULT NULL COMMENT '是否有效',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `service_method_is_deleted_idx` (`service_id`,`method_alias`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=464 DEFAULT CHARSET=utf8;

-- 数据导出被取消选择。

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
