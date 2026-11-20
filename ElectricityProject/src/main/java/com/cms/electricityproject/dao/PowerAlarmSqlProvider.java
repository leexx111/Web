// PowerAlarmSqlProvider.java
package com.cms.electricityproject.dao;

import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * 按查询条件动态生成报警与能耗统计 SQL。
 */
public class PowerAlarmSqlProvider {
    
    // 方法名应与 @SelectProvider 中指定的 method 一致
    public String queryAlarms(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM power_alarm WHERE 1=1");
        
        // 处理开始日期
        if (params.get("startDate") != null) {
            sql.append(" AND create_time >= #{startDate}");
        }
        
        // 处理结束日期
        if (params.get("endDate") != null) {
            sql.append(" AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)");
        }
        
        // 处理报警级别1（跳过"全部"选项）
        String alarmLevel1 = (String) params.get("alarmLevel1");
        if (alarmLevel1 != null && !"全部".equals(alarmLevel1)) {
            sql.append(" AND alarm_title = #{alarmLevel1}");
        }
        
        // 处理报警级别2
        String alarmLevel2 = (String) params.get("alarmLevel2");
        if (alarmLevel2 != null && !"全部".equals(alarmLevel2)) {
            sql.append(" AND alarm_level = #{alarmLevel2}");
        }
        
        // 处理设备
        String device = (String) params.get("device");
        if (device != null && !"全部".equals(device)) {
            sql.append(" AND alarm_name = #{device}");
        }
        
        // 处理状态
        String status = (String) params.get("status");
        if (status != null && !"全部".equals(status)) {
            sql.append(" AND alarm_status = #{status}");
        }

        sql.append(" ORDER BY create_time DESC, id DESC ");
        return sql.toString();
    }

