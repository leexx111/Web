package com.cms.electricityproject.config;

import com.cms.electricityproject.domain.Ytmem;
import com.cms.electricityproject.server.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 定时器工具类
 */
@Component
/**
 * 定义周期性采集、数据库保存和报警轮询任务。
 */
public class ScheduledTasks {

    int i =0;
    @Scheduled(cron = "0 0/10 * * * *")
//    @Scheduled(cron = "0 * * * * *")
    /**
     * 每十分钟采集电气数据并分发到主配电表、散表和配电室汇总保存流程。
     */
    public  void DingShiQiDemo02(){
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        // 将当前时间按照指定格式进行格式化
        String formattedTime = currentTime.format(formatter);
        System.out.println("每10分钟执行一次，现在的时间是："+formattedTime);

        // 拆分日期和时间部分
        String[] dateTimeParts = formattedTime.split(" ");
        if (dateTimeParts.length < 2) {
            System.out.println("格式无效");
            return;
        }
        // 拆分时间部分（时-分-秒）
        String[] timeParts = dateTimeParts[1].split("-");
        if (timeParts.length < 3) {
            System.out.println("时间格式无效");
            return;
        }
        // 提取分钟部分（索引1）
        String minute = timeParts[1];
        if ("00".equals(minute)) {
            System.out.println("分钟段为00，执行保存电费数据操作");
//            E325dianfei(formattedTime,1);
            dianfei(formattedTime,1);
            shanbiaodianfei(formattedTime,1);
            // 这里添加你的业务逻辑
            //添加大区信息库内容，四个总表的有功减去无功
            userService.setOnePDS(formattedTime,1);
        } else {
//            System.out.println("分钟段不是00");
            i++;
            System.out.println("定时器任务执行了: " + i+"次");
//            E325dianfei(formattedTime,2);
            dianfei(formattedTime,2);
            shanbiaodianfei(formattedTime,2);
            userService.setOnePDS(formattedTime,2);
//            userService.setOnePDS();
        }
    }



    HashMap<Object, Object> QJmap = new HashMap();
    private final Map<Object, String> pendingDiValues = new HashMap<>();
    private final Map<Object, Integer> pendingDiCounts = new HashMap<>();

    int KGbj =1;

    @Scheduled(cron = "0/5 * * * * *")
    /**
     * 周期轮询 DI 状态；新状态连续确认后才创建/恢复报警并广播事件。
     */
    public void KGbaojing() {
        Map<Object, Object> map = bacnetTool.baojing();

        // A failed or missing read cannot confirm a pending transition.
        pendingDiValues.keySet().removeIf(key -> !map.containsKey(key));
        pendingDiCounts.keySet().removeIf(key -> !map.containsKey(key));

        String key;
        String value;
        List<Object> changedList = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            key = entry.getKey().toString();   // 例如 4AA8.DI1
            value = entry.getValue().toString(); // 1 或 0
            if ("1.0".equals(value)) value = "1";
            if ("0.0".equals(value)) value = "0";

            // 2AA11-1 is intentionally excluded from DI alarm change handling.
            if (key.equals("2AA11-1") || key.startsWith("2AA11-1.")
                    || key.equalsIgnoreCase("3AA16-3.DI1")) {
                continue;
            }

            if (!QJmap.containsKey(entry.getKey())) {
                // Establish the initial state without inventing a transition.
                String initialPending = pendingDiValues.get(entry.getKey());
                int initialCount = value.equals(initialPending)
                        ? pendingDiCounts.getOrDefault(entry.getKey(), 0) + 1 : 1;
                pendingDiValues.put(entry.getKey(), value);
                pendingDiCounts.put(entry.getKey(), initialCount);
                if (initialCount < 2) continue;
                if ("1".equals(value)) {
                    userService.reconcileInitialDiNormal(key);
                }
                QJmap.put(entry.getKey(), value);
                pendingDiValues.remove(entry.getKey());
                pendingDiCounts.remove(entry.getKey());
                continue;
            }

            String oldValue = QJmap.get(entry.getKey()).toString();
            if (oldValue.equals(value)) {
                pendingDiValues.remove(entry.getKey());
                pendingDiCounts.remove(entry.getKey());
                continue;
            }

            String pendingValue = pendingDiValues.get(entry.getKey());
            int stableCount = value.equals(pendingValue)
                    ? pendingDiCounts.getOrDefault(entry.getKey(), 0) + 1
                    : 1;
            pendingDiValues.put(entry.getKey(), value);
            pendingDiCounts.put(entry.getKey(), stableCount);
            if (stableCount < 2) {
                continue;
            }

            System.out.println("DI变化详情 -> key=" + key + ", oldValue=" + oldValue + ", newValue=" + value);
            changedList.add(key);

            Map<String, Object> alarmEvent = userService.handleDiAlarmChange(key, oldValue, value);
            if (alarmEvent != null) {
                alarmEvent.put("pointKey", key);
                alarmEvent.put("oldValue", oldValue);
                alarmEvent.put("newValue", value);
                System.out.println("准备广播报警事件: " + alarmEvent);
                userService.broadcastMessage(alarmEvent);
            }
            QJmap.put(entry.getKey(), value);
            pendingDiValues.remove(entry.getKey());
            pendingDiCounts.remove(entry.getKey());
        }

