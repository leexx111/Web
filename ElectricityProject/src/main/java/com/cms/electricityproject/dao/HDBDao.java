package com.cms.electricityproject.dao;


import com.cms.electricityproject.domain.Heidb;
import com.cms.electricityproject.domain.Ytmem;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.cms.electricityproject.domain.PowerAlarm;


@Mapper
/**
 * 集中定义电气测量、能耗和报警记录的数据访问 SQL。
 */
public interface HDBDao {


    @Select("SELECT * FROM heidb")
     List<Heidb> getNow();

    @Select("SELECT * FROM Ytmem")
     List<Ytmem> getYtmemAll();

    //负荷获取上月的平均值
    @Select("-- 生成上月所有日期\n" +
            "WITH RECURSIVE last_month_dates AS (\n" +
            "    SELECT DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m-01') AS date  -- 上月第一天\n" +
            "    UNION ALL\n" +
            "    SELECT DATE_ADD(date, INTERVAL 1 DAY)\n" +
            "    FROM last_month_dates\n" +
            "    WHERE date < LAST_DAY(CURDATE() - INTERVAL 1 MONTH)  " +
            ")\n" +
            "\n" +
            "-- 计算每日平均值\n" +
            "SELECT \n" +
            "--     d.date AS 日期,\n" +
            "    ROUND(IFNULL(AVG(r.fh), 0), 2) AS 平均fh值\n" +
            "FROM last_month_dates d\n" +
            "LEFT JOIN region_one_summary r \n" +
            "    ON DATE(r.`data`) = d.date\n" +
            "    AND r.`data` BETWEEN DATE_SUB(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 1 MONTH) \n" +
            "                     AND LAST_DAY(CURDATE() - INTERVAL 1 MONTH) " +
            "  AND name = #{name}\n" +
            "GROUP BY d.date\n" +
            "ORDER BY d.date;")
    List<Double> getByLastMonthMeanW(String name);

    //负荷获取本月的平均值
    @Select("-- 生成当月所有日期\n" +
            "WITH RECURSIVE dates AS (\n" +
            "    SELECT DATE_FORMAT(CURDATE() - INTERVAL (DAY(CURDATE()) - 1) DAY, '%Y-%m-%d') AS date\n" +
            "    UNION ALL\n" +
            "    SELECT DATE_ADD(date, INTERVAL 1 DAY)\n" +
            "    FROM dates\n" +
            "    WHERE DATE_ADD(date, INTERVAL 1 DAY) <= LAST_DAY(CURDATE())\n" +
            ")\n" +
            "\n" +
            "-- 关联实际数据计算平均值\n" +
            "SELECT \n" +
            "--     d.date AS 日期,\n" +
            "    ROUND(IFNULL(AVG(r.fh), 0), 2) AS 平均fh值\n" +
            "FROM dates d\n" +
            "LEFT JOIN region_one_summary r \n" +
            "    ON DATE(r.data) = d.date\n" +
            "    AND YEAR(r.data) = YEAR(CURDATE())  -- 确保只查询本年度数据\n" +
            "GROUP BY d.date\n" +
            "ORDER BY d.date;")
    List<Double> getByThisMonthMeanW();

    //负荷获取本月的电量平均值
    @Select("SELECT  ROUND(SUM(region_one_summary.dl), 2) AS avg_w\n" +
            "FROM region_one_summary\n" +
            "WHERE YEAR(region_one_summary.data) = YEAR(CURDATE())\n" +
            "  AND MONTH(region_one_summary.data) = MONTH(CURDATE())\n" +
            "AND region_one_summary.name = #{name} " +
            "GROUP BY \n" +
            "    DATE(region_one_summary.data)\n" +
            "ORDER BY \n" +
            "    DATE(region_one_summary.data);")
    List<Double> getByLastMonthMeanEQ(String name);

    //最新的负荷值
    @Select("SELECT fh from region_one_summary WHERE `name` =#{name}  ORDER BY data DESC\n" +
            "            LIMIT 1;")
    String getWnew(String name);

    //本日最高的负荷值
    @Select(" SELECT  MAX(fh) from region_one_summary WHERE `name` =#{name} AND  DATE(data) = (SELECT DATE(MAX(data)) FROM region_one_summary);")
    String getTodayMax(String name);

    //本月最高的负荷值
    @Select("SELECT  MAX(fh) from region_one_summary WHERE `name` =#{name} AND  YEAR(data) = YEAR(CURDATE())  AND MONTH(data) = MONTH(CURDATE());")
    String getmonthMax(String name);

    //本年的最高负荷
    @Select("SELECT  MAX(fh) from region_one_summary WHERE `name` =#{name} AND  YEAR(data) = YEAR(CURDATE())")
    String getByYearMax(String name);

    //本年的最低负荷
        @Select("SELECT  MIN(fh) from region_one_summary WHERE `name` =#{name} AND  YEAR(data) = YEAR(CURDATE())")
    String getByYearMin(String name);


    //获取最新的总电量
    @Select("SELECT SUM(ADSP) \n" +
            "FROM ytmem \n" +
            "WHERE name IN ('1AA1-1', '2AA15-1', '3AA1-1', '4AA17-1') \n" +
            "  AND `data` = (\n" +
            "      SELECT MAX(`data`) \n" +
            "      FROM ytmem \n" +
            "      WHERE name IN ('1AA1-1', '2AA15-1', '3AA1-1', '4AA17-1')\n" +
            "  );")
    Double getzdl();
//    @Select("  SELECT SUM(ADSP) AS ADSP \n" +
//            "FROM (\n" +
//            "    SELECT \n" +
//            "        ADSP,\n" +
//            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data DESC) AS rn\n" +
//            "    FROM ytmem\n" +
//            "    WHERE name IN (\"1AA1-1\", \"2AA15-1\", \"3AA1-1\", \"4AA17-1\")\n" +
//            ") t\n" +
//            "WHERE rn = 1; -- 只保留每个名称的最新记录（rn=1表示最新）")
//    Double getzdl();
    //获取2号配最新的总电量
    @Select("  SELECT SUM(ADSP) AS ADSP \n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        ADSP,\n" +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data DESC) AS rn\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN (\"2-1AA1-1\", \"2-2AA14-1\", \"2-3AA1-1\", \"2-4AA14-1\")\n" +
            ") t\n" +
            "WHERE rn = 1; -- 只保留每个名称的最新记录（rn=1表示最新）")
    Double getTwozdl();
    @Select("  SELECT SUM(ADSP) AS ADSP \n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        ADSP,\n" +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data DESC) AS rn\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN (\"6-1AA1-1\", \"6-2AA11-1\")\n" +
            ") t\n" +
            "WHERE rn = 1; -- 只保留每个名称的最新记录（rn=1表示最新）")
    Double getSixzdl();
    //获取本月1号最新的的总电量
    @Select("SELECT SUM(ADSP) AS ADSP \n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        ADSP,\n" +
            "        -- 按时间升序排序，最早的记录行号为1\n" +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
            "    FROM ytmem\n" +
            "    WHERE \n" +
            "        name IN (${cmsliststr})\n" +
            "        -- 过滤本月数据（兼容大多数数据库）\n" +
            "        AND data >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')\n" +
            "        AND data < DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 1 MONTH)\n" +
            ") t\n" +
            "WHERE rn = 1;")
    Double getbyzxdl(String cmsliststr);

    //获取上月1号最新电量
    @Select("SELECT SUM(ADSP) AS ADSP \n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        ADSP,\n" +
            "        -- 按时间升序排序，上月最早的记录行号为1\n" +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
            "    FROM ytmem\n" +
            "    WHERE \n" +
            "        name IN (${cmsliststr})\n" +
            "        -- 过滤上月数据（兼容多数数据库）\n" +
            "        AND data >= DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH), '%Y-%m-01')\n" +
            "        AND data < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')\n" +
            ") t\n" +
            "WHERE rn = 1;")
    Double getsyzxdl(String cmsliststr);
//    @Select("SELECT SUM(ADSP) AS ADSP \n" +
//            "FROM (\n" +
//            "    SELECT \n" +
//            "        ADSP,\n" +
//            "        -- 按时间升序排序，上月最早的记录行号为1\n" +
//            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
//            "    FROM ytmem\n" +
//            "    WHERE \n" +
//            "        name IN (${cmsliststr})\n" +
//            "        -- 过滤上月数据（兼容多数数据库）\n" +
//            "        AND data >= DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH), '%Y-%m-01')\n" +
//            "        AND data < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')\n" +
//            ") t\n" +
//            "WHERE rn = 1;")
//     Double getsyzxdl(String cmsliststr);

