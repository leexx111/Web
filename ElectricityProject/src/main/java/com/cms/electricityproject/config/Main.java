package com.cms.electricityproject.config;


import com.cms.electricityproject.domain.Ytmem;
import com.cms.electricityproject.server.UserService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 项目辅助数据转换和本地演示逻辑入口。
 */
public class Main {
    // 模拟获取原始 Map 集合的方法
    public static Map<Object, Object> getOriginalMap() {
        Map<Object, Object> originalMap = new LinkedHashMap<>();
        originalMap.put("1AA1-1.A", "12");
        originalMap.put("1AA1-1.B", "13");
        originalMap.put("1AA1-1.C", "14");
        originalMap.put("1AA1-1.D", "15");
        originalMap.put("1AA1-1.E", "16");
        originalMap.put("1AA1-1.F", "17");

        originalMap.put("1AA2-1.A", "12");
        originalMap.put("1AA2-1.B", "13");
        originalMap.put("1AA2-1.C", "14");
        originalMap.put("1AA2-1.D", "15");
        originalMap.put("1AA2-1.E", "16");
        originalMap.put("1AA2-1.F", "17");

        originalMap.put("1AA2-2.A", "12");
        originalMap.put("1AA2-2.B", "13");
        originalMap.put("1AA2-2.C", "14");
        originalMap.put("1AA2-2.D", "15");
        originalMap.put("1AA2-2.E", "16");
        originalMap.put("1AA2-2.F", "17");
        return originalMap;
    }
//    public  static void csjieshou(){
//        Map<Object, Object> hqsj = BacnetTool.Hqsj();
//        Map<String,String> ytm = new LinkedHashMap<>();
//        String key;
//        String value ;
//        String cms ="1AA6-1";
//        // 获取当前时间
//        LocalDateTime currentTime = LocalDateTime.now();
//        // 定义日期时间格式
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
//        // 将当前时间按照指定格式进行格式化
//        String formattedTime = currentTime.format(formatter);
//        for (Map.Entry<Object, Object> entry : hqsj.entrySet()) {
//            key = entry.getKey().toString();  //1AA1-1.A
//            value = entry.getValue().toString(); //12
//
//            if (key.length()>3){
//                String substring = key.substring(0, key.indexOf(".")); //1AA1-1
//                if (substring.equals(cms)){
//                    //如果等于，添加属性信息
//                    ytm.put(key.substring(key.indexOf(".")+1),value);
//                }else{
//                    //如果不等于，添加名称，并且修改校验值
//                    ytm.put("name",cms);
//                    ytm.put("data",formattedTime);
//                    System.out.println("执行保存数据库"+cms);
//                    System.out.println(ytm);
//                    cms=substring;
//                    ytm.put(key.substring(key.indexOf(".")+1),value);
//                }
//            }
//            //循环结束时保存一次
//            if (key.equals("1AA10-7.ADSQ")){
//                ytm.put("name",cms);
//                ytm.put("data",formattedTime);
//                System.out.println("执行保存数据库"+cms);
//                System.out.println(ytm);
//            }
//        }
////        System.out.println("M3封装完成了——————————————————————————————");
////        ytm.put("name","6号核心筒客梯1");
////        // 获取当前时间
////        LocalDateTime currentTime = LocalDateTime.now();
////
////        // 定义日期时间格式
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
////
////        // 将当前时间按照指定格式进行格式化
////        String formattedTime = currentTime.format(formatter);
////        ytm.put("data",formattedTime);
//        //执行一次保存数据库的操作
//
////        UserService userService = new UserService();
////        String string = userService.setYtmemByAll(ytm);
////        if (string.equals("700")){
////            System.out.println("可以下班了");
////        }
////        for (Map.Entry<String, String > entry : ytm.entrySet()) {
////            System.out.println(entry.getKey() + " : " + entry.getValue());
////        }
//
////        if (entry.getKey() =="F"){
////            ytmem.setF((Double) entry.getValue());
////        }
//    }

    public static void main(String[] args) {
//        csjieshou();
        demo02();
    }