        if (!changedList.isEmpty()) {
            System.out.println("本次检测到DI状态变化: " + changedList);
        } else {
            System.out.println("本次未检测到DI状态变化");
        }

        KGbj++;
    }

    @Async("asyncTaskExecutor")
    /**
     * 将指定测点的历史数据修复任务提交到异步线程池。
     */
    public void SYshujuxiufu(String names){
        //查询出10月最初的数据并做记录 2025-10-01 00:00:00
        //拿查询到的数据跟上一条数据做计算
        //拿时间和计算的值更新到dianliang表内

        userService.getSYshujuxiufu(names);
    }

    /**
     * 比较两个Map的相同键值，并更新旧Map
     * @param oldMap 旧Map（将被更新）
     * @param newMap 新Map（提供比较数据）
     * @return 记录值不一致的键和旧值
     */
    public static Map<String, String> compareAndUpdateMaps(Map<String, String> oldMap, Map<String, String> newMap) {
        Map<String, String> diffMap = new HashMap<>();

        // 遍历新Map的键，检查旧Map是否包含相同键
        for (String key : newMap.keySet()) {
            if (oldMap.containsKey(key)) {
                // 获取新旧值并比较
                String oldValue = oldMap.get(key).toString();
                String newValue = newMap.get(key).toString();

                // 比较值是否不同（使用equals确保内容一致，而非引用相等）
                if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
                    // 记录差异
                    diffMap.put(key, oldValue == null ? "null" : oldValue.toString());
                    // 更新旧Map的值
                    oldMap.put(key, newValue.toString()); // 假设值可转为String

                    // 弹出提示（实际开发中可替换为日志或UI提示）
                    System.out.println("提示：键 '" + key + "' 的值不一致，已更新");
                }
            }
        }

        return diffMap;
    }



    @Autowired
    private UserService userService;

    @Autowired
    private BacnetTool bacnetTool;


    public void cms(){
//        Map<Object, Object> hqsj = BacnetTool.Hqsj(); //获取数据
    }
    /**
     * 读取常规 BACnet 数据，按设备名聚合测量值并写入电气数据表。
     */
    public void dianfei(String formattedTime,int biaoshi){
        Map<Object, Object> hqsj = bacnetTool.Hqsj(); //获取数据

        Set<Map.Entry<Object, Object>> entries = hqsj.entrySet();
        int size = entries.size();
        Map<String,String> ytm = new LinkedHashMap<>();
        String key,value;
        String cms ="1AA6-1";

        int count = 0;
        //循环遍历低压信息
        for (Map.Entry<Object, Object> entry : hqsj.entrySet()) {
            key = entry.getKey().toString();  //1AA1-1.A
            value = entry.getValue().toString(); //12

            count++;
            if (key.length()>3){
                String substring="";
                if (key.contains("Modbus")){
                    substring = key.substring(17, key.lastIndexOf(".")); //1AA1-1
                }else {
                    substring = key.substring(0, key.indexOf(".")); //1AA1-1
                }
                if (substring.equals(cms)){
                    //如果等于，添加属性信息
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }else{
                    //如果不等于，添加名称，并且修改校验值
                    ytm.put("name",cms);
                    ytm.put("data",formattedTime);
//                    System.out.println("执行保存数据库"+cms);
                    System.out.println("执行保存数据库"+ytm.get("name"));
                    userService.setYtmemByAll(ytm,biaoshi);
//                    System.out.println(ytm);
                    cms=substring;
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }
            }
            //循环结束时保存一次
            if (count == size){
                ytm.put("name",cms);
                ytm.put("data",formattedTime);
                System.out.println("执行保存数据库"+ytm.get("name"));
                userService.setYtmemByAll(ytm,biaoshi);
                System.out.println("执行保存完毕------");
            }
        }
        //处理高压信息

    }
