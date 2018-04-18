#### 项目介绍
基于Spring+SpringMvc+Mybatis敏捷开发系统架构，提供了电商平台基础模块：用户管理、商品管理、购物车、订单管理、收获地址、在线支付
#### 组织结构
![mark](http://upload.i20forever.cn/blog/180418/gJfjH7bD4k.png?imageslim)

#### 技术选型
##### 后端技术

技术 | 名称 | 官网
---|---|---
Spring Framework | 容器 | 	http://projects.spring.io/spring-framework/
SpringMVC | MVC框架 | http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#mvc
Spring session | 分布式Session管理 | 	http://projects.spring.io/spring-session/
MyBatis | ORM框架 | http://www.mybatis.org/mybatis-3/zh/index.html
MyBatis Generator | 代码生成 | http://www.mybatis.org/generator/index.html
PageHelper | MyBatis物理分页插件 | http://git.oschina.net/free/Mybatis_PageHelper
c3p0 | 数据库连接池 | http://www.mchange.com/projects/c3p0/
Redis | 分布式缓存数据库 | https://redis.io/
Logback | 日志组件 | https://logback.qos.ch/
Swagger2 | 接口测试框架 | http://swagger.io/
Guava | 谷歌工具类 | https://github.com/google/guava
Joda-Time | 时间管理 | http://www.joda.org/joda-time/
Maven | 项目构建管理 | http://maven.apache.org/
Git | 分布式版本控制 | https://git-scm.com/


#### 架构图
![mark](http://upload.i20forever.cn/blog/180418/0j94JhCLiL.png?imageslim)

#### 项目部署
 - 安装git客户端，克隆项目

```
git clone https://github.com/zhonglunsheng/mmall.git
```
 - 修改配置文件 *datasource.properties mmall.properties zfbinfo.properties*

- maven打包

```
mvn clean package -Dmaven.skip.test=true -Pprod
```

 - 发布war包

#### 演示地址


#### 部分截图
