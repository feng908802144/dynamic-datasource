# starsray-dynamic-datasource

#### 介绍
本项目整合开源框架封装了动态数据源的一些基本功能,主要包含以下功能点:
- 基于mybatis-plus实现数据源的动态添加移除;
- 基于AOP切面或者自定义DsProcessor解析实现数据源动态切换;
- 基于flyway实现SQL脚本管理;
- 支持添加数据源时初始化库表结构,支持SQL脚本同步变更到多个schema用户;

#### 软件架构
软件架构说明
- 后端 SpringBoot 2.3.7
- 控制台 Vue2.x + ElementUI
- 接口文档 Swagger + Knife4j
#### 安装教程

1. 后端项目 git@gitee.com:starsray/starsray-dynamic-datasource.git
2. 前端项目 git@gitee.com:starsray/starsray-dynamic-datasource-front.git

#### 其他
- mysql测试数据：https://www.cnblogs.com/starsray/p/14760757.html
- flyway 
  - API说明
  - SQL版本规范说明
  - 参考文档