//散表
    /**
     * 读取散表数据，按设备名聚合后保存到散表数据表。
     */
    public void shanbiaodianfei(String formattedTime,int biaoshi){
        Map<Object, Object> hqsbsj = bacnetTool.Hqsbsj(); //获取数据

        Set<Map.Entry<Object, Object>> entries = hqsbsj.entrySet();
        int size = entries.size();
        Map<String,String> ytm = new LinkedHashMap<>();
        String key,value;
        String cms ="(19)ALT-B1-G2";

        int count = 0;
        //循环遍历低压信息
        for (Map.Entry<Object, Object> entry : hqsbsj.entrySet()) {
            key = entry.getKey().toString();  //1AA1-1.A
            value = entry.getValue().toString(); //12

            count++;
            if (key.length()>3){
                String substring="";
                if (key.contains("Modbus")){
                    substring = key.substring(17, key.lastIndexOf(".")); //1AA1-1
                }else {
                    substring = key.substring(0, key.indexOf(".")); //1AA1-1
                }
                if (substring.equals(cms)){
                    //如果等于，添加属性信息
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }else{
                    //如果不等于，添加名称，并且修改校验值
                    ytm.put("name",cms);
                    ytm.put("data",formattedTime);
//                    System.out.println("执行保存数据库"+cms);
                    System.out.println("执行保存数据库"+ytm.get("name"));
                    userService.setHushByAll(ytm,biaoshi);
//                    System.out.println(ytm);
                    cms=substring;
                    ytm.put("ADSP",value);

                }
            }
            //循环结束时保存一次
            if (count == size){
                ytm.put("name",cms);
                ytm.put("data",formattedTime);
                System.out.println("执行保存数据库"+ytm.get("name"));
                userService.setHushByAll(ytm,biaoshi);
                System.out.println("执行保存完毕------");
            }
        }
        //处理高压信息

    }
    //散表
    /**
     * 读取 325 网关数据并按设备名分组保存。
     */
    public void E325dianfei(String formattedTime,int biaoshi){
        Map<Object, Object> hq325sj = bacnetTool.Hq325sj(); //获取数据

        Set<Map.Entry<Object, Object>> entries = hq325sj.entrySet();
        int size = entries.size();
        Map<String,String> ytm = new LinkedHashMap<>();
        String key,value;
        String cms ="(19)ALT-B1-G2";

        int count = 0;
        //循环遍历低压信息
        for (Map.Entry<Object, Object> entry : hq325sj.entrySet()) {
            key = entry.getKey().toString();  //1AA1-1.A
            value = entry.getValue().toString(); //12

            count++;
            if (key.length()>3){
                String substring="";
                if (key.contains("Modbus")){
                    substring = key.substring(17, key.lastIndexOf(".")); //1AA1-1
                }else {
                    substring = key.substring(0, key.indexOf(".")); //1AA1-1
                }
                if (substring.equals(cms)){
                    //如果等于，添加属性信息
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }else{
                    //如果不等于，添加名称，并且修改校验值
                    ytm.put("name",cms);
                    ytm.put("data",formattedTime);
//                    System.out.println("执行保存数据库"+cms);
                    System.out.println("执行保存数据库"+ytm.get("name"));
                    userService.set325ByAll(ytm,biaoshi);
//                    System.out.println(ytm);
                    cms=substring;
                    ytm.put("ADSP",value);

                }
            }
            //循环结束时保存一次
            if (count == size){
                ytm.put("name",cms);
                ytm.put("data",formattedTime);
                System.out.println("执行保存数据库"+ytm.get("name"));
                userService.set325ByAll(ytm,biaoshi);
                System.out.println("执行保存完毕------");
            }
        }
        //处理高压信息

    }

    //滤波器工具类
    @Async("asyncTaskExecutor")
    @Scheduled(cron = "0 0/10 * * * *")
    /**
     * 周期读取滤波器点位并按设备分组写入数据库。
     */
    public void lbqbaocun(){
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        // 将当前时间按照指定格式进行格式化
        String formattedTime = currentTime.format(formatter);

        Map<Object, Object> lbqduqu = bacnetTool.lbqduqu();
        Set<Map.Entry<Object, Object>> entries = lbqduqu.entrySet();
        int size = entries.size();
        int count = 0;
        String key ="",value="",cms="1AA5";
        Map<String,String> ytm = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> entry : lbqduqu.entrySet()) {
            key = entry.getKey().toString();  //1AA5.Ib
            value = entry.getValue().toString(); //281.0
            //判断为第一次循环
//            if (count ==0){
//                cms = key.substring(0, key.indexOf("."));
//                System.out.println("执行一次判断==========此时的值为"+key);
//            }
            count++;
            if (key.length()>3) {
                String substring = key.substring(0, key.indexOf(".")); //1AA1-1
                if (substring.equals(cms)){
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }else {
                    ytm.put("name",cms);
                    ytm.put("data",formattedTime);
                    System.out.println("执行保存滤波数据库"+ytm.get("name"));

                    //向滤波器中添加一条数据
                    userService.setLBQByALL(ytm);
                    cms=substring;
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }
            }
            // 判断是否为最后一次循环
            if (count == size) {
                ytm.put("name",cms);
                ytm.put("data",formattedTime);
                userService.setLBQByALL(ytm);
                System.out.println("执行保存滤波器完毕------");
            }

        }

    }


    //变压器工具类
