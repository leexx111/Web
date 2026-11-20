# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An electrical power distribution monitoring and management system (电力配电监控管理系统) for a facility with multiple distribution rooms (配电室 #1-#7). Built with Spring Boot, it collects real-time electrical data from BACnet field devices, stores it in MySQL, calculates energy billing, detects alarms (trip, over-temperature, overload, voltage, capacitor imbalance), and serves a web dashboard with Thymeleaf templates.

## Build & Run

- **JDK**: 17 (language level set to JDK 1.8 in IDE, but runtime is JDK 17)
- **Build tool**: Maven (wrapper at `.mvn/`). No `pom.xml` on disk — the IDE manages dependencies via `ExternalStorageConfigurationManager`. Re-import from `.idea/misc.xml` if needed.
- **Run**: `./mvnw spring-boot:run` or run `ElectricityProjectApplication.main()` from IDE
- **Server port**: `8081` (configured in `src/main/resources/application.properties`)
- **Database**: MySQL 8.x, database `yth`. Connection config is in `application.properties` — localhost by default, with commented-out remote/alternative profiles.

## Architecture

```
src/main/java/com/cms/electricityproject/
├── ElectricityProjectApplication.java   # Entry point, enables @EnableScheduling, @EnableAsync
├── config/
│   ├── BacnetTool.java                  # Core: BACnet/IP device communication (bacnet4j lib)
│   ├── ScheduledTasks.java              # Timed data collection + alarm monitoring loops
│   ├── AsyncConfig.java                 # Thread pool config (asyncTaskExecutor, capCurrentTaskExecutor)
│   ├── MyMvcConfig.java                 # Static resource paths, CORS
│   ├── MyRunner.java                    # CommandLineRunner placeholder
│   └── HttpGetUtils.java               # Simple HTTP GET utility
├── controller/
│   ├── UserController.java              # Main REST API (/hdb/**): power data, alarms, billing stats
│   ├── fhsbController.java              # BACnet real-time data API + energy billing aggregation
│   └── dljksController.java             # Page router (Thymeleaf templates under /dljk/)
├── server/
│   └── UserService.java                 # ~5000-line business logic: data ingestion, alarm handling, billing
├── dao/
│   ├── HDBDao.java                      # MyBatis mapper: all SQL queries for ytmem, dianliang, power_alarm
│   ├── UserRepository.java              # Spring Data JPA repository (heidb table, minimal usage)
│   └── PowerAlarmSqlProvider.java       # Dynamic SQL builder for alarm queries and energy aggregation
├── domain/
│   ├── Ytmem.java                       # Main electrical measurement entity (voltage, current, power, energy)
│   ├── Heidb.java                       # Legacy/high-voltage measurement entity
│   ├── Dianliang.java                   # Hourly energy/electricity billing record
│   └── PowerAlarm.java                  # Alarm event entity
├── websoket/                            # Note: typo in package name (websoket, not websocket)
│   ├── WebSocketConfig.java             # Registers /ws endpoint
│   └── MyWebSocketHandler.java          # Broadcasts alarm events (JSON via Jackson) to web clients
└── pachong/
    └── NotionCrawler.java               # Selenium web scraper (experimental, not core)
```

## Key Data Flow

### Data Ingestion (every 10 minutes)
1. `ScheduledTasks.dianfei()` calls `BacnetTool.Hqsj()` which reads all analog input points from BACnet gateways (devices 301–325) via the bacnet4j library.
2. Raw readings are grouped by device name (e.g., `1AA1-1.A` → name `1AA1-1`, property `A`), normalized (CT ratio, scaling factors), and inserted into `ytmem` table.
3. At the top of each hour (`minute == "00"`) an additional `dianliang` record is created with peak/flat/valley tier classification (尖峰平谷 1-4) and electricity cost calculated per seasonal pricing rules.

### Alarm Detection (every 10 seconds)
- **Trip alarm**: DI (Digital Input) state changes detected in `ScheduledTasks.KGbaojing()` — compares current DI1 values against `QJmap` cache.
- **Transformer temp alarm**: `ScheduledTasks.byqWenduBaojing()` reads temperature from device 300/319/325 analog points.
- **Power quality alarms**: `powerQualityAlarmTask()` (every 30s) checks capacitor current unbalance, transformer overload, and voltage anomalies — with debounce logic (re-check after 10s, requires 2 consecutive abnormal readings).

All alarms are persisted to `power_alarm` table and broadcast via WebSocket.

### Device Naming Convention
Electrical points follow the pattern `<cabinet>-<breaker>.<property>`, e.g. `1AA1-1.Ia` = distribution room 1, cabinet AA1, breaker 1, phase A current. High-voltage points use `AH<n>` prefix.

## Database Tables

| Table | Purpose |
|-------|---------|
| `ytmem` | Time-series electrical measurements (every 10 min per point) |
| `dianliang` | Hourly energy consumption with peak/valley/flat tier and cost |
| `power_alarm` | Alarm events with status (0=unconfirmed, 1=confirmed, 2=recovered) |
| `heidb` | High-voltage measurement data |
| `gaoyashuju` | High-voltage analog data from device 300 |
| `region_one_summary` | Aggregated per-distribution-room summary |
| `lbg` | Filter/reactor bank (滤波器) measurements |

## Thread Safety Notes

- `BacnetTool` is a singleton Spring bean. Its `Hqsj()` and `baojing()` methods use `synchronized(this)` for BACnet communication serialization.
- `ScheduledTasks` uses `AtomicBoolean` guards to prevent overlapping executions of the same scheduled method.
- `MyWebSocketHandler` uses `ConcurrentHashMap` for session management.

## Important Details

- The `UserService` is the god class — nearly all business logic lives here (data transformation, alarm state machines, billing calculation, query aggregation). When adding features, consider extracting new service classes rather than extending it further.
- The `UserController` route is `/hdb` not `/users` — confusing naming, the controller handles all power data APIs, not just user CRUD.
- BACnet device IDs are hardcoded in `BacnetTool` and `ScheduledTasks`. Adding a new distribution room requires updating these classes plus the HDBDao SQL queries with new device name lists.
- Electricity pricing follows China's time-of-use tariff: 尖(peak)/峰(high)/平(flat)/谷(valley) tiers with seasonal variations (Jan/Dec, Jul/Aug, Feb-Jun+Sep-Nov).
- The `websoket` package has a typo — be consistent with it when adding new files.
