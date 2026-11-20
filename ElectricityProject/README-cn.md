# 电力能源管理系统

本项目是基于 Spring Boot 的配电监控与能耗管理系统。系统从 BACnet 网关采集电气测量数据，将数据和报警记录保存到 MySQL，并提供运行监控、报警查询和能耗统计页面。

[English README](README.md)

## 功能概览

- BACnet 网关发现及模拟量点位采集。
- 定时采集电表、散表和滤波器数据并保存到数据库。
- DI 跳闸报警轮询、报警记录、恢复处理及 WebSocket 推送。
- 配电室运行页面、主接线图、电能质量图表和能耗报表。
- 支持指定散表分组的能耗统计树编辑。

## 技术栈

- 运行环境要求 JDK 17 或更高版本；Maven 编译目标为 Java 8 字节码。
- Spring Boot 3.3.5、Spring MVC、Thymeleaf、定时任务和异步任务。
- MyBatis、Spring Data JPA、MySQL 8。
- BACnet4J 6、Modbus4J。
- HTML、JavaScript、Layui、ECharts。

## 运行要求

- JDK 17 或更高版本。
- Maven 3.6 或更高版本，也可以使用 Maven Wrapper。
- MySQL 中需要预先准备 `yth` 数据库及项目所需数据表。本仓库不包含数据库备份或建表迁移脚本。
- 应用主机需要能够访问现场 BACnet 网关。

## 配置

数据库凭据通过环境变量提供，请勿将本地凭据提交到仓库。

| 环境变量 | 默认值 | 用途 |
| --- | --- | --- |
| `DB_URL` | 本机 MySQL `yth` 连接地址 | JDBC 连接地址 |
| `DB_USER` | `root` | MySQL 用户名 |
| `DB_PASSWORD` | 必填 | MySQL 密码 |
| `ENERGY_TREE_PASSWORD` | 编辑树时必填 | 保存或重置能耗树所需口令 |

PowerShell 启动示例：

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/yth?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
$env:DB_USER = "root"
$env:DB_PASSWORD = "<你的数据库密码>"
$env:ENERGY_TREE_PASSWORD = "<能耗树编辑口令>"
mvn.cmd spring-boot:run
```

BACnet 设备号、点位索引和局域网参数目前配置在源码中。部署到现场前，请核对这些配置是否匹配目标站点。

## 构建与启动

```bash
mvn -DskipTests package
java -jar target/ElectricityProject-0.0.1-SNAPSHOT.jar
```

Web 服务默认监听 `8081` 端口。

## 源码结构

```text
src/main/java/com/cms/electricityproject/
  config/       BACnet 通信、定时任务和应用配置
  controller/   REST 接口和页面路由
  dao/          MyBatis SQL 映射及 SQL 提供器
  domain/       数据库实体和请求模型
  server/       采集数据、报警和能耗统计业务逻辑
  websoket/     WebSocket 报警通知
src/main/resources/
  templates/    Thymeleaf 页面和配电图
  static/       前端依赖库及应用静态资源
```

常规采集任务每十分钟运行一次，DI 报警轮询每五秒运行一次。实际运行还依赖数据库结构以及现场网关的网络连通和点表配置。
