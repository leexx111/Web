# ElectricityProject

An electrical distribution monitoring and energy management application built with Spring Boot. It collects measurement data from BACnet gateways, stores readings and alarms in MySQL, and serves monitoring, alarm, and energy-statistics pages.

[中文说明](README-cn.md)

## Features

- BACnet gateway discovery and analog-point collection.
- Scheduled collection and persistence of meter, scattered-meter, and filter-bank readings.
- DI trip-alarm polling, alarm history, recovery handling, and WebSocket notifications.
- Distribution-room dashboards, circuit diagrams, power-quality charts, and energy reports.
- Editable energy-statistics tree for the supported scattered-meter groups.

## Technology

- Java 17 or newer at runtime; Maven project configured for Java 8 bytecode.
- Spring Boot 3.3.5, Spring MVC, Thymeleaf, Spring Scheduling and Async.
- MyBatis, Spring Data JPA, and MySQL 8.
- BACnet4J 6 and Modbus4J.
- HTML, JavaScript, Layui, and ECharts frontend assets.

## Requirements

- JDK 17 or newer.
- Maven 3.6 or newer (or the Maven wrapper in a normal Windows/Linux shell).
- MySQL with the `yth` database and the tables expected by the application. This repository does not provide a database dump or schema migration.
- Network access from the application host to the configured BACnet gateways.

## Configuration

Database credentials are supplied through environment variables. Do not commit local credentials.

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | Local MySQL `yth` URL | JDBC connection URL |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | Required | MySQL password |
| `ENERGY_TREE_PASSWORD` | Required for tree edits | Password for saving or resetting the energy tree |

PowerShell example:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/yth?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
$env:DB_USER = "root"
$env:DB_PASSWORD = "<your-password>"
$env:ENERGY_TREE_PASSWORD = "<your-tree-password>"
mvn.cmd spring-boot:run
```

BACnet device IDs, point indices, and local network settings are currently configured in the source. Review them for the target site before running the application.

## Build and run

```bash
mvn -DskipTests package
java -jar target/ElectricityProject-0.0.1-SNAPSHOT.jar
```

The web application listens on port `8081` by default.

## Main source layout

```text
src/main/java/com/cms/electricityproject/
  config/       BACnet access, scheduled jobs, and application configuration
  controller/   REST endpoints and page routing
  dao/          MyBatis SQL mappings and providers
  domain/       Persistence and request models
  server/       Measurement, alarm, and energy-statistics business logic
  websoket/     WebSocket alarm notifications
src/main/resources/
  templates/    Thymeleaf pages and electrical diagrams
  static/       Frontend libraries and application assets
```

The main data-collection task runs every ten minutes; the DI alarm poll runs every five seconds. Runtime behavior depends on the MySQL schema and reachability/configuration of the site gateways.