    // 生成查询多个名称在指定日期的ADSP总和的SQL
    public String sumAdspByNamesAndDate(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");
        String date = (String) params.get("date"); // 格式应为：yyyy-MM-dd

        // 构建基础SQL，使用小时维度分组
        return new SQL() {{
            // 使用DATE_FORMAT提取小时，统计每个小时的ADSP总和
            SELECT("HOUR(d_time) AS hour, COALESCE(ROUND(SUM(ygdl), 2), 0) AS adsp_sum");
            FROM("dianliang");
            // 时间条件：精确到指定日期
            WHERE("DATE(d_time) = #{date}");
            // 处理多个名称的IN条件
            if (names != null && !names.isEmpty()) {
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) inClause.append(", ");
                    inClause.append("#{names[").append(i).append("]}");
                }
                WHERE("`name` IN (" + inClause + ")");
            }
            // 按小时分组
            GROUP_BY("HOUR(d_time)");
            ORDER_BY("HOUR(d_time)");
        }}.toString();
    }

    // 生成查询多个名称在指定日期的ADSP总和的SQL
    public String sumAdspByNamesAndmonth(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");
        String date = (String) params.get("date"); // 格式应为：yyyy-MM-dd

        // 构建基础SQL，使用小时维度分组
        return new SQL() {{
            // 使用DATE_FORMAT提取小时，统计每个小时的ADSP总和
            SELECT("DATE(d_time) AS hour, COALESCE(ROUND(SUM(ygdl), 2), 0) AS adsp_sum");
            FROM("dianliang");
            // 时间条件：精确到指定日期
            WHERE("DATE_FORMAT(d_time, '%Y-%m') = #{date}");
            // 处理多个名称的IN条件
            if (names != null && !names.isEmpty()) {
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) inClause.append(", ");
                    inClause.append("#{names[").append(i).append("]}");
                }
                WHERE("`name` IN (" + inClause + ")");
            }
            // 按小时分组
            GROUP_BY("DATE(d_time)");
            ORDER_BY("DATE(d_time)");
        }}.toString();
    }
    // 生成查询多个名称在指定日期的ADSP总和的SQL
    public String sumAdspByNamesAndyear(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");
        String date = (String) params.get("date"); // 格式应为：yyyy-MM-dd

        // 构建基础SQL，使用小时维度分组
        return new SQL() {{
            // 使用DATE_FORMAT提取小时，统计每个小时的ADSP总和
            SELECT("DATE_FORMAT(d_time, '%Y-%m') AS hour, COALESCE(ROUND(SUM(ygdl), 2), 0) AS adsp_sum");
            FROM("dianliang");
            // 时间条件：精确到指定日期
            WHERE("YEAR(d_time) = #{date}");
            // 处理多个名称的IN条件
            if (names != null && !names.isEmpty()) {
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) inClause.append(", ");
                    inClause.append("#{names[").append(i).append("]}");
                }
                WHERE("`name` IN (" + inClause + ")");
            }
            // 按小时分组
            GROUP_BY("DATE_FORMAT(d_time, '%Y-%m')");
            ORDER_BY("DATE_FORMAT(d_time, '%Y-%m')");
        }}.toString();
    }

    public String sumAdspByNamesAndRange(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        return new SQL() {{
            SELECT("DATE(d_time) AS hour, COALESCE(ROUND(SUM(ygdl), 2), 0) AS adsp_sum");
            FROM("dianliang");
            WHERE("DATE(d_time) BETWEEN #{startDate} AND #{endDate}");
            if (names != null && !names.isEmpty()) {
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) inClause.append(", ");
                    inClause.append("#{names[").append(i).append("]}");
                }
                WHERE("`name` IN (" + inClause + ")");
            }
            GROUP_BY("DATE(d_time)");
            ORDER_BY("DATE(d_time)");
        }}.toString();
    }

    private String buildNamesInClause(List<String> names) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                inClause.append(", ");
            }
            inClause.append("#{names[").append(i).append("]}");
        }
        return inClause.toString();
    }

    public String getDLjfpgBydateAndNames(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        return "SELECT " +
                "SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian, " +
                "SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng, " +
                "SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping, " +
                "SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu " +
                "FROM dianliang " +
                "WHERE name IN (" + buildNamesInClause(names) + ") " +
                "AND DATE(d_time) = #{date}";
    }

    public String getDLjfpgBymonthAndNames(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        return "SELECT " +
                "SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian, " +
                "SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng, " +
                "SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping, " +
                "SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu " +
                "FROM dianliang " +
                "WHERE name IN (" + buildNamesInClause(names) + ") " +
                "AND DATE_FORMAT(d_time, '%Y-%m') = #{date}";
    }

    public String getDLjfpgByyearAndNames(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        return "SELECT " +
                "SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian, " +
                "SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng, " +
                "SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping, " +
                "SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu " +
                "FROM dianliang " +
                "WHERE name IN (" + buildNamesInClause(names) + ") " +
                "AND YEAR(d_time) = #{date}";
    }

    public String getDLjfpgBycustomAndNames(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        return "SELECT " +
                "SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian, " +
                "SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng, " +
                "SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping, " +
                "SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu " +
                "FROM dianliang " +
                "WHERE name IN (" + buildNamesInClause(names) + ") " +
                "AND DATE(d_time) BETWEEN #{startDate} AND #{endDate}";
    }

    /**
     * 生成查询多个name的两条时间数据的SQL
     */
    public String getMultiNameTimeData(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");
        String date = (String) params.get("date");

        // 构建IN条件
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                inClause.append(", ");
            }
            inClause.append("#{names[").append(i).append("]}");
        }

        // 构建SQL，查询每个name的两条最新时间数据
        return new SQL() {{
            SELECT("`name`, ADSP, `data` as time");
            FROM("ytmem");
            WHERE("`name` IN (" + inClause + ")");
            WHERE("DATE(`data`) = #{date}");
            // 按name分组，每组取两条数据（这里取最新的两条）
            // 注意：不同数据库取前N条语法可能不同，这里以MySQL为例
            ORDER_BY("`name`, `data` DESC");
        }}.toString() + " LIMIT " + (names.size() * 2); // 总记录数为name数量×2
    }

    public String batchGetNhtj(Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                inClause.append(", ");
            }
            inClause.append("#{names[").append(i).append("]}");
        }

        StringBuilder orderClause = new StringBuilder("FIELD(`name`, ");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                orderClause.append(", ");
            }
            orderClause.append("#{names[").append(i).append("]}");
        }
        orderClause.append(")");

        String startSubquery = "SELECT y.name, y.ADSP AS startValue, NULL AS endValue " +
                "FROM ytmem y " +
                "WHERE y.name IN (" + inClause + ") AND y.data = #{startDate}";

        String endSubquery = "SELECT y.name, NULL AS startValue, y.ADSP AS endValue " +
                "FROM ytmem y " +
                "WHERE y.name IN (" + inClause + ") AND y.data = #{endDate}";

        return "SELECT result.name, " +
                "MAX(result.startValue) AS startValue, " +
                "MAX(result.endValue) AS endValue " +
                "FROM (" + startSubquery + " UNION ALL " + endSubquery + ") result " +
                "GROUP BY result.name " +
                "ORDER BY " + orderClause;
    }

}