//    @Async("asyncTaskExecutor")
//    @Scheduled(cron = "0 0/10 * * * *")
    /**
     * 读取变压器测量点并按设备分组保存。
     */
    public void byqbaocun(){
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        // 将当前时间按照指定格式进行格式化
        String formattedTime = currentTime.format(formatter);

        Map<Object, Object> lbqduqu = bacnetTool.byqduqu();
        Set<Map.Entry<Object, Object>> entries = lbqduqu.entrySet();
        int size = entries.size();
        int count = 0;
        String key ="",value="",cms="1AA5";
        Map<String,String> ytm = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> entry : lbqduqu.entrySet()) {
            key = entry.getKey().toString();  //1AA5.Ib
            value = entry.getValue().toString(); //281.0
            //判断为第一次循环
//            if (count ==0){
//                cms = key.substring(0, key.indexOf("."));
//                System.out.println("执行一次判断==========此时的值为"+key);
//            }
            count++;
            if (key.length()>3) {
                String substring = key.substring(0, key.indexOf(".")); //1AA1-1
                if (substring.equals(cms)){
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }else {
                    ytm.put("name",cms);
                    ytm.put("data",formattedTime);
                    System.out.println("执行保存滤波数据库"+ytm.get("name"));

                    //向滤波器中添加一条数据
                    userService.setLBQByALL(ytm);
                    cms=substring;
                    ytm.put(key.substring(key.indexOf(".")+1),value);
                }
            }
            // 判断是否为最后一次循环
            if (count == size) {
                ytm.put("name",cms);
                ytm.put("data",formattedTime);
                userService.setLBQByALL(ytm);
                System.out.println("执行保存滤波器完毕------");
            }

        }
    }

    // 测试主线程（定时任务）
//    @Scheduled(cron = "0/5 * * * * *")
//    public void demoYibu1() throws InterruptedException {
//        // 【优化 2】缩短执行时间，方便观察
//        for (int j = 0; j < 500; j++) {
//            System.out.println("正常线程测试-" + j + "次----- 线程名：" + Thread.currentThread().getName());
//            bacnetTool.yibuDemo(2);
//        }
//    }
//
//    // 测试异步方法（定时任务 + 异步）
//    @Async("asyncTaskExecutor")
//    @Scheduled(cron = "0/10 * * * * *")
//    public void asyncMethod() {
//        System.out.println("异步方法执行，线程名：" + Thread.currentThread().getName());
//        bacnetTool.yibuDemo(1);
//    }

}