    //本年1号电量
//        @Select("SELECT SUM(ADSP) AS ADSP\n" +
//                "FROM ytmem t1\n" +
//                "WHERE \n" +
//                "    name IN (${cmsliststr})\n" +
//                "    AND data >= DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//                "    AND data < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 - MONTH(CURRENT_DATE) MONTH), '%Y-%m-01') + INTERVAL 1 YEAR\n" +
//                "    AND NOT EXISTS (\n" +
//                "        SELECT 1\n" +
//                "        FROM ytmem t2\n" +
//                "        WHERE t2.name = t1.name\n" +
//                "        AND t2.data >= DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//                "        AND t2.data < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 - MONTH(CURRENT_DATE) MONTH), '%Y-%m-01') + INTERVAL 1 YEAR\n" +
//                "        AND t2.data < t1.data\n" +
//                "    )")
//    Double getbnzxdl(String cmsliststr);
//    @Select("\tSELECT SUM(ADSP) AS ADSP \n" +
//            "FROM (\n" +
//            "    SELECT \n" +
//            "        ADSP,\n" +
//            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
//            "    FROM ytmem\n" +
//            "    WHERE \n" +
//            "        name IN (${cmsliststr})\n" +
//            "        -- 提取本年数据\n" +
//            "        AND data >= DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//            "        AND data < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 - MONTH(CURRENT_DATE) MONTH), '%Y-%m-01') + INTERVAL 1 YEAR\n" +
//            ") t\n" +
//            "WHERE rn = 1;")
//    Double getbnzxdl(String cmsliststr);
    @Select("SELECT SUM(ADSP) AS ADSP \n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        ADSP,\n" +
            "        -- 按时间升序排序，本年最早的记录行号为1\n" +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
            "    FROM ytmem\n" +
            "    WHERE \n" +
            "        name IN (${cmsliststr})\n" +
            "        -- 过滤本年数据（兼容多数数据库）\n" +
            "        AND data >= DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
            "        AND data < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 - MONTH(CURRENT_DATE) MONTH), '%Y-%m-01') + INTERVAL 1 YEAR\n" +
            ") t\n" +
            "WHERE rn = 1;")
    Double getbnzxdl(String cmsliststr);
//    @Select("SELECT SUM(ADSP) AS ADSP \n" +
//            "FROM (\n" +
//            "    SELECT \n" +
//            "        ADSP,\n" +
//            "        -- 按时间升序排序，本年最早的记录行号为1\n" +
//            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
//            "    FROM ytmem\n" +
//            "    WHERE \n" +
//            "        name IN (${cmsliststr})\n" +
//            "        -- 过滤本年数据（兼容多数数据库）\n" +
//            "        AND data >= DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//            "        AND data < DATE_FORMAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 - MONTH(CURRENT_DATE) MONTH), '%Y-%m-01') + INTERVAL 1 YEAR\n" +
//            ") t\n" +
//            "WHERE rn = 1;")
//    Double getbnzxdl(String cmsliststr);

    //去年1号电量
//    @Select("SELECT SUM(t1.ADSP) AS ADSP\n" +
//            "FROM ytmem t1\n" +
//            "LEFT JOIN (\n" +
//            "    -- 查找去年每个name的最小日期\n" +
//            "    SELECT \n" +
//            "        name,\n" +
//            "        MIN(data) AS min_data\n" +
//            "    FROM ytmem\n" +
//            "    WHERE \n" +
//            "        name IN (${cmsliststr})\n" +
//            "        AND data >= DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR), '%Y-01-01')\n" +
//            "        AND data < DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//            "    GROUP BY name\n" +
//            ") t2 ON t1.name = t2.name AND t1.data = t2.min_data\n" +
//            "WHERE \n" +
//            "    t1.name IN (${cmsliststr})\n" +
//            "    AND t2.min_data IS NOT NULL;")
//    Double getqnzxdl(String cmsliststr);


//    @Select("SELECT SUM(t1.ADSP) AS ADSP\n" +
//            "FROM ytmem t1\n" +
//            "JOIN (\n" +
//            "    SELECT name, MIN(data) AS min_data\n" +
//            "    FROM ytmem\n" +
//            "    WHERE \n" +
//            "        name IN (${cmsliststr})\n" +
//            "        AND data >= DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR), '%Y-01-01')\n" +
//            "        AND data < DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
//            "    GROUP BY name\n" +
//            ") t2 \n" +
//            "ON t1.name = t2.name \n" +
//            "AND t1.data = t2.min_data;")
//    Double getqnzxdl(String cmsliststr);
        @Select("SELECT SUM(ADSP) AS ADSP \n" +
                "FROM (\n" +
                "    SELECT \n" +
                "        ADSP,\n" +
                "        -- 按时间升序排序，去年最早的记录行号为1\n" +
                "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data ASC) AS rn\n" +
                "    FROM ytmem\n" +
                "    WHERE \n" +
                "        name IN (${cmsliststr})\n" +
                "        -- 过滤去年数据（兼容多数数据库）\n" +
                "        AND data >= DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR), '%Y-01-01')\n" +
                "        AND data < DATE_FORMAT(CURRENT_DATE, '%Y-01-01')\n" +
                ") t\n" +
                "WHERE rn = 1;")
        Double getqnzxdl(String cmsliststr);

    @Select("")
    Map<String,List<String>> getByjrfh(String str);

    @Select("SELECT COALESCE(t.w, 0) AS 电力值\n" +
            "FROM (\n" +
            "  SELECT 0 AS hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 \n" +
            "  UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 \n" +
            "  UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11\n" +
            "  UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15\n" +
            "  UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19\n" +
            "  UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23\n" +
            ") AS hours\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    HOUR(`data`) AS hour,\n" +
            "    w\n" +
            "  FROM ytmem \n" +
            "  WHERE \n" +
            "    DATE(`data`) = CURDATE() - INTERVAL 1 DAY  -- 修改为昨天\n" +
            "    AND MINUTE(`data`) = 0\n" +
            "    AND name = \"AH2\"\n" +
            ") AS t ON hours.hour = t.hour\n" +
            "ORDER BY hours.hour;")
    double[] getByzrfh();


    //根据id查询某个设备最新的值
    @Select("SELECT * \n" +
            "FROM ytmem h\n" +
            "WHERE h.name = #{id}\n" +
            "ORDER BY h.data DESC\n" +
            "LIMIT 1;")
    Ytmem getsbjcTableData(String id);