    private static void demo05() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        // 将当前时间按照指定格式进行格式化
        String formattedTime = currentTime.format(formatter);
        System.out.println("每10分钟执行一次，现在的时间是："+formattedTime);
    }

    private static void demo04() {
        //拆分为每年每月每日每小时
        String dateTimeStr ="2025-06-11 15:12:43";   // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            // 解析字符串为LocalDateTime对象
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

            // 获取月份和小时
            int month = dateTime.getMonthValue();
            int hour = dateTime.getHour();

            // 判断月份区间
            String monthRange;
            if (month >= 1 && month <= 2) {
                monthRange = "1-2月";
            } else if (month >= 3 && month <= 6) {
                monthRange = "3-6月";
            } else if (month >= 7 && month <= 12) {
                monthRange = "7-12月";
            } else {
                monthRange = "无效月份";
            }

            // 判断小时区间
            boolean isEarlyMorning = hour >= 0 && hour <= 6;

            // 输出结果
            System.out.println("输入的日期时间: " + dateTimeStr);
            System.out.println("月份区间: " + monthRange);
            System.out.println("时间区间: " + hour);
            System.out.println("是否是0点到6点: " + (isEarlyMorning ? "是" : "否"));

        } catch (Exception e) {
            System.out.println("日期时间格式错误: " + e.getMessage());
        }
    }

    public static void demo03(){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate date = LocalDate.parse("2025-05-23", formatter);
//        // 日期加一
//        LocalDate nextDay = date.plusDays(1);
//        String nextDayStr = nextDay.format(formatter);
//        System.out.println(nextDayStr);

        String data = "2025-06-11 15-12-43";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(data, formatter);

        int month = dateTime.getMonthValue();
        int hour = dateTime.getHour();
        System.out.println(month+" "+hour);
    }

    //切割实体类
    public static void demo02(){
        String originalString = "1AA6-1.A";
//        int index = originalString.indexOf("-");
//        if (index != -1) {
            String firstPart = originalString.substring(0, originalString.indexOf("."));
            System.out.println(firstPart); // 输出: Hello
//        }
    }
    //字符串转double类型
    public static void demo01(){
            String str = "314159";

            // 将字符串转换为 double
            double number = Double.parseDouble(str);

            // 使用 DecimalFormat 格式化并保留两位小数
            DecimalFormat df = new DecimalFormat("#.00");
            String formatted = df.format(number/100);

            // 输出结果
            System.out.println("原始字符串: " + str);
            System.out.println("格式化后: " + formatted); // 输出: 3.14

            // 如果需要转回 double 类型（注意：可能会丢失精度）
            double result = Double.parseDouble(formatted);
            System.out.println("转回 double: " + result); // 输出: 3.14

    }

//    public static void main(String[] args) {
//        // 获取原始 Map 集合
//        Map<String, String> originalMap = getOriginalMap();
//
//        // 用于存储每个 Mn 的数据
//        Map<String, Map<String, String>> groupedData = new HashMap<>();
//
//        // 对原始 Map 进行分组
//        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
//            String key = entry.getKey();
//            String prefix = key.split("\\.")[0];
//            groupedData.computeIfAbsent(prefix, k -> new HashMap<>()).put(key, entry.getValue());
//        }
//
//        // 定义每个 Mn 的特殊属性
//        Map<String, Map<String, String>> specialAttributeMap = new HashMap<>();
//        Map<String, String> m3Special = new HashMap<>();
//        m3Special.put("fenzu", "31");
//        specialAttributeMap.put("M3", m3Special);
//
//        Map<String, String> m4Special = new HashMap<>();
//        m4Special.put("fenzu", "12");
//        specialAttributeMap.put("M4", m4Special);
//
//        // 创建实体类列表
//        List<Entity> entities = new ArrayList<>();
//        for (Map.Entry<String, Map<String, String>> entry : groupedData.entrySet()) {
//            String name = entry.getKey();
//            Map<String, String> data = entry.getValue();
//            // 为每个 Mn 添加特殊属性
//            Map<String, String> specialAttributes = specialAttributeMap.getOrDefault(name, new HashMap<>());
//            entities.add(new Entity(name, data, specialAttributes));
//        }
//
//
//        // 调用 Dao 层保存数据
//        Dao dao = new Dao();
//        dao.saveEntities(entities);
//
//    }
    static class Dao {
        public void saveEntities(List<Entity> entities) {
            // 这里可以实现具体的 SQL 保存逻辑
            for (Entity entity : entities) {
                System.out.println("Saving entity: " + entity);
                if (entity.getName().equals("M3")){
                    System.out.println(entity.getData());
                }

            }
        }
    }

    // 假设的实体类
    static class Entity {
        private String name;
        private Map<String, String> data;
        private Map<String, String> specialAttributes;

        public Entity(String name, Map<String, String> data, Map<String, String> specialAttributes) {
            this.name = name;
            this.data = data;
            this.specialAttributes = specialAttributes;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getData() {
            return data;
        }

        public Map<String, String> getSpecialAttributes() {
            return specialAttributes;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "name='" + name + '\'' +
                    ", data=" + data +
                    ", specialAttributes=" + specialAttributes +
                    '}';
        }
    }

}