    //查询总有功功率的最大值和最小值以及对应的时间
    @Select("(SELECT  w,data\n" +
            "FROM ytmem\n" +
            "WHERE w = (SELECT MAX(w) FROM ytmem where `name` =#{id}) LIMIT 1 )\n" +
            "UNION ALL\n" +
            "-- 查询 w 字段的最小值及其对应的 data 字段值\n" +
            "(SELECT w,data\n" +
            "FROM ytmem\n" +
            "WHERE w = (SELECT MIN(w) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String , Object>> getWMinMaxValues(String id);

    //查询总功率因数最大值最小值和时间
    @Select("(SELECT  PF,data\n" +
            "FROM ytmem\n" +
            "WHERE PF = (SELECT MAX(PF) FROM ytmem where `name` =#{id}) LIMIT 1)\n" +
            "UNION ALL\n" +
            "-- 查询 w 字段的最小值及其对应的 data 字段值\n" +
            "(SELECT PF,data\n" +
            "FROM ytmem\n" +
            "WHERE PF = (SELECT MIN(PF) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String , Object>> getPFMinMaxValues(String id);

    //查询uan的最大值和最小值
    @Select("(SELECT  Uan,data\n" +
            "FROM ytmem\n" +
            "WHERE Uan = (SELECT MAX(Uan) FROM ytmem where `name` =#{id}) LIMIT 1)\n" +
            "UNION ALL\n" +
            "-- 查询 w 字段的最小值及其对应的 data 字段值\n" +
            "(SELECT PF,data\n" +
            "FROM ytmem\n" +
            "WHERE Uan = (SELECT MIN(Uan) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String , Object>> getUanMinMaxValues(String id);

    @Select("(SELECT  Ubn,data\n" +
            "FROM ytmem\n" +
            "WHERE Ubn = (SELECT MAX(Ubn) FROM ytmem where `name` =#{id}) LIMIT 1)\n" +
            "UNION ALL\n" +
            "(SELECT Ubn,data\n" +
            "FROM ytmem\n" +
            "WHERE Ubn = (SELECT MIN(Ubn) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String, Object>> getUbnMinMaxValues(String id);

    @Select("(SELECT data, Ucn\n" +
            "FROM ytmem\n" +
            "WHERE Ucn = (SELECT MAX(Ucn) FROM ytmem WHERE `name` =#{id} )\n" +
            "LIMIT 1\n" +
            ")\n" +
            "UNION ALL\n" +
            "-- 查询 w 字段的最小值及其对应的 data 字段值\n" +
            "(SELECT data, Ucn\n" +
            "FROM ytmem\n" +
            "WHERE Ucn = (SELECT MIN(Ucn) FROM ytmem WHERE `name` =#{id})\n" +
            "LIMIT 1\n" +
            ");    ")
    List<Map<String, Object>> getUcnMinMaxValues(String id);

    @Select("(SELECT  Ia,data\n" +
            "FROM ytmem\n" +
            "WHERE Ia = (SELECT MAX(Ia) FROM ytmem where `name` =#{id}) LIMIT 1)\n" +
            "UNION ALL\n" +
            "(SELECT Ia,data\n" +
            "FROM ytmem\n" +
            "WHERE Ia = (SELECT MIN(Ia) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String, Object>> getIaMinMaxValues(String id);

    @Select("(SELECT  ${name},data\n" +
            "FROM ytmem\n" +
            "WHERE ${name} = (SELECT MAX(${name}) FROM ytmem where `name` =#{id}) LIMIT 1)\n" +
            "UNION ALL\n" +
            "(SELECT ${name},data\n" +
            "FROM ytmem\n" +
            "WHERE ${name} = (SELECT MIN(${name}) FROM ytmem where `name` =#{id}) LIMIT 1);    ")
    List<Map<String, Object>> getAllMinMaxValues(String id,String name);

    @Select("select ${id} from ytmem ")
    void demo01(String id);

    @Select("SELECT  y.Uan, y.Ubn, y.Ucn,y.`data`\n" +
            "FROM ytmem y\n" +
            "WHERE `data` > #{ks} AND `data` < #{js}\n" +
            "AND `name` = #{wz} \n" +
            "ORDER BY y.`data` ASC\n" +
            ";")
    List<Map<Object,Object>> getDnzlU(String wz, String ks, String js);

    @Select("SELECT  ${cmsa}, ${cmsb}, ${cmsc},`data`\n" +
            "FROM ytmem y\n" +
            "WHERE `data` > #{ks} AND `data` < #{js}\n" +
            "AND `name` =#{wz} \n" +
            "ORDER BY y.`data` ASC\n" +
            ";")
    List<Map<Object,Object>> getdnzldlsj(String cmsa, String cmsb, String cmsc, String wz, String ks, String js);

    @Select("SELECT  ${cms},${cmsa}, ${cmsb}, ${cmsc},`data`\n" +
            "FROM ytmem y\n" +
            "WHERE `data` > #{ks} AND `data` < #{js}\n" +
            "AND `name` =#{wz} \n" +
            "ORDER BY y.`data` ASC\n" +
            ";")
    List<Map<Object, Object>> getdnzlygsj(String cms, String cmsa, String cmsb, String cmsc, String wz, String ks, String js);

    @Select("SELECT  ${s},`data`\n" +
            "FROM ytmem y\n" +
            "WHERE `data` > #{ks} AND `data` < #{js}\n" +
            "AND `name` =#{wz} \n" +
            "ORDER BY y.`data` ASC\n" +
            ";")
    List<Map<Object, Object>> getdnzzszgl(String s, String wz, String ks, String js);

    @Insert("INSERT into ytmem(name,data,F,Q,S,W,Ia,Ib,Ic,PF,Qa,Qb,Qc,Sa,Sb,Sc,Wa,Wb,Wc,PFa,PFb,PFc,Uab,Uan,Ubc,Ubn,Uca,Ucn,ADSP,ADSQ,DI1,CT1) values" +
      " (#{name},#{data},#{F},#{Q},#{S},#{W},#{Ia},#{Ib},#{Ic},#{PF}," +
            "#{Qa},#{Qb},#{Qc},#{Sa},#{Sb},#{Sc},#{Wa},#{Wb},#{Wc},#{PFa},#{PFb},#{PFc},#{Uab},#{Uan},#{Ubc},#{Ubn},#{Uca},#{Ucn},#{ADSP},#{ADSQ},#{DI1},#{CT1})")
    void setYtmemByAll(Map<String, String> map);

    @Insert("INSERT into dianliang(name,d_time,jfpg,ygdl,df) values " +
            "(#{name},#{d_time},#{jfpg},#{dl},#{df})")
    void setDianLing(String name,String d_time,Integer jfpg,String dl,String df);

    //查询最新的电量值
    @Select("SELECT COALESCE(\n" +
            "    (SELECT ytmem.ADSP\n" +
            "     FROM ytmem \n" +
            "     WHERE \n" +
            "       name = #{name} \n" +
            "       -- 过滤整点：分钟=0 且 秒=0（确保是整点时间）\n" +
            "       AND MINUTE(data) = 0 \n" +
            "       AND SECOND(data) = 0 \n" +
            "     ORDER BY data DESC  -- 按时间降序，最新的在前面\n" +
            "     LIMIT 1),  -- 取最新的一条整点数据\n" +
            "    0.0\n" +
            ") AS adsp_value;")
    Double cxZuiXinDL(String name);

    /// 查询最新信息
//    @Select("SELECT \n" +
//            "    latest.name, \n" +
//            "    latest.Ia, \n" +
//            "    latest.Ib, \n" +
//            "    latest.Ic, \n" +
//            "    latest.DI1,\n" +
//            "    max_adsp.ADSP\n" +
//            "FROM (\n" +
//            "    SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1, t1.ADSP, t1.data\n" +
//            "    FROM ytmem t1\n" +
//            "    INNER JOIN (\n" +
//            "        SELECT name, MAX(data) AS latest_data\n" +
//            "        FROM ytmem\n" +
//            "        WHERE name IN ('1AA1-1','1AA2-1','1AA3-1','1AA4-1','1AA6-1','1AA6-2','1AA6-3','1AA6-4','1AA7-1','1AA7-2','1AA7-3','1AA7-4','1AA8-1','1AA8-2','1AA8-3','1AA8-4','1AA8-5'\\n\" +\n" +
//            "            \"    ,'1AA9-1','1AA9-2','1AA9-3','1AA9-4','1AA10-1','1AA10-2','1AA10-3','1AA10-4','1AA10-5','1AA10-6','1AA10-7','1AA11-1','1AA11-2','1AA11-3','1AA11-4','1AA11-5','1AA11-6','1AA11-7','1AA11-8'\\n\" +\n" +
//            "            \"    )\n" +
//            "        GROUP BY name\n" +
//            "    ) t2 ON t1.name = t2.name AND t1.data = t2.latest_data\n" +
//            ") latest\n" +
//            "INNER JOIN (\n" +
//            "    SELECT name, MAX(ADSP) AS ADSP\n" +
//            "    FROM ytmem\n" +
//            "    WHERE name IN ('1AA1-1','1AA2-1','1AA3-1','1AA4-1','1AA6-1','1AA6-2','1AA6-3','1AA6-4','1AA7-1','1AA7-2','1AA7-3','1AA7-4','1AA8-1','1AA8-2','1AA8-3','1AA8-4','1AA8-5'\\n\" +\n" +
//            "            \"    ,'1AA9-1','1AA9-2','1AA9-3','1AA9-4','1AA10-1','1AA10-2','1AA10-3','1AA10-4','1AA10-5','1AA10-6','1AA10-7','1AA11-1','1AA11-2','1AA11-3','1AA11-4','1AA11-5','1AA11-6','1AA11-7','1AA11-8'\\n\" +\n" +
//            "            \"    )\n" +
//            "    GROUP BY name\n" +
//            ") max_adsp ON latest.name = max_adsp.name;")
    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('1AA1-1','1AA2-1','1AA3-1','1AA4-1','1AA6-1','1AA6-2','1AA6-3','1AA6-4','1AA7-1','1AA7-2','1AA7-3','1AA7-4','1AA8-1','1AA8-2','1AA8-3','1AA8-4','1AA8-5'\n" +
            "    ,'1AA9-1','1AA9-2','1AA9-3','1AA9-4','1AA10-1','1AA10-2','1AA10-3','1AA10-4','1AA10-5','1AA10-6','1AA10-7','1AA11-1','1AA11-2','1AA11-3','1AA11-4','1AA11-5','1AA11-6','1AA11-7','1AA11-8'\n" +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getById41();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1 , t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('1AA12-1','1AA12-2','1AA12-3','1AA12-4','1AA12-5','1AA12-6','1AA12-7'" +
            ",'1AA13-1','1AA13-2','1AA13-3','1AA13-4','1AA13-5','1AA13-6','1AA13-7','1AA13-8','1AA14-1','1AA14-2','1AA14-3','1AA14-4'" +
            ",'1AA14-5','1AA14-6','1AA14-7','1AA14-8','1AA14-9','1AA15-1','1AA15-2','1AA15-3','1AA15-4','1AA15-5','1AA15-6','1AA16-1'" +
            ",'1AA16-2','1AA16-3','1AA16-4','1AA16-5','1AA16-6','1AA16-7','1AA16-8','1AA16-9'" +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getById42();

    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.total_ygdl, 2), 0) AS 电力值 \n" +
            "FROM \n" +
            "  (SELECT 0 AS hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 \n" +
            "   UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 \n" +
            "   UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 \n" +
            "   UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 \n" +
            "   UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 \n" +
            "   UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23) AS hours \n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    HOUR(d_time) AS hour,\n" +
            "    SUM(ygdl) AS total_ygdl \n" +
            "  FROM dianliang \n" +
            "  WHERE DATE(d_time) = #{datatest} \n" +
            "    AND MINUTE(d_time) = 0 \n" +
            "    AND name IN (${cmsliststr}) \n" +
            "  GROUP BY HOUR(d_time)\n" +
            ") AS t ON hours.hour = t.hour\n" +
            "ORDER BY hours.hour;")
    List<String> getdltjcxList2(String cmsliststr, String datatest);


    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.ygdl, 2), 0) AS 电力值\n" +
            "FROM \n" +
            "  (SELECT 0 AS hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 \n" +
            "   UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 \n" +
            "   UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 \n" +
            "   UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 \n" +
            "   UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 \n" +
            "   UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23) AS hours\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    HOUR(d_time) AS hour,\n" +
            "    ygdl\n" +
            "  FROM dianliang \n" +
            "  WHERE name = #{firstPart}\n" +
            "    AND d_time >= #{startTime}\n" +
            "    AND d_time < #{endTime}\n" +
            "    AND MINUTE(d_time) = 0\n" +
            ") AS t ON hours.hour = t.hour\n" +
            "ORDER BY hours.hour;")
    List<Double> getdltjcxList22(@Param("firstPart") String firstPart,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime);




    //查询某个月31天的值
    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.daily_sum, 2), 0) AS 日总电力值 \n" +
            "FROM (\n" +
            "  SELECT 1 AS day UNION SELECT 2  UNION SELECT 3  UNION SELECT 4 \n" +
            "  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7  UNION SELECT 8 \n" +
            "  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11 UNION SELECT 12\n" +
            "  UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16\n" +
            "  UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20\n" +
            "  UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24\n" +
            "  UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28\n" +
            "  UNION SELECT 29 UNION SELECT 30 UNION SELECT 31\n" +
            ") AS days\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    DAY(d_time) AS day_of_month,\n" +
            "    SUM(ygdl) AS daily_sum  \n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "    `name` IN (${cmsliststr})   \n" +
            "    AND d_time >= #{datatest} \n" +
            "    AND d_time < #{datats}     \n" +
            "  GROUP BY DAY(d_time)       \n" +
            ") AS t ON days.day = t.day_of_month\n" +
            "ORDER BY days.day;")
    List<String> getdltjcxmonthsList2(String cmsliststr, String datatest,String datats);

    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.daily_sum, 2), 0) AS 日总电力值 \n" +
            "FROM (\n" +
            "  SELECT 1 AS day UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 \n" +
            "  UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 \n" +
            "  UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 \n" +
            "  UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 \n" +
            "  UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 \n" +
            "  UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 \n" +
            "  UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 \n" +
            "  UNION SELECT 29 UNION SELECT 30 UNION SELECT 31\n" +
            ") AS days\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    DAY(d_time) AS day_of_month,\n" +
            "    SUM(ygdl) AS daily_sum\n" +
            "  FROM dianliang \n" +
            "  WHERE name = #{firstPart}\n" +
            "    AND d_time >= #{datatest}\n" +
            "    AND d_time < #{datats}\n" +
            "  GROUP BY DAY(d_time)\n" +
            ") AS t ON days.day = t.day_of_month\n" +
            "ORDER BY days.day;")
    List<Double> getdltjcxmonthsList22(String firstPart, String datatest, String datats);


    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.monthly_sum, 2), 0) AS 月总电力值\n" +
            "FROM (\n" +
            "  SELECT 1 AS month_num UNION SELECT 2  UNION SELECT 3  UNION SELECT 4 \n" +
            "  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7  UNION SELECT 8 \n" +
            "  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11 UNION SELECT 12\n" +
            ") AS months\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    MONTH(d_time) AS month,\n" +
            "    SUM(ygdl) AS monthly_sum  \n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "    `name` IN (${cmsliststr})  \n" +
            "    AND d_time >= #{datatest}  \n" +
            "    AND d_time < #{datats}    \n" +
            "  GROUP BY MONTH(d_time)   \n" +
            ") AS t ON months.month_num = t.month\n" +
            "ORDER BY months.month_num;")
    List<String> getdltjcxyearList2(String cmsliststr, String datatest,String datats);

    @Select("\n" +
            "SELECT \n" +
            "  COALESCE(ROUND(t.monthly_sum, 2), 0) AS 月总电力值\n" +
            "FROM (\n" +
            "  -- 生成1-12月的序列\n" +
            "  SELECT 1 AS month_num UNION SELECT 2  UNION SELECT 3  UNION SELECT 4 \n" +
            "  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7  UNION SELECT 8 \n" +
            "  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11 UNION SELECT 12\n" +
            ") AS months\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    MONTH(d_time) AS month,\n" +
            "    SUM(ygdl) AS monthly_sum\n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "    `name` = #{firstPart} AND\n" +
            "    d_time >= #{datatest} AND  -- 查询整个2025年\n" +
            "    d_time < #{datats}\n" +
            "  GROUP BY MONTH(d_time)\n" +
            ") AS t ON months.month_num = t.month\n" +
            "ORDER BY months.month_num;\n")
    List<Double> getdltjcxyearList22(String firstPart, String datatest,String datats);

    @Select("SELECT\n" +
            "\tADSP\n" +
            "FROM\n" +
            "\tytmem\n" +
            "WHERE\n" +
            "\t`name` =  #{name}  \n" +
            "  AND DATE(`data`)= #{date}    \n" +
            "LIMIT 1;\n")
    Double getnhtj(String name, String date);

    //查询一天24小时的值
    @Select("SELECT \n" +
            "    COALESCE(nearest_data.ADSP, 0) AS `ADSP`  -- 无数据时显示0[2,6](@ref)\n" +
            "FROM (\n" +
            "    SELECT 0 AS hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 \n" +
            "    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 \n" +
            "    UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 \n" +
            "    UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 \n" +
            "    UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23\n" +
            ") AS hour_series\n" +
            "LEFT JOIN LATERAL (\n" +
            "    SELECT \n" +
            "        y.ADSP,\n" +
            "        ABS(HOUR(y.data) - hour_series.hour) AS time_diff\n" +
            "    FROM ytmem y\n" +
            "    WHERE \n" +
            "        y.name = #{realclwz} \n" +
            "        AND y.data >= #{clsj}  -- 优化时间范围[6](@ref)\n" +
            "        AND y.data < #{nextDayStr} \n" +
            "        AND HOUR(y.data) BETWEEN hour_series.hour - 1 AND hour_series.hour + 1\n" +
            "    ORDER BY time_diff ASC, y.data DESC\n" +
            "    LIMIT 1\n" +
            ") AS nearest_data ON true\n" +
            "ORDER BY hour_series.hour;")
    List<String> getbrsj(String realclwz, String clsj, String nextDayStr);

    //查询每小时的电费
    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.total_df, 2), 0) AS 电力值 \n" +
            "FROM \n" +
            "  (-- 生成24小时的数字序列（不变）\n" +
            "   SELECT 0 AS hour UNION SELECT 1  UNION SELECT 2  UNION SELECT 3 \n" +
            "   UNION SELECT 4  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7 \n" +
            "   UNION SELECT 8  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11\n" +
            "   UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15\n" +
            "   UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19\n" +
            "   UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23\n" +
            "  ) AS hours\n" +
            "LEFT JOIN (\n" +
            "  -- 按整点小时聚合：多个name的df求和\n" +
            "  SELECT \n" +
            "    HOUR(d_time) AS hour,\n" +
            "    SUM(df) AS total_df  -- 关键：求和多个name的df值\n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "    DATE(d_time) = #{datatest}  -- 指定日期（不变）\n" +
            "    AND MINUTE(d_time) = 0       -- 只取整点（不变）\n" +
            "    AND name IN (${cmsliststr})    \n" +
            "  GROUP BY HOUR(d_time)          -- 仅按小时分组（去掉name分组）\n" +
            ") AS t ON hours.hour = t.hour\n" +
            "ORDER BY hours.hour;")
    List<String> getdltjcxList3(String cmsliststr, String datatest);

    @Select("    SELECT \n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '1' THEN ygdl ELSE 0 END), 0), 2) AS jfpg_1,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '2' THEN ygdl ELSE 0 END), 0), 2) AS jfpg_2,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '3' THEN ygdl ELSE 0 END), 0), 2) AS jfpg_3,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '4' THEN ygdl ELSE 0 END), 0), 2) AS jfpg_4\n" +
            "FROM dianliang \n" +
            "WHERE \n" +
            "    DATE(d_time) = #{datatest}  \n" +
            "    AND `name` IN (${cmsliststr})  \n" +
            "    AND jfpg IN ('1', '2', '3', '4');  -- 限定jfpg范围[6,7](@ref)")
    Map<String, Double> getdltjcxList4(String cmsliststr, String datatest);

    @Select("    SELECT \n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '1' THEN df ELSE 0 END),0), 2) AS jfpg_1,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '2' THEN df ELSE 0 END),0),2) AS jfpg_2,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '3' THEN df ELSE 0 END),0), 2) AS jfpg_3,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '4' THEN df ELSE 0 END),0), 2) AS jfpg_4\n" +
            "FROM dianliang \n" +
            "WHERE \n" +
            "    DATE(d_time) = #{datatest}  \n" +
            "     AND `name` IN (${cmsliststr}) \n" +
            "    AND jfpg IN ('1', '2', '3', '4');  -- 限定jfpg范围[6,7](@ref)")
    Map<String, Double> getdltjcxList5(String cmsliststr, String datatest);

    @Select("SELECT \n" +
            "  COALESCE(ROUND(t.daily_sum, 2), 0) AS 日总电力值  -- 保留两位小数，无数据返回0\n" +
            "FROM (\n" +
            "  -- 生成1-31天序列（不变）\n" +
            "  SELECT 1 AS day UNION SELECT 2  UNION SELECT 3  UNION SELECT 4 \n" +
            "  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7  UNION SELECT 8 \n" +
            "  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11 UNION SELECT 12\n" +
            "  UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16\n" +
            "  UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20\n" +
            "  UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24\n" +
            "  UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28\n" +
            "  UNION SELECT 29 UNION SELECT 30 UNION SELECT 31\n" +
            ") AS days\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    DAY(d_time) AS day_of_month,\n" +
            "    SUM(df) AS daily_sum  -- 汇总多个name的当日df总和（先求和再 rounding 更精准）\n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "    `name` IN (${cmsliststr})  -- 多name条件\n" +
            "    AND d_time >= #{datatest}  -- 起始日期（不变）\n" +
            "    AND d_time < #{datats}     -- 结束日期（不变）\n" +
            "  GROUP BY DAY(d_time)        -- 仅按日期分组（汇总所有指定name）\n" +
            ") AS t ON days.day = t.day_of_month\n" +
            "ORDER BY days.day;")
    List<String> getdltjcxmonthsList3(String cmsliststr, String datatest, String datats);

    @Select("SELECT \n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '1' THEN ygdl ELSE 0 END), 0),2) AS jfpg_1,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '2' THEN ygdl ELSE 0 END),0), 2) AS jfpg_2,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '3' THEN ygdl ELSE 0 END),0), 2) AS jfpg_3,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '4' THEN ygdl ELSE 0 END),0), 2) AS jfpg_4\n" +
            "FROM dianliang \n" +
            "WHERE \n" +
            "    d_time >= #{s} \n" +
            "    AND d_time < #{s1}\n" +
            "    AND name IN (${cmsliststr}) \n" +
            "    AND jfpg IN ('1', '2', '3', '4');  -- 限定jfpg范围[6,7](@ref)")
    Map<String, Double> getdltjcxmonthsList4(String cmsliststr, String s, String s1);

    @Select("SELECT \n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '1' THEN df ELSE 0 END),0), 2) AS jfpg_1,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '2' THEN df ELSE 0 END),0), 2) AS jfpg_2,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '3' THEN df ELSE 0 END),0), 2) AS jfpg_3,\n" +
            "    ROUND(COALESCE(SUM(CASE WHEN jfpg = '4' THEN df ELSE 0 END),0), 2) AS jfpg_4\n" +
            "FROM dianliang \n" +
            "WHERE \n" +
            "    d_time >= #{s} \n" +
            "    AND d_time < #{s1}\n" +
            "    AND name IN (${cmsliststr}) \n" +
            "    AND jfpg IN ('1', '2', '3', '4');  -- 限定jfpg范围[6,7](@ref)")
    Map<String, Double> getdltjcxmonthsList5(String cmsliststr, String s, String s1);
    
    @Select("\n" +
            "SELECT \n" +
            "  COALESCE(ROUND(t.monthly_sum, 2), 0) AS 月总电力值\n" +
            "FROM (\n" +
            "  -- 生成1-12月的序列\n" +
            "  SELECT 1 AS month_num UNION SELECT 2  UNION SELECT 3  UNION SELECT 4 \n" +
            "  UNION SELECT 5  UNION SELECT 6  UNION SELECT 7  UNION SELECT 8 \n" +
            "  UNION SELECT 9  UNION SELECT 10 UNION SELECT 11 UNION SELECT 12\n" +
            ") AS months\n" +
            "LEFT JOIN (\n" +
            "  SELECT \n" +
            "    MONTH(d_time) AS month,\n" +
            "    SUM(df) AS monthly_sum\n" +
            "  FROM dianliang \n" +
            "  WHERE \n" +
            "     `name` IN (${cmsliststr}) AND\n" +
            "    d_time >= #{datatest} AND  -- 查询整个2025年\n" +
            "    d_time < #{datats}\n" +
            "  GROUP BY MONTH(d_time)\n" +
            ") AS t ON months.month_num = t.month\n" +
            "ORDER BY months.month_num;\n")
    List<String> getdltjcxyearList3(String cmsliststr, String datatest, String datats);

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2AA1-1','2AA1-2','2AA1-3','2AA1-4','2AA1-5','2AA1-6','2AA1-7','2AA1-8','2AA1-9'" +
            "                   ,'2AA2-1','2AA2-2','2AA2-3','2AA2-4','2AA2-5','2AA2-6','2AA2-7','2AA2-8','2AA2-9'" +
            "                    ,'2AA3-1' ,'2AA3-2' ,'2AA3-3' ,'2AA3-4' ,'2AA3-5' ,'2AA3-6' ,'2AA3-7' ,'2AA3-8'" +
            "                ,'2AA4-1','2AA4-2','2AA4-3','2AA4-4','2AA4-5','2AA4-6','2AA4-7'" +
            "               ,'2AA5-1','2AA5-2','2AA5-3','2AA5-4','2AA5-5','2AA5-6','2AA5-7','2AA5-8','2AA5-9'" +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getById51();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic,t1.Uan,t1.Ubn,t1.Ucn, t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2AA7-1','2AA7-2','2AA7-3','2AA7-4','2AA7-5','2AA7-6','2AA7-7','2AA7-8'" +
            ",'2AA8-1','2AA8-2','2AA8-3','2AA8-4','2AA9-1','2AA9-2','2AA9-3','2AA9-4','2AA9-5'" +
            ",'2AA10-1','2AA10-2','2AA10-3','2AA10-4','2AA11-1','2AA11-2','2AA11-3','2AA11-4','2AA12-1'" +
            ",'2AA13-1','2AA14-1','2AA15-1'   " +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getBYId52();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic,t1.Uan,t1.Ubn,t1.Ucn, t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('3AA1-1','3AA2-1','3AA3-1','3AA4-1','3AA5-1','3AA6-1','3AA6-2','3AA6-3','3AA6-4','3AA6-5' " +
            ",'3AA7-1','3AA7-2','3AA7-3','3AA7-4','3AA7-5','3AA8-1','3AA8-2','3AA8-3','3AA8-4','3AA9-1','3AA9-2','3AA9-3','3AA9-4','3AA9-5','3AA9-6','3AA9-7'" +
            ",'3AA10-1','3AA10-2','3AA10-3','3AA10-4','3AA10-5','3AA10-6','3AA11-1','3AA11-2','3AA11-3','3AA11-4','3AA11-5','3AA11-6','3AA11-7'   " +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getBYId61();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('3AA12-1','3AA12-2','3AA12-3','3AA12-4','3AA12-5','3AA12-6','3AA13-1','3AA13-2','3AA13-3','3AA13-4','3AA13-5'" +
            ",'3AA14-1','3AA14-2','3AA14-3','3AA14-4','3AA14-5','3AA15-1','3AA15-2','3AA15-3','3AA15-4','3AA16-1','3AA16-2','3AA16-3','3AA16-4','3AA16-5','3AA16-6'" +
            ",'3AA17-1','3AA17-2','3AA17-3','3AA17-4','3AA17-5','3AA17-6','3AA17-7','3AA18-1','3AA18-2','3AA18-3','3AA18-4','3AA18-5','3AA18-6'" +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getBYId62();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('4AA1-1','4AA1-2','4AA1-3','4AA1-4','4AA1-5','4AA1-6'" +
            ",'4AA2-1','4AA2-2','4AA2-3','4AA2-4','4AA2-5','4AA2-6','4AA3-1','4AA3-2','4AA3-3','4AA3-4','4AA3-5','4AA3-6'" +
            ",'4AA4-1','4AA4-2','4AA4-3','4AA4-4','4AA4-5','4AA5-1','4AA5-2','4AA5-3','4AA5-4','4AA5-5' " +
            ",'4AA6-1','4AA6-2','4AA6-3','4AA6-4','4AA6-5','4AA6-6' " +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getBYId71();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.Uan,t1.Ubn,t1.Ucn,t1.DI1,t1.CT1\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('4AA7-1','4AA7-2','4AA7-3','4AA7-4','4AA7-5','4AA7-6'" +
            ",'4AA8-1','4AA8-2','4AA8-3','4AA8-4','4AA8-5','4AA8-6','4AA8-7','4AA8-8'" +
            ",'4AA9-1','4AA9-2','4AA9-3','4AA9-4','4AA9-5','4AA9-6','4AA9-7','4AA11-1','4AA11-2'" +
            ",'4AA11-3','4AA11-4','4AA11-5','4AA12-1','4AA12-2','4AA12-3','4AA12-4','4AA12-5'" +
            ",'4AA13-1','4AA13-2','4AA13-3','4AA13-4','4AA13-5'" +
            ",'4AA14-1','4AA15-1','4AA16-1','4AA17-1'" +
            "  )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getBYId72();

    /**
     * 拿到指定日期的最新电量
     * @param realclwz
     * @param clsj
     * @return
     */
    @Select("select ADSP from ytmem where name =#{realclwz} AND DATE(data) = #{clsj} ORDER BY data DESC LIMIT 1; ")
    Double getADSPByname(String realclwz, String clsj);

    @Select("SELECT \n" +
            "  DATE(`data`) AS data, \n" +
            "  MAX(W) AS W \n" +
            "FROM ytmem \n" +
            "WHERE `name` = #{clwz} \n" +
            "  AND DATE(`data`) >= #{kssj} \n" +
            "  AND DATE(`data`) <= #{sjsj} \n" +
            "GROUP BY DATE(`data`) \n" +
            "ORDER BY DATE(`data`);")
    List<Map<String,String>> getfhdbfx(String kssj, String sjsj, String clwz);


    @Select("SELECT MAX(W) AS max_W FROM ytmem \n" +
            "WHERE `name` =#{clwz} \n" +
            "AND DATE(`data`) = #{dbrq};")
    String getfhdbfx2(String clwz, String dbrq);

    @Select("SELECT \n" +
            "  DATE_FORMAT(`data`, '%Y-%m-%d %H:%i:%s') AS data  -- 格式化时间为 \"2025-07-10 02:00:00\"\n" +
            "FROM ytmem \n" +
            "WHERE  `name` =\"1AA6-1\" AND `data`>= '2025-6-21' AND `data`<= '2025-7-23'")
    List<Map<String,String>> demodata();

    @Select("SELECT  data, W FROM ytmem \n" +
            "WHERE `name` = #{clwz} \n" +
            "AND DATE(`data`) = #{dysj}\n" +
            "ORDER BY data\n" +
            ";")
    List<Map<String, String>> getfhsbfx(String dysj, String clwz);

    //查询四个低压总的功率计算后的值
//    @Select("SELECT \n" +
//            "  TIME(data) AS shijian,  -- 时间数据（不分日期）\n" +
//            "  ROUND(SUM(CASE WHEN DATE(data) =  #{shijain}  THEN W - Q ELSE 0 END), 2) AS jr,  -- 21号数据\n" +
//            "  ROUND(SUM(CASE WHEN DATE(data) = #{shijain2} THEN W - Q ELSE 0 END), 2) AS zr   -- 22号数据\n" +
//            "FROM ytmem\n" +
//            "WHERE `name` IN (\"1AA1-1\", \"2AA15-1\", \"3AA1-1\", \"4AA17-1\") \n" +
//            "  AND DATE(data) IN (#{shijain}, #{shijain2})\n" +
//            "GROUP BY TIME(data)  -- 按时间分组（忽略日期）\n" +
//            "ORDER BY shijian;")
    @Select("SELECT \n" +
            "                TIME(a.data) AS shijian, \n" +
            "                a.fh AS jr\n" +
            "            FROM \n" +
            "                region_one_summary a  WHERE\n" +
            "                    DATE(a.data) = #{shijain}" +
            "AND name = #{name} ")
    List<Map<String, Object>> getNewByjrfh(String shijain,String name);

    @Select("SELECT ROUND(SUM(w), 2) AS result\n" +
            "FROM (\n" +
            "    -- 你的原查询，获取四个名称的最新记录\n" +
            "    SELECT t.w\n" +
            "    FROM ytmem t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(data) AS latest_time\n" +
            "        FROM ytmem\n" +
            "        WHERE name IN (\"1AA1-1\", \"2AA15-1\", \"3AA1-1\", \"4AA17-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.data = lt.latest_time\n" +
            ") AS subquery;")
    Double getOnePDS();

    @Select("SELECT ROUND(SUM(w), 2) AS result\n" +
            "FROM (\n" +
            "    -- 你的原查询，获取四个名称的最新记录\n" +
            "    SELECT t.w\n" +
            "    FROM ytmem t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(data) AS latest_time\n" +
            "        FROM ytmem\n" +
            "        WHERE name IN (\"2-1AA1-1\", \"2-2AA14-1\", \"2-3AA1-1\", \"2-4AA14-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.data = lt.latest_time\n" +
            ") AS subquery;")
    Double getTwoPDS();

    @Select("SELECT ROUND(SUM(w), 2) AS result\n" +
            "FROM (\n" +
            "    -- 你的原查询，获取四个名称的最新记录\n" +
            "    SELECT t.w\n" +
            "    FROM ytmem t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(data) AS latest_time\n" +
            "        FROM ytmem\n" +
            "        WHERE name IN (\"6-1AA1-1\", \"6-2AA11-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.data = lt.latest_time\n" +
            ") AS subquery;")
    Double getSixPDS();

    @Select("\n" +
            "SELECT ROUND(SUM(ygdl), 2) AS ygdl,MAX(d_time) AS data\n" +
            "FROM (\n" +
            "    -- 原查询：获取四个名称的最新ygdl数据\n" +
            "    SELECT t.ygdl ,t.d_time\n" +
            "    FROM dianliang t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(d_time) AS latest_time\n" +
            "        FROM dianliang\n" +
            "        WHERE name IN (\"1AA1-1\", \"2AA15-1\", \"3AA1-1\", \"4AA17-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.d_time = lt.latest_time\n" +
            ") AS subquery;")
    Map<String,Object> getOnePDShdl();

    @Select("\n" +
            "SELECT ROUND(SUM(ygdl), 2) AS ygdl,MAX(d_time) AS data\n" +
            "FROM (\n" +
            "    -- 原查询：获取四个名称的最新ygdl数据\n" +
            "    SELECT t.ygdl ,t.d_time\n" +
            "    FROM dianliang t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(d_time) AS latest_time\n" +
            "        FROM dianliang\n" +
            "        WHERE name IN (\"2-1AA1-1\", \"2-2AA14-1\", \"2-3AA1-1\", \"2-4AA14-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.d_time = lt.latest_time\n" +
            ") AS subquery;")
    Map<String,Object> getTwoPDShdl();
    @Select("\n" +
            "SELECT ROUND(SUM(ygdl), 2) AS ygdl,MAX(d_time) AS data\n" +
            "FROM (\n" +
            "    -- 原查询：获取四个名称的最新ygdl数据\n" +
            "    SELECT t.ygdl ,t.d_time\n" +
            "    FROM dianliang t\n" +
            "    INNER JOIN (\n" +
            "        SELECT name, MAX(d_time) AS latest_time\n" +
            "        FROM dianliang\n" +
            "        WHERE name IN (\"6-1AA1-1\", \"6-2AA11-1\")\n" +
            "        GROUP BY name\n" +
            "    ) lt ON t.name = lt.name AND t.d_time = lt.latest_time\n" +
            ") AS subquery;")
    Map<String,Object> getSixPDShdl();

    @Insert("INSERT into region_one_summary(name,fh,dl,data,zdl) values " +
            "('1号配电室',#{onePDS},#{onePDShdl},#{data},#{zdl})")
    void setOnePDS(Double onePDS, Object onePDShdl, String data,Double zdl);

    @Insert("INSERT into region_one_summary(name,fh,dl,data,zdl) values " +
            "(#{name},#{onePDS},#{onePDShdl},#{data},#{zdl})")
    void setTwoPDS(Double onePDS, Object onePDShdl, String data,Double zdl,String name);

    //滤波器添加数据
    @Insert("INSERT into lbg(name,data,LBQ_status,LBQ_alarm,bc_Ia,bc_Ib,bc_Ic,temperature,Ia,Ib,Ic,Uan,Ubn,Ucn,DLJBL,PF,FZL) values " +
            "(#{name},#{data},#{LBQ_status},#{LBQ_alarm},#{bc_Ia},#{bc_Ib},#{bc_Ic},#{temperature},#{Ia},#{Ib},#{Ic},#{Uan},#{Ubn},#{Ucn},#{DLJBL},#{PF},#{FZL})")
    void setLBQByALL(Map<String, String> map);

    //查询指定名称的滤波器数据
    @Select("select * from lbg WHERE `name` = #{name} ORDER BY data DESC LIMIT 1;")
    Map<String, Object> getlbqByName(String name);

    @Select("SELECT W \n" +
            "FROM (\n" +
            "  SELECT \n" +
            "    W,\n" +
            "    name,\n" +
            "    -- 按名称分组，按时间降序排序，每组内最新的记录标记为1\n" +
            "    ROW_NUMBER() OVER (PARTITION BY name ORDER BY data DESC) AS rn\n" +
            "  FROM ytmem \n" +
//            "  WHERE name IN ('1AA1-1', '2AA15-1', '3AA1-1', '4AA17-1')\n" +
            "  WHERE name IN (${cmsliststr})\n" +
            ") AS sub\n" +
            "-- 只取每组中排序为1的记录（即最新的记录）\n" +
            "WHERE rn = 1;")
    List<String> getbyqxq(String cmsliststr);

    @Select("SELECT * FROM ytmem  WHERE name = #{names} ORDER BY `data` DESC LIMIT 1;")
    Map<String,Object> getDBnewDate(@Param("names") String names);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "sumAdspByNamesAndDate")
    List<Map<String, Object>> getbyday(
            @Param("names") List<String> names,
            @Param("date") String date
    );
    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "sumAdspByNamesAndmonth")
    List<Map<String, Object>> getbymonth(
            @Param("names") List<String> names,
            @Param("date") String date
    );
    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "sumAdspByNamesAndyear")
    List<Map<String, Object>> getbyyear(
            @Param("names") List<String> names,
            @Param("date") String date
    );
    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "sumAdspByNamesAndRange")
    List<Map<String, Object>> getbycustom(
            @Param("names") List<String> names,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "getMultiNameTimeData")
    List<Map<String, Object>> getMultiNameTimeData(
            @Param("names") List<String> names,
            @Param("date") String date
    );
    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "batchGetNhtj")
    List<Map<String, Object>> batchGetNhtj(
            @Param("names") List<String> names,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    @Select("SELECT\n" +
            "    SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian,\n" +
            "    SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng,\n" +
            "    SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping,\n" +
            "    SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu\n" +
            "FROM\n" +
            "    dianliang\n" +
            "WHERE\n" +
            "    DATE(d_time) = #{date}  -- 替换为你要查询的日期，格式如 '2025-06-03'\n" +
            "ORDER BY\n" +
            "    jfpg;")
    List<Map<String, Object>> getDLjfpgBydate(String date);

    @Select("SELECT\n" +
            "    SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian,\n" +
            "    SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng,\n" +
            "    SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping,\n" +
            "    SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu\n" +
            "FROM\n" +
            "    dianliang\n" +
            "WHERE\n" +
            "    DATE_FORMAT(d_time, '%Y-%m') = #{date}  -- 替换为你要查询的日期，格式如 '2025-06-03'\n" +
            "ORDER BY\n" +
            "    jfpg;")
    List<Map<String, Object>> getDLjfpgBymonth(String date);

    @Select("SELECT\n" +
            "    SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian,\n" +
            "    SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng,\n" +
            "    SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping,\n" +
            "    SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu\n" +
            "FROM\n" +
            "    dianliang\n" +
            "WHERE\n" +
            "    YEAR(d_time) = #{date}  -- 替换为你要查询的日期，格式如 '2025-06-03'\n" +
            "ORDER BY\n" +
            "    jfpg;")
    List<Map<String, Object>> getDLjfpgByyear(String date);

    @Select("SELECT\n" +
            "    SUM(CASE WHEN jfpg = 1 THEN ygdl ELSE 0 END) AS jian,\n" +
            "    SUM(CASE WHEN jfpg = 2 THEN ygdl ELSE 0 END) AS feng,\n" +
            "    SUM(CASE WHEN jfpg = 3 THEN ygdl ELSE 0 END) AS ping,\n" +
            "    SUM(CASE WHEN jfpg = 4 THEN ygdl ELSE 0 END) AS gu\n" +
            "FROM\n" +
            "    dianliang\n" +
            "WHERE\n" +
            "    DATE(d_time) BETWEEN #{startDate} AND #{endDate}\n" +
            "ORDER BY\n" +
            "    jfpg;")
    List<Map<String, Object>> getDLjfpgBycustom(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "getDLjfpgBydateAndNames")
    List<Map<String, Object>> getDLjfpgBydateAndNames(@Param("names") List<String> names,
                                                      @Param("date") String date);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "getDLjfpgBymonthAndNames")
    List<Map<String, Object>> getDLjfpgBymonthAndNames(@Param("names") List<String> names,
                                                       @Param("date") String date);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "getDLjfpgByyearAndNames")
    List<Map<String, Object>> getDLjfpgByyearAndNames(@Param("names") List<String> names,
                                                      @Param("date") String date);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "getDLjfpgBycustomAndNames")
    List<Map<String, Object>> getDLjfpgBycustomAndNames(@Param("names") List<String> names,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate);


    @Select("SELECT * FROM ytmem  WHERE name = #{name} AND `data` BETWEEN #{startDate} AND #{endDate}  ORDER BY `data` DESC ;")
    List<Map<String, Object>> getYthQH(String name, String startDate, String endDate);

    @Select("SELECT * FROM ytmem WHERE name = #{name} AND `data` <= #{startDate} ORDER BY `data` DESC LIMIT 1;")
    List<Map<String, Object>> getYthQHByPoint(String name, String startDate);


    @Select("SELECT \n" +
            "    current_hour.name,\n" +
            "    current_hour.data AS current_hour_time,\n" +
            "    current_hour.ADSP AS current_reading,\n" +
            "    LAG(current_hour.ADSP) OVER (ORDER BY current_hour.data) AS previous_hour_reading,\n" +
            "    -- 计算耗电量差值，当没有前一小时数据时（如第一天第一个点）设为NULL或0\n" +
            "    ROUND( current_hour.ADSP - LAG(current_hour.ADSP) OVER (ORDER BY current_hour.data),2) AS hourly_consumption\n" +
            "FROM \n" +
            "    (SELECT \n" +
            "        name,\n" +
            "        data,\n" +
            "        ADSP\n" +
            "     FROM ytmem\n" +
            "     WHERE `name` = #{name}\n" +
            "       AND data BETWEEN '2025-11-01 01:00:00' AND '2025-11-08 23:59:59'\n" +
            "       AND MINUTE(data) = 0 \n" +
            "       AND SECOND(data) = 0\n" +
            "     ORDER BY data ASC\n" +
            "    ) AS current_hour;")
    List<Map<String, Object>> getSYshujuxiufu(String name);

    @Update("UPDATE dianliang SET ygdl = #{hourlyConsumption} WHERE name = #{name} AND d_time = #{localDateTime}")
    void setSYshuju(String name, LocalDateTime localDateTime, Double hourlyConsumption);

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-1AA1-1','2-1AA2-1','2-1AA3-1','2-1AA4-1','2-1AA6-1','2-1AA6-2','2-1AA6-3','2-1AA6-4','2-1AA7-1','2-1AA7-2','2-1AA7-3','2-1AA8-1','2-1AA8-2','2-1AA8-3'\n" +
            "    ,'2-1AA9-1','2-1AA9-2','2-1AA9-3','2-1AA9-4','2-1AA9-5','2-1AA9-6','2-1AA9-7','2-1AA10-1','2-1AA10-2','2-1AA10-3','2-1AA10-4','2-1AA10-5','2-1AA10-6','2-1AA10-7','2-1AA10-8','2-1AA11-1','2-1AA11-2','2-1AA11-3','2-1AA11-4','2-1AA11-5','2-1AA11-6','2-1AA11-7','2-1AA11-8'\n" +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId42();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-1AA12-1','2-1AA12-2','2-1AA12-3','2-1AA12-4','2-1AA12-5','2-1AA12-6','2-1AA12-7'" +
            ",'2-1AA13-1','2-1AA13-2','2-1AA13-3','2-1AA13-4','2-1AA13-5','2-1AA13-6','2-1AA13-7','2-1AA13-8','2-1AA13-9'" +
            ",'2-1AA14-1','2-1AA14-2','2-1AA14-3','2-1AA14-4','2-1AA14-5','2-1AA14-6'" +
            ",'2-1AA15-1','2-1AA15-2','2-1AA15-3','2-1AA15-4','2-1AA15-5'  \n" +
           "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId422();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-2AA1-1','2-2AA1-2','2-2AA1-3','2-2AA1-4','2-2AA1-5'" +
            ",'2-2AA2-1','2-2AA2-2','2-2AA2-3','2-2AA2-4','2-2AA2-5','2-2AA2-6'" +
            ",'2-2AA3-1','2-2AA3-2','2-2AA3-3','2-2AA3-4','2-2AA3-5','2-2AA3-6','2-2AA3-7','2-2AA3-8','2-2AA3-9'" +
            ",'2-2AA4-1','2-2AA4-2','2-2AA4-3','2-2AA4-4','2-2AA4-5','2-2AA4-6','2-2AA4-7'" +
            ",'2-2AA5-1','2-2AA5-2','2-2AA5-3','2-2AA5-4','2-2AA5-5','2-2AA5-6','2-2AA5-7','2-2AA5-8'    \n" +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId51();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-2AA7-1','2-2AA7-2','2-2AA7-3','2-2AA7-4','2-2AA7-5','2-2AA7-6','2-2AA7-7','2-2AA7-8'" +
            ",'2-2AA8-1','2-2AA8-2','2-2AA8-3'" +
            ",'2-2AA9-1','2-2AA9-2','2-2AA9-3'" +
            ",'2-2AA10-1','2-2AA10-2','2-2AA10-3','2-2AA10-4','2-2AA12-1','2-2AA13-1','2-2AA14-1'  " +
             "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId52();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-3AA1-1','2-3AA2-1','2-3AA3-1','2-3AA4-1'" +
            ",'2-3AA6-1','2-3AA6-2','2-3AA6-3','2-3AA6-4','2-3AA6-5'" +
            ",'2-3AA7-1','2-3AA7-2','2-3AA7-3','2-3AA7-4','2-3AA7-5','2-3AA7-6'" +
            ",'2-3AA8-1','2-3AA8-2','2-3AA8-3','2-3AA8-4','2-3AA8-5'" +
            ",'2-3AA9-1','2-3AA9-2','2-3AA9-3','2-3AA9-4','2-3AA9-5','2-3AA9-6'  " +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId61();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-3AA10-1','2-3AA10-2','2-3AA10-3','2-3AA10-4','2-3AA10-5','2-3AA10-6'" +
            ",'2-3AA10-7','2-3AA10-8','2-3AA10-9'" +
            ",'2-3AA11-1','2-3AA11-2','2-3AA11-3','2-3AA11-4','2-3AA11-5','2-3AA11-6','2-3AA11-7'" +
            ",'2-3AA12-1','2-3AA12-2','2-3AA12-3','2-3AA12-4','2-3AA12-5','2-3AA12-6'" +
            ",'2-3AA13-1','2-3AA13-2','2-3AA13-3','2-3AA13-4','2-3AA13-5','2-3AA13-6','2-3AA13-7','2-3AA13-8'" +
            ",'2-3AA14-1','2-3AA14-2','2-3AA14-3','2-3AA14-4','2-3AA14-5','2-3AA14-6','2-3AA14-7'   " +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId62();
    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-4AA1-1','2-4AA1-2','2-4AA1-3','2-4AA1-4','2-4AA1-5','2-4AA1-6','2-4AA1-7','2-4AA1-8'" +
            ",'2-4AA2-1','2-4AA2-2','2-4AA2-3','2-4AA2-4','2-4AA2-5','2-4AA2-6','2-4AA2-7','2-4AA2-8'" +
            ",'2-4AA3-1','2-4AA3-2','2-4AA3-3','2-4AA3-4','2-4AA3-5','2-4AA3-6'" +
            ",'2-4AA4-1','2-4AA4-2','2-4AA4-3','2-4AA4-4','2-4AA4-5','2-4AA4-6','2-4AA4-7' " +
            ",'2-4AA5-1','2-4AA5-2','2-4AA5-3','2-4AA5-4','2-4AA5-5','2-4AA5-6','2-4AA5-7','2-4AA5-8','2-4AA5-9'" +
            ",'2-4AA6-1','2-4AA6-2','2-4AA6-3','2-4AA6-4','2-4AA6-5','2-4AA6-6' " +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId71();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('2-4AA8-1','2-4AA8-2','2-4AA8-3','2-4AA8-4','2-4AA8-5'" +
            ",'2-4AA9-1','2-4AA9-2','2-4AA9-3','2-4AA9-4','2-4AA9-5','2-4AA9-6'" +
            ",'2-4AA10-1','2-4AA10-2','2-4AA10-3','2-4AA10-4','2-4AA10-5'" +
            "   ,'2-4AA12-1','2-4AA13-1','2-4AA14-1'  " +

            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getTowBYId72();

    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('6-1AA1-1','6-1AA2-1','6-1AA3-1','6-1AA4-1','6-1AA6-1','6-1AA7-1'" +
            ",'6-1AA8-1','6-1AA8-2','6-1AA8-3','6-1AA8-4','6-1AA8-5','6-1AA9-1'" +
            ",'6-1AA10-1','6-1AA10-2','6-1AA10-3','6-1AA10-4','6-1AA10-5'  " +

            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getSixBYId64();
    @Select("SELECT t1.name, t1.Ia, t1.Ib, t1.Ic, t1.DI1,t1.CT1,t1.Uan,t1.Ubn,t1.Ucn\n" +
            "FROM ytmem t1\n" +
            "INNER JOIN (\n" +
            "    SELECT name, MAX(data) AS latest_data\n" +
            "    FROM ytmem\n" +
            "    WHERE name IN ('6-2AA1-1','6-2AA1-2','6-2AA1-3','6-2AA1-4','6-2AA1-5'" +
            ",'6-2AA2-1','6-2AA3-1','6-2AA3-2','6-2AA3-3','6-2AA3-4','6-2AA3-5'" +
            ",'6-2AA4-1','6-2AA4-2','6-2AA4-3','6-2AA4-4','6-2AA4-5'" +
            ",'6-2AA5-1','6-2AA6-1','6-2AA7-1','6-2AA9-1','6-2AA10-1','6-2AA11-1' " +
            "    )\n" +
            "    GROUP BY name\n" +
            ") t2 ON t1.name = t2.name AND t1.data = t2.latest_data;")
    List<Map<String, Object>> getSixBYId65();

    @Select("<script>" +
            "SELECT ROUND(IFNULL(SUM(ADSP), 0), 2) AS ADSP " +
            "FROM (" +
            "    SELECT " +
            "        ADSP, " +
            "        " +
            "        ROW_NUMBER() OVER (PARTITION BY name ORDER BY data DESC) AS rn " +
            "    FROM ytmem " +
            "    WHERE " +
            "        name IN " +
            "        <foreach collection='cmsList' item='name' open='(' separator=',' close=')'>" +
            "            #{name}" +
            "        </foreach> " +
            "       " +
            "        AND data &gt;= #{startDate} " +
            "        AND data &lt; #{endDate} " +
            ") t " +
            "WHERE rn = 1;" +
            "</script>")
    Double gethbxqDemo(Map<String, Object> paramMap);

    @Select("SELECT * FROM power_alarm WHERE alarm_status = #{status} ORDER BY create_time DESC LIMIT 1")
    Map<String,Object> getBystatus(String status);

    @Select("SELECT id, " +
            "alarm_name AS alarmName, " +
            "alarm_content AS alarmContent, " +
            "alarm_title AS alarmTitle, " +
            "alarm_level AS alarmLevel, " +
            "create_time AS createTime, " +
            "update_time AS updateTime, " +
            "alarm_status AS alarmStatus " +
            "FROM power_alarm WHERE id = #{alarmId} LIMIT 1")
    Map<String, Object> getAlarmById(String alarmId);


    @Select("SELECT * FROM power_alarm WHERE alarm_status = '0' ORDER BY create_time DESC LIMIT 1")
    Map<String, Object> getLatestUnconfirmedAlarm();

    @Update("update power_alarm SET alarm_status ='1' where id =#{alarmId} AND alarm_status = '0'")
    void updatealarmById(String alarmId);

    @SelectProvider(type = PowerAlarmSqlProvider.class, method = "queryAlarms")
    List<Map<String, String>> getALLalarm(
            @Param("alarmLevel1") String alarmLevel1,
            @Param("alarmLevel2") String alarmLevel2,
            @Param("status") String status,
            @Param("device") String device,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    // 查询开关状态
    @Select("select DI1 from ytmem where name =#{name} order by data DESC limit 1")
    String getByDI1(String name);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT into power_alarm(alarm_name,create_time,update_time,alarm_content,alarm_title,alarm_level,alarm_status) values " +
            "(#{alarmName},#{createTime},#{updateTime},#{alarmContent},#{alarmTitle},#{alarmLevel},#{alarmStatus})")
    void insertAlarm(PowerAlarm alarm);

    @Select("SELECT id, " +
            "alarm_name AS alarmName, " +
            "alarm_content AS alarmContent, " +
            "alarm_title AS alarmTitle, " +
            "alarm_level AS alarmLevel, " +
            "create_time AS createTime, " +
            "update_time AS updateTime, " +
            "alarm_status AS alarmStatus " +
            "FROM power_alarm " +
            "WHERE alarm_name = #{alarmName} AND alarm_title = #{alarmTitle} AND alarm_status IN ('0','1') " +
            "ORDER BY create_time DESC LIMIT 1")
    PowerAlarm findLatestOpenAlarm(@Param("alarmName") String alarmName,
                                   @Param("alarmTitle") String alarmTitle);

    @Update("UPDATE power_alarm SET alarm_status = '2', update_time = #{updateTime} " +
            "WHERE alarm_name = #{alarmName} AND alarm_title = #{alarmTitle} AND alarm_status IN ('0','1')")
    int reconcileOpenAlarms(@Param("alarmName") String alarmName,
                            @Param("alarmTitle") String alarmTitle,
                            @Param("updateTime") LocalDateTime updateTime);


    @Update("UPDATE power_alarm SET alarm_status = #{alarmStatus}, update_time = #{updateTime}, alarm_content = #{alarmContent} WHERE id = #{id} AND alarm_status IN ('0','1')")
    void updateAlarmState(@Param("id") Integer id,
                          @Param("alarmStatus") String alarmStatus,
                          @Param("updateTime") LocalDateTime updateTime,
                          @Param("alarmContent") String alarmContent);

    @Select("SELECT * FROM power_alarm ORDER BY create_time DESC LIMIT 5")
    List<Map<String, Object>> getrecentalarm();

}
