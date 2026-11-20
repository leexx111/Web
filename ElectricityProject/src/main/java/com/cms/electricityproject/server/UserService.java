package com.cms.electricityproject.server;

import com.cms.electricityproject.dao.HDBDao;
import com.cms.electricityproject.dao.UserRepository;
import com.cms.electricityproject.domain.Heidb;
import com.cms.electricityproject.domain.PowerAlarm;
import com.cms.electricityproject.domain.Ytmem;
import com.cms.electricityproject.websoket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Key;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;





@Service
/**
 * 实现电量数据、报警状态、统计分析等核心业务逻辑。
 */
public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private static final String ALARM_STATUS_UNCONFIRMED = "0";
    private static final String ALARM_STATUS_CONFIRMED = "1";
    private static final String ALARM_STATUS_RECOVERED = "2";
    private static final String TRIP_ALARM_TITLE = "跳闸报警";
    private static final String TRIP_ALARM_CONTENT = "设备由合闸变为断开";
    private static final String RECOVER_ALARM_CONTENT = "设备由断开变为合闸";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HDBDao hdbDao;

    public List<Heidb> getNow() {
        return hdbDao.getNow();
    }

    public List<Ytmem> getYtmemAll() {
        return hdbDao.getYtmemAll();
    }
    public List<Double> getByLastMonthMeanW(String name) {
        return hdbDao.getByLastMonthMeanW(name);
    }


    public List<Heidb> getAllUsers() {
        return userRepository.findAll();
    }

    public Heidb saveUser(Heidb user) {
        return userRepository.save(user);
    }

    public Heidb getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public List<Double> getByThisMonthMeanW() {
        List<Double> byThisMonthMeanW = hdbDao.getByThisMonthMeanW();
//        byThisMonthMeanW.get(0);
//        //处理日期，1-30天
//        ArrayList<String> strings = new ArrayList<>(Collections.nCopies(31, "0"));
//        for (Map<String, String> row : byThisMonthMeanW) {
//            String day = row.get("day");
//            String avgW = row.get("avg_w");
//            int index = Integer.parseInt(day) - 1;
//            if (index >= 0 && index < 31) {
//                strings.set(index, avgW);
//            }
//        }
        return byThisMonthMeanW;
    }

    public List<Double> getByLastMonthMeanEQ(String name) {
        // 1. 创建一个包含所有有效值的列表
        List<String> validNames = Arrays.asList("1号配电室", "2号配电室", "6号配电室");

        // 2. 使用 List.contains() 进行判断
        //    - 为了避免空指针，可以先判断 name 是否为 null
        if (name == null || !validNames.contains(name)) {
            name = "1号配电室";
        }
        return hdbDao.getByLastMonthMeanEQ(name);
    }

    //查询负荷详情  【总负荷，每日最高负荷，本月最高负荷，本年最高负荷，本年最低负荷】 /getfhxq
    public List<String> getfhxq(String name) {
        List<String> fhxq = new ArrayList<String>();
        fhxq.add(hdbDao.getWnew(name));//当前负荷
        fhxq.add(hdbDao.getTodayMax(name));//本日日最高负荷
        fhxq.add(hdbDao.getmonthMax(name));//本月最高负荷
        fhxq.add(hdbDao.getByYearMax(name));//本年最高负荷
        fhxq.add(hdbDao.getByYearMin(name));//本年最低负荷
        return fhxq;
    }

//    //查询环比详情 【月总电量，环比电量，月环比，年总电量，环比电量，年环比】
//    public List<Double> gethbxq() {
//        List<Double> hbxq = new ArrayList<>();
//        Double zdl = hdbDao.getzdl();//最新电量
//        if(zdl == null){
//            zdl = 0.0;
//        }
//        Double byzxdl = hdbDao.getbyzxdl("'1AA1-1','2AA15-1','3AA1-1','4AA17-1'");//本月1号电量
//        if(byzxdl == null){
//            byzxdl = 0.0;
//        }
//        Double syzxdl = hdbDao.getsyzxdl("'1AA1-1','2AA15-1','3AA1-1','4AA17-1'");//上月1号电量
//        if(syzxdl == null){
//            syzxdl = 0.0;
//        }
//        Double bnzxdl = hdbDao.getbnzxdl("'1AA1-1','2AA15-1','3AA1-1','4AA17-1'");//本年1号电量
//        if(bnzxdl == null){
//            bnzxdl = 0.0;
//        }
//        Double qnzxdl = hdbDao.getqnzxdl("'1AA1-1','2AA15-1','3AA1-1','4AA17-1'");//去年最新
//        if(qnzxdl == null){
//            qnzxdl = 0.0;
//        }
//        if (byzxdl <1){
//            hbxq.add(0.0);
//        }else {
//            String byzdl = String.format("%.2f", (zdl-byzxdl)/1000);
//            hbxq.add(Double.parseDouble(byzdl));//本月总电量
//        }
//        if (syzxdl <1){
//            hbxq.add(0.0);
//            hbxq.add(0.0);
//        }else {
//            Double hbdl =(zdl-byzxdl)-(byzxdl-syzxdl);
//            String hbdls = String.format("%.2f", hbdl);
////            hbxq.add(Double.parseDouble(hbdls));//环比电量
//            String format = String.format("%.2f", (byzxdl - syzxdl) / 1000);
//            hbxq.add(Double.parseDouble(format));//上月电量
//            String result = String.format("%.2f", hbdl/(byzxdl-syzxdl));
//            hbxq.add(Double.parseDouble(result));//月环比
//        }
//        //年总电量
//        hbxq.add(Double.parseDouble(String.format("%.2f", (zdl- bnzxdl)/1000 )));
//
//        if (qnzxdl<1){
//            hbxq.add(0.0);
//            hbxq.add(0.0);
//        }else {
//            //环比电量
//            Double nhbdl =(zdl-bnzxdl)-(bnzxdl-qnzxdl);
//            String nhbdls = String.format("%.2f", nhbdl);
//            String qnz = String.format("%.2f", (bnzxdl - qnzxdl) / 1000);
//            hbxq.add(Double.parseDouble(qnz));//去年总电量
//            String nresult = String.format("%.2f", nhbdl/(zdl- bnzxdl));
//            hbxq.add(Double.parseDouble(nresult));//年环比
//        }
//        return hbxq;
//    }
public List<Double> gethbxq() {
    long startTime = System.currentTimeMillis();
    System.out.println("[开始] 接口总开始时间: " + new Date());

    List<Double> hbxq = new ArrayList<>();

    // 1. 查询最新电量
    long query1Start = System.currentTimeMillis();
    Double zdl = hdbDao.getzdl();//最新电量
    long query1End = System.currentTimeMillis();
    System.out.println("[耗时] getzdl查询: " + (query1End - query1Start) + "ms");

    if(zdl == null){
        zdl = 0.0;
    }
    System.out.println("[数据] 最新电量zdl: " + zdl);

    // 2. 查询本月1号电量
    long query2Start = System.currentTimeMillis();
    String deviceIds = "'1AA1-1','2AA15-1','3AA1-1','4AA17-1'";
    Double byzxdl = hdbDao.getbyzxdl(deviceIds);//本月1号电量
    long query2End = System.currentTimeMillis();
    System.out.println("[耗时] getbyzxdl查询: " + (query2End - query2Start) + "ms");

    if(byzxdl == null){
        byzxdl = 0.0;
    }
    System.out.println("[数据] 本月1号电量byzxdl: " + byzxdl);

    // 3. 查询上月1号电量
    long query3Start = System.currentTimeMillis();
    Double syzxdl = hdbDao.getsyzxdl(deviceIds);//上月1号电量
    long query3End = System.currentTimeMillis();
    System.out.println("[耗时] getsyzxdl查询: " + (query3End - query3Start) + "ms");

    if(syzxdl == null){
        syzxdl = 0.0;
    }
    System.out.println("[数据] 上月1号电量syzxdl: " + syzxdl);

    // 4. 查询本年1号电量
    long query4Start = System.currentTimeMillis();
    Double bnzxdl = hdbDao.getbnzxdl(deviceIds);//本年1号电量
    long query4End = System.currentTimeMillis();
    System.out.println("[耗时] getbnzxdl查询: " + (query4End - query4Start) + "ms");

    if(bnzxdl == null){
        bnzxdl = 0.0;
    }
    System.out.println("[数据] 本年1号电量bnzxdl: " + bnzxdl);

    // 5. 查询去年最新电量
    long query5Start = System.currentTimeMillis();
    Double qnzxdl = hdbDao.getqnzxdl(deviceIds);//去年最新
    long query5End = System.currentTimeMillis();
    System.out.println("[耗时] getqnzxdl查询: " + (query5End - query5Start) + "ms");

    if(qnzxdl == null){
        qnzxdl = 0.0;
    }
    System.out.println("[数据] 去年最新电量qnzxdl: " + qnzxdl);

    // 计算部分开始
    long calcStart = System.currentTimeMillis();

    if (byzxdl <1){
        hbxq.add(0.0);
    }else {
        String byzdl = String.format("%.2f", (zdl-byzxdl)/1000);
        hbxq.add(Double.parseDouble(byzdl));//本月总电量
    }

    if (syzxdl <1){
        hbxq.add(0.0);
        hbxq.add(0.0);
    }else {
        Double hbdl =(zdl-byzxdl)-(byzxdl-syzxdl);
        String hbdls = String.format("%.2f", hbdl);
//            hbxq.add(Double.parseDouble(hbdls));//环比电量
        String format = String.format("%.2f", (byzxdl - syzxdl) / 1000);
        hbxq.add(Double.parseDouble(format));//上月电量
        String result = String.format("%.2f", hbdl/(byzxdl-syzxdl));
        hbxq.add(Double.parseDouble(result));//月环比
    }

    //年总电量
    hbxq.add(Double.parseDouble(String.format("%.2f", (zdl- bnzxdl)/1000 )));

    if (qnzxdl<1){
        hbxq.add(0.0);
        hbxq.add(0.0);
    }else {
        //环比电量
        Double nhbdl =(zdl-bnzxdl)-(bnzxdl-qnzxdl);
        String nhbdls = String.format("%.2f", nhbdl);
        String qnz = String.format("%.2f", (bnzxdl - qnzxdl) / 1000);
        hbxq.add(Double.parseDouble(qnz));//去年总电量
        String nresult = String.format("%.2f", nhbdl/(zdl- bnzxdl));
        hbxq.add(Double.parseDouble(nresult));//年环比
    }

    long calcEnd = System.currentTimeMillis();
    System.out.println("[耗时] 计算逻辑耗时: " + (calcEnd - calcStart) + "ms");

    long totalEnd = System.currentTimeMillis();
    System.out.println("[结束] 接口总耗时: " + (totalEnd - startTime) + "ms");
    System.out.println("[结束] 接口结束时间: " + new Date());

    return hbxq;
}

    public List<Map<String, Object>> getByjrfh(String data,String name) {
        //返回今日的负荷数组
        // 解析字符串为LocalDate对象
        LocalDate cms = LocalDate.parse(data);

        // 获取前一天的日期
        LocalDate previousDay = cms.minusDays(1);

        // 转换回字符串（保持原格式）
        String data2 = previousDay.toString();
        List<Map<String, Object>> result = new ArrayList<>();
//        System.out.println("执行了");
        List<Map<String, Object>> newByjrfh = hdbDao.getNewByjrfh(data,name);
        List<Map<String, Object>> newByjrfh2 = hdbDao.getNewByjrfh(data2,name);

        for (Map<String, Object> map : newByjrfh) {
            Map<String, Object> merged = new HashMap<>(map); // 复制今日数据
            merged.put("zr", null); // 初始化昨日值为null
            result.add(merged);
        }
         // 2. 处理昨日数据：已存在的时间点更新zr，不存在的新增记录
            for (Map<String, Object> yMap : newByjrfh2) {
                String yTime = yMap.get("shijian").toString();
                Object yJr = yMap.get("jr");
                boolean found = false;

                // 查找是否有相同时间的记录
                for (Map<String, Object> rMap : result) {
                    String rTime =  rMap.get("shijian").toString();
                    if (yTime != null && yTime.equals(rTime)) {
                        rMap.put("zr", yJr); // 找到则更新zr
                        found = true;
                        break;
                    }
                }

                // 没找到则新增一条记录（今日jr为null，昨日zr为对应值）
                if (!found) {
                    Map<String, Object> newRecord = new HashMap<>();
                    newRecord.put("shijian", yTime);
                    newRecord.put("jr", null);
                    newRecord.put("zr", yJr);
                    result.add(newRecord);
                }
            }

//        System.out.println(result+"查询到的总数据");
        return result;
    }

    public double[] getByzrfh() {
        //返回昨日的负荷数组
        return hdbDao.getByzrfh();
    }

    public Map<String, Object> getsbjcTableData(String id) {
        //查询无功补偿功率  根据id查询数据的最新值
        //一条sql查询无功功率，A相，B相，C相电流，总功率因数  Q，la，lb，lc，pf
        Ytmem yth =hdbDao.getsbjcTableData(id);
        // 额外查询电容柜温度
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("无功补偿功率:", yth.getQ()+"kvar");
        dataMap.put("电容柜A相电流:", yth.getIa()+"A");
        dataMap.put("电容柜B相电流:", yth.getIb()+"A");
        dataMap.put("电容柜C相电流:", yth.getIc()+"A");
        dataMap.put("电容柜温度:", "");
        dataMap.put("总功率因数1:", yth.getPf());

        return dataMap;
    }

    public List<List<Object>> getdnnjcs(String id) {

        Ytmem yth =hdbDao.getsbjcTableData(id);
        List<List<Object>> outerList = new ArrayList<>();
        //每个集合里面为 总 a b c
        // 有功功率
        List<Object> innerList1 = new ArrayList<>();
        innerList1.add("有功功率(kW)");
        innerList1.add(yth.getW()); //w
        innerList1.add(yth.getWa()); //wa
        innerList1.add(yth.getWb()); //wb
        innerList1.add(yth.getWc()); //wc
        outerList.add(innerList1);

        // 无功功率
        List<Object> innerList2 = new ArrayList<>();
        innerList2.add("无功功率(kVar)");
        innerList2.add(yth.getQ()); //q
        innerList2.add(yth.getQa()); //qa
        innerList2.add(yth.getQb()); //qb
        innerList2.add(yth.getQc()); //qc
        outerList.add(innerList2); // 无功功率

        List<Object> innerList3 = new ArrayList<>();
        innerList3.add("功率因数");
        innerList3.add(yth.getPf()); //pf
        innerList3.add(yth.getPFa()); //pfa
        innerList3.add(yth.getPFb()); //pfb
        innerList3.add(yth.getPFc()); //pfc
        outerList.add(innerList3);

        List<Object> innerList4 = new ArrayList<>();
        innerList4.add("视在功率(Kva)"); //靠算
        innerList4.add(yth.getS());
        innerList4.add(yth.getSa());
        innerList4.add(yth.getSb());
        innerList4.add(yth.getSc());
        outerList.add(innerList4);

        List<Object> innerList5 = new ArrayList<>();
        innerList5.add("电压（V）");
        innerList5.add("-"); // -
        innerList5.add(yth.getUan()); //uan
        innerList5.add(yth.getUbn()); //ubn
        innerList5.add(yth.getUcn()); //ucn
        outerList.add(innerList5);

        List<Object> innerList6 = new ArrayList<>();
        innerList6.add("电流（A）");
        innerList6.add("-"); //-
        innerList6.add(yth.getIa()); //la
        innerList6.add(yth.getIb()); //lb
        innerList6.add(yth.getIc()); //lc
        outerList.add(innerList6);

        return outerList;
    }

    //渲染表数据监测方法2
    public Map<String, Object> dnnjcs2(String id) {

        Ytmem yth =hdbDao.getsbjcTableData(id);
        Map<String, Object> dataMap = new HashMap<>();
        //计算零序电流值 （la+lb+lc）/ 3
        double lxdl =(yth.getIa()+yth.getIb()+yth.getIc()) / 3;
        // 创建 DecimalFormat 对象，指定格式
        DecimalFormat df = new DecimalFormat("#.##");
        // 格式化结果
        String formattedResult = df.format(lxdl);
        dataMap.put("number", formattedResult);
        dataMap.put("zong", yth.getF());
        dataMap.put("ady", "-");//具体总电压谐波值
        dataMap.put("dby", "-");//具体总电流谐波含量值
        return dataMap;
    }

    public Map<String, Object> dnnjcs3(String id) {
        Ytmem yth =hdbDao.getsbjcTableData(id);
        Map<String, Object> dataMap = new HashMap<>();
        // 创建 DecimalFormat 对象，指定格式
        DecimalFormat df = new DecimalFormat("#.##");
        dataMap.put("number", "电能示值");
        dataMap.put("yzx", yth.getAdsp());
        dataMap.put("yfx", "-");//具体总电压谐波值
        dataMap.put("wzx", yth.getAdsq());//具体总电流谐波含量值
        dataMap.put("wfx", "-");//具体总电流谐波含量值
        return dataMap;
    }

    public List<List<Object>> getcsjz(String id) {
        List<List<Object>> allList = new ArrayList<>();

        //时间转换
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Object> innerList1 = new ArrayList<>();
        innerList1.add("总有功功率(kW)");

        List<Object> innerList2 = new ArrayList<>();
        innerList2.add("总有功最大需量(KW)");
        List<Map<String , Object>> wMinMaxValues = hdbDao.getWMinMaxValues(id);
        if (!wMinMaxValues.isEmpty()) {
            innerList1.add( wMinMaxValues.get(0).get("w"));
            innerList2.add( wMinMaxValues.get(0).get("w"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) wMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList1.add(formattedData);
            innerList2.add(formattedData);
            if (wMinMaxValues.size() > 1) {
                innerList1.add(wMinMaxValues.get(1).get("w"));
                innerList2.add(wMinMaxValues.get(1).get("w"));
                LocalDateTime data2 = (LocalDateTime) wMinMaxValues.get(1).get("data");
                String formattedData2 = data2.format(formatter);
                innerList1.add(formattedData2);
                innerList2.add(formattedData2);
            }
        }
        allList.add(innerList1);
        allList.add(innerList2);


        List<Map<String , Object>> pfMinMaxValues = hdbDao.getPFMinMaxValues(id);
//        System.out.println(pfMinMaxValues.toString());
        List<Object> innerList3 = new ArrayList<>();
        innerList3.add("总功率因数(%)");

        if (!pfMinMaxValues.isEmpty()){
            innerList3.add( pfMinMaxValues.get(0).get("PF"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) pfMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList3.add(formattedData);
            if (pfMinMaxValues.size() > 1) {
                innerList3.add(pfMinMaxValues.get(1).get("PF"));
                LocalDateTime data2 = (LocalDateTime) pfMinMaxValues.get(1).get("data");
                String formattedData2 = data2.format(formatter);
                innerList3.add(formattedData2);
            }
        }
        allList.add(innerList3);


        List<Map<String , Object>> uanMinMaxValues = hdbDao.getUanMinMaxValues(id);
        List<Object> innerList4 = new ArrayList<>();
        innerList4.add("A项电压(V)");
        if (!uanMinMaxValues.isEmpty()){
            innerList4.add( uanMinMaxValues.get(0).get("Uan"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) uanMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList4.add(formattedData);
            if (uanMinMaxValues.size() > 1) {
                innerList4.add(uanMinMaxValues.get(1).get("Uan"));
                LocalDateTime data2 = (LocalDateTime) uanMinMaxValues.get(1).get("data");
                String formattedData2 = data2.format(formatter);
                innerList4.add(formattedData2);
            }
        }
        allList.add(innerList4);

        List<Object> innerList5 = new ArrayList<>();
        innerList5.add("B项电压(V)");
        List<Map<String , Object>> ubnMinMaxValues = hdbDao.getUbnMinMaxValues(id);
        if (!ubnMinMaxValues.isEmpty()){
            innerList5.add( ubnMinMaxValues.get(0).get("Ubn"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) ubnMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList5.add(formattedData);
            if (ubnMinMaxValues.size() > 1) {
                innerList5.add(ubnMinMaxValues.get(1).get("Ubn"));
                LocalDateTime data2 = (LocalDateTime) ubnMinMaxValues.get(1).get("data");
                String formattedData2 = data2.format(formatter);
                innerList5.add(formattedData2);
            }
        }
        allList.add(innerList5);

        List<Object> innerList6 = new ArrayList<>();
        innerList6.add("C项电压(V)");
        List<Map<String , Object>> ucnMinMaxValues = hdbDao.getUcnMinMaxValues(id);
        if (!ucnMinMaxValues.isEmpty()){
            innerList6.add( ucnMinMaxValues.get(0).get("Ucn"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) ucnMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList6.add(formattedData);
            if (ucnMinMaxValues.size() > 1) {
                innerList6.add(ucnMinMaxValues.get(1).get("Ucn"));
                LocalDateTime data6 = (LocalDateTime) ucnMinMaxValues.get(1).get("data");
                String formattedData2 = data6.format(formatter);
                innerList6.add(formattedData2);
            }
        }
        allList.add(innerList6);

        List<Object> innerList7 = new ArrayList<>();
        innerList7.add("A项电流(A)");

        List<Map<String , Object>> iaMinMaxValues = hdbDao.getIaMinMaxValues(id);
        if (!iaMinMaxValues.isEmpty()){
            innerList7.add( iaMinMaxValues.get(0).get("Ia"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) iaMinMaxValues.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList7.add(formattedData);
            if (iaMinMaxValues.size() > 1) {
                innerList7.add(iaMinMaxValues.get(1).get("Ia"));
                LocalDateTime data6 = (LocalDateTime) iaMinMaxValues.get(1).get("data");
                String formattedData2 = data6.format(formatter);
                innerList7.add(formattedData2);
            }
        }
        allList.add(innerList7);

        List<Object> innerList8 = new ArrayList<>();
        innerList8.add("B项电流(A)");
        List<Map<String, Object>> ib = hdbDao.getAllMinMaxValues(id,"Ib");
        if (!ib.isEmpty()){
            innerList8.add( ib.get(0).get("Ib"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) ib.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList8.add(formattedData);
            if (ib.size() > 1) {
                innerList8.add(ib.get(1).get("Ib"));
                LocalDateTime data6 = (LocalDateTime) ib.get(1).get("data");
                String formattedData2 = data6.format(formatter);
                innerList8.add(formattedData2);
            }
        }
        allList.add(innerList8);

        List<Object> innerList9 = new ArrayList<>();
        innerList9.add("C项电流(A)");
        List<Map<String, Object>> ic = hdbDao.getAllMinMaxValues(id,"Ic");

        if (!ic.isEmpty()){
            innerList9.add( ic.get(0).get("Ic"));
            //处理时间类型的数据
            LocalDateTime data = (LocalDateTime) ic.get(0).get("data");
            String formattedData = data.format(formatter);
            innerList9.add(formattedData);
            if (ic.size() > 1) {
                innerList9.add(ic.get(1).get("Ic"));
                LocalDateTime data6 = (LocalDateTime) ic.get(1).get("data");
                String formattedData2 = data6.format(formatter);
                innerList9.add(formattedData2);
            }
        }
        allList.add(innerList9);

        List<Object> innerList10 = new ArrayList<>();
        innerList10.add("零序电流(A)");
        innerList10.add("-");
        innerList10.add("-");
        innerList10.add("-");
        innerList10.add("-");
        allList.add(innerList10);
        return allList;
    }
    //根据时间查询三相电压
    public List<Map<Object,Object>> getdnzljcsj(String wz, String ks, String js, String lx) {
        //处理名字

        return hdbDao.getDnzlU(wz, ks, js);
    }
    //根据时间查询三相电压
    public List<Map<Object, Object>> getdnzldlsj( String wz, String ks, String js, String lx) {
        String cmsa ="Ia";
        String cmsb ="Ib";
        String cmsc ="Ic";
        return hdbDao.getdnzldlsj(cmsa,cmsb,cmsc,wz,ks,js);
    }

    public List<Map<Object, Object>> getdnzlygsj(String cms,String cmsa,String cmsb,String cmsc,String wz, String ks, String js, String lx) {

        return hdbDao.getdnzlygsj(cms,cmsa,cmsb,cmsc,wz,ks,js);
    }

    public List<Map<Object, Object>> getdnzzszgl(String s, String wz, String ks, String js, String lx) {
        return hdbDao.getdnzzszgl(s,wz,ks,js);
    }
    //向ytmem表中添加一条数据
    /**
     * 转换并保存一组常规电气测量值及采集时间。
     */
    public String setYtmemByAll(Map<String,String> map,int biaoshi){

//        System.out.println(map);
        if (!map.containsKey("F") ){
            //备用删点
            map.put("F","0");
            map.put("PFa","0");
            map.put("PFb","0");
            map.put("PFc","0");
            map.put("Uan","0");
            map.put("Ubn","0");
            map.put("Ucn","0");
            map.put("Uab","0");
            map.put("Ubc","0");
            map.put("Uca","0");
            map.put("W","0");
            map.put("Wa","0");
            map.put("Wb","0");
            map.put("Wc","0");
            map.put("Q","0");
            map.put("Qa","0");
            map.put("Qb","0");
            map.put("Qc","0");
            map.put("S","0");
            map.put("Sa","0");
            map.put("Sb","0");
            map.put("Sc","0");
        }

        if (!map.containsKey("DI1") ){
            map.put("DI1","0");
            map.put("CT1","0");
            map.put("Ia","0");
            map.put("Ib","0");
            map.put("Ic","0");
            map.put("PF","0");
            map.put("ADSQ","0");
        }
        String str = map.get("F");
        double number = Double.parseDouble(str);
        // 使用 DecimalFormat 格式化并保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        //获取ct1的值
        double ct = Double.parseDouble(df.format(Double.parseDouble(map.get("CT1")) / 5));
        String formatted = df.format(number/100);
        // 输出结果值并替换
        map.put("F", formatted);

        // 输出结果值并替换
        map.put("PF", df.format(Double.parseDouble(map.get("PF"))/1000));
        map.put("PFa", df.format(Double.parseDouble(map.get("PFa"))/1000));
        map.put("PFb", df.format(Double.parseDouble(map.get("PFb"))/1000));
        map.put("PFc", df.format(Double.parseDouble(map.get("PFc"))/1000));

        map.put("Uan", df.format(Double.parseDouble(map.get("Uan"))/10));
        map.put("Ubn", df.format(Double.parseDouble(map.get("Ubn"))/10));
        map.put("Ucn", df.format(Double.parseDouble(map.get("Ucn"))/10));
        map.put("Uab", df.format(Double.parseDouble(map.get("Uab"))/10));
        map.put("Ubc", df.format(Double.parseDouble(map.get("Ubc"))/10));
        map.put("Uca", df.format(Double.parseDouble(map.get("Uca"))/10));

        map.put("Ia", df.format(Double.parseDouble(map.get("Ia"))/1000 * ct));
        map.put("Ib", df.format(Double.parseDouble(map.get("Ib"))/1000 * ct));
        map.put("Ic", df.format(Double.parseDouble(map.get("Ic"))/1000 * ct));

        map.put("W", df.format(Double.parseDouble(map.get("W")) * ct/1000));
        map.put("Wa", df.format(Double.parseDouble(map.get("Wa")) * ct/1000));
        map.put("Wb", df.format(Double.parseDouble(map.get("Wb")) * ct/1000));
        map.put("Wc", df.format(Double.parseDouble(map.get("Wc")) * ct/1000));
        map.put("Q", df.format(Double.parseDouble(map.get("Q")) * ct/1000));
        map.put("Qa", df.format(Double.parseDouble(map.get("Qa")) * ct/1000));
        map.put("Qb", df.format(Double.parseDouble(map.get("Qb")) * ct/1000));
        map.put("Qc", df.format(Double.parseDouble(map.get("Qc")) * ct/1000));
        map.put("S", df.format(Double.parseDouble(map.get("S")) * ct /1000));
        map.put("Sa", df.format(Double.parseDouble(map.get("Sa")) * ct /1000));
        map.put("Sb", df.format(Double.parseDouble(map.get("Sb")) * ct /1000));
        map.put("Sc", df.format(Double.parseDouble(map.get("Sc")) * ct /1000));

        map.put("ADSP", df.format(Double.parseDouble(map.get("ADSP"))/10));
        map.put("ADSQ", df.format(Double.parseDouble(map.get("ADSQ"))/10));

        if (biaoshi ==1){
            //添加电费信息
            //查询名称对应的最新的电量，计算出本小时耗时电量
            Double ZuiXinDL = hdbDao.cxZuiXinDL(map.get("name"));
            if (ZuiXinDL ==0.0){
                //查询不到值，不做电费计算操作
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            Double v = Double.parseDouble(map.get("ADSP")) - ZuiXinDL;
            if (v <0.0){
                //计算出电量是负数，不做电费存入
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            String adsp = df.format(v);
            //if判断出尖峰平谷，计算电费信息，并且存入map集合
            System.out.println("保存的名称："+map.get("name")+"||查询电费"+map.get("ADSP")+"||最大电费"+ZuiXinDL+"||结余电费"+adsp);
            String data = map.get("data");//2025-06-11 15:12:43

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            LocalDateTime dateTime = LocalDateTime.parse(data, formatter);
            // 获取月份和小时
            int month = dateTime.getMonthValue();
            int hour = dateTime.getHour();
            String JinQian ="";
            int jfpg =0;
            if ((month >=2 && month<=6) || (month>=9 && month<=11)){//2-6月电费计算
                if ((hour>=0 && hour<=7) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                     JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 8 && hour<= 10)|| (hour==23) || (hour>=14 &&hour<=17 )) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.8);
                }else if((hour>=11 && hour<=13) ||(hour>= 18 && hour <=22)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }
            }
            if ((month==7) || (month==8)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour==10) ||(hour>= 13 && hour <=15)||(hour>= 17 && hour <=21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour>=11 && hour <=12) ||(hour==16)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            if ((month==1) || (month==12)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour>=13 && hour<=16) ||(hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour>=10 && hour<=12) ||(hour==17)||(hour==21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour==12) ||(hour>=18 && hour <=20)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            //计算完后将数据加入数据库
            hdbDao.setDianLing(map.get("name"),data,jfpg,adsp,JinQian);
        }

        //判断开关值状态更新
        //查询此名称最新开关，跟现在的做对比，如果不一样则添加一条报警数据
//        String newDI1 =hdbDao.getByDI1(map.get("name"));
//        if (newDI1.equals(map.get("DI1"))){
//            System.out.println("此闸开关正常");
//        }else {
//            hdbDao.setalarm(map.get("name"),map.get("data"),"合闸状态异常","1","1");
//        }
        hdbDao.setYtmemByAll(map);
        return "700";
    }

    //向ytmem表中添加一条数据
    /**
     * 转换并保存散表测量值。
     */
    public String setHushByAll(Map<String,String> map,int biaoshi){

//        System.out.println(map);
        if (!map.containsKey("F") ){
            //备用删点
            map.put("F","0");
            map.put("PFa","0");
            map.put("PFb","0");
            map.put("PFc","0");
            map.put("Uan","0");
            map.put("Ubn","0");
            map.put("Ucn","0");
            map.put("Uab","0");
            map.put("Ubc","0");
            map.put("Uca","0");
            map.put("W","0");
            map.put("Wa","0");
            map.put("Wb","0");
            map.put("Wc","0");
            map.put("Q","0");
            map.put("Qa","0");
            map.put("Qb","0");
            map.put("Qc","0");
            map.put("S","0");
            map.put("Sa","0");
            map.put("Sb","0");
            map.put("Sc","0");
        }

        if (!map.containsKey("DI1") ){
            map.put("DI1","0");
            map.put("CT1","0");
            map.put("Ia","0");
            map.put("Ib","0");
            map.put("Ic","0");
            map.put("PF","0");
            map.put("ADSQ","0");
        }
        String str = map.get("F");
        double number = Double.parseDouble(str);
        // 使用 DecimalFormat 格式化并保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        //获取ct1的值
        double ct = Double.parseDouble(df.format(Double.parseDouble(map.get("CT1")) / 5));
        String formatted = df.format(number/100);
        // 输出结果值并替换
        map.put("F", formatted);

        // 输出结果值并替换
        map.put("PF", df.format(Double.parseDouble(map.get("PF"))/1000));
        map.put("PFa", df.format(Double.parseDouble(map.get("PFa"))/1000));
        map.put("PFb", df.format(Double.parseDouble(map.get("PFb"))/1000));
        map.put("PFc", df.format(Double.parseDouble(map.get("PFc"))/1000));

        map.put("Uan", df.format(Double.parseDouble(map.get("Uan"))/10));
        map.put("Ubn", df.format(Double.parseDouble(map.get("Ubn"))/10));
        map.put("Ucn", df.format(Double.parseDouble(map.get("Ucn"))/10));
        map.put("Uab", df.format(Double.parseDouble(map.get("Uab"))/10));
        map.put("Ubc", df.format(Double.parseDouble(map.get("Ubc"))/10));
        map.put("Uca", df.format(Double.parseDouble(map.get("Uca"))/10));

        map.put("Ia", df.format(Double.parseDouble(map.get("Ia"))/1000 * ct));
        map.put("Ib", df.format(Double.parseDouble(map.get("Ib"))/1000 * ct));
        map.put("Ic", df.format(Double.parseDouble(map.get("Ic"))/1000 * ct));

        map.put("W", df.format(Double.parseDouble(map.get("W")) * ct/1000));
        map.put("Wa", df.format(Double.parseDouble(map.get("Wa")) * ct/1000));
        map.put("Wb", df.format(Double.parseDouble(map.get("Wb")) * ct/1000));
        map.put("Wc", df.format(Double.parseDouble(map.get("Wc")) * ct/1000));
        map.put("Q", df.format(Double.parseDouble(map.get("Q")) * ct/1000));
        map.put("Qa", df.format(Double.parseDouble(map.get("Qa")) * ct/1000));
        map.put("Qb", df.format(Double.parseDouble(map.get("Qb")) * ct/1000));
        map.put("Qc", df.format(Double.parseDouble(map.get("Qc")) * ct/1000));
        map.put("S", df.format(Double.parseDouble(map.get("S")) * ct /1000));
        map.put("Sa", df.format(Double.parseDouble(map.get("Sa")) * ct /1000));
        map.put("Sb", df.format(Double.parseDouble(map.get("Sb")) * ct /1000));
        map.put("Sc", df.format(Double.parseDouble(map.get("Sc")) * ct /1000));

        map.put("ADSP", df.format(Double.parseDouble(map.get("ADSP"))));
        map.put("ADSQ", df.format(Double.parseDouble(map.get("ADSQ"))));

        if (biaoshi ==1){
            //添加电费信息
            //查询名称对应的最新的电量，计算出本小时耗时电量
            Double ZuiXinDL = hdbDao.cxZuiXinDL(map.get("name"));
            if (ZuiXinDL ==0.0){
                //查询不到值，不做电费计算操作
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            Double v = Double.parseDouble(map.get("ADSP")) - ZuiXinDL;
            if (v <0.0){
                //计算出电量是负数，不做电费存入
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            String adsp = df.format(v);
            //if判断出尖峰平谷，计算电费信息，并且存入map集合
            System.out.println("保存的名称："+map.get("name")+"||查询电费"+map.get("ADSP")+"||最大电费"+ZuiXinDL+"||结余电费"+adsp);
            String data = map.get("data");//2025-06-11 15:12:43

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            LocalDateTime dateTime = LocalDateTime.parse(data, formatter);
            // 获取月份和小时
            int month = dateTime.getMonthValue();
            int hour = dateTime.getHour();
            String JinQian ="";
            int jfpg =0;
            if ((month >=2 && month<=6) || (month>=9 && month<=11)){//2-6月电费计算
                if ((hour>=0 && hour<=7) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 8 && hour<= 10)|| (hour==23) || (hour>=14 &&hour<=17 )) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.8);
                }else if((hour>=11 && hour<=13) ||(hour>= 18 && hour <=22)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }
            }
            if ((month==7) || (month==8)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour==10) ||(hour>= 13 && hour <=15)||(hour>= 17 && hour <=21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour>=11 && hour <=12) ||(hour==16)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            if ((month==1) || (month==12)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour>=13 && hour<=16) ||(hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour>=10 && hour<=12) ||(hour==17)||(hour==21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour==12) ||(hour>=18 && hour <=20)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            //计算完后将数据加入数据库
            hdbDao.setDianLing(map.get("name"),data,jfpg,adsp,JinQian);
        }

        //判断开关值状态更新
        //查询此名称最新开关，跟现在的做对比，如果不一样则添加一条报警数据
//        String newDI1 =hdbDao.getByDI1(map.get("name"));
//        if (newDI1.equals(map.get("DI1"))){
//            System.out.println("此闸开关正常");
//        }else {
//            hdbDao.setalarm(map.get("name"),map.get("data"),"合闸状态异常","1","1");
//        }
        hdbDao.setYtmemByAll(map);
        return "700";
    }
    //向ytew表中添加一条数据
    /**
     * 转换并保存 325 网关采集的数据。
     */
    public String set325ByAll(Map<String,String> map,int biaoshi){

//        System.out.println(map);
        if (!map.containsKey("F") ){
            //备用删点
            map.put("F","0");
            map.put("PFa","0");
            map.put("PFb","0");
            map.put("PFc","0");
            map.put("Uan","0");
            map.put("Ubn","0");
            map.put("Ucn","0");
            map.put("Uab","0");
            map.put("Ubc","0");
            map.put("Uca","0");
            map.put("W","0");
            map.put("Wa","0");
            map.put("Wb","0");
            map.put("Wc","0");
            map.put("Q","0");
            map.put("Qa","0");
            map.put("Qb","0");
            map.put("Qc","0");
            map.put("S","0");
            map.put("Sa","0");
            map.put("Sb","0");
            map.put("Sc","0");
        }

        if (!map.containsKey("DI1") ){
            map.put("DI1","0");
            map.put("CT1","0");
            map.put("Ia","0");
            map.put("Ib","0");
            map.put("Ic","0");
            map.put("PF","0");
            map.put("ADSQ","0");
        }
        String str = map.get("F");
        double number = Double.parseDouble(str);
        // 使用 DecimalFormat 格式化并保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        //获取ct1的值
        double ct = Double.parseDouble(df.format(Double.parseDouble(map.get("CT1")) / 5));
        String formatted = df.format(number/100);
        // 输出结果值并替换
        map.put("F", formatted);

        // 输出结果值并替换
        map.put("PF", df.format(Double.parseDouble(map.get("PF"))/1000));
        map.put("PFa", df.format(Double.parseDouble(map.get("PFa"))/1000));
        map.put("PFb", df.format(Double.parseDouble(map.get("PFb"))/1000));
        map.put("PFc", df.format(Double.parseDouble(map.get("PFc"))/1000));

        map.put("Uan", df.format(Double.parseDouble(map.get("Uan"))/10));
        map.put("Ubn", df.format(Double.parseDouble(map.get("Ubn"))/10));
        map.put("Ucn", df.format(Double.parseDouble(map.get("Ucn"))/10));
        map.put("Uab", df.format(Double.parseDouble(map.get("Uab"))/10));
        map.put("Ubc", df.format(Double.parseDouble(map.get("Ubc"))/10));
        map.put("Uca", df.format(Double.parseDouble(map.get("Uca"))/10));

        map.put("Ia", df.format(Double.parseDouble(map.get("Ia"))/1000 * ct));
        map.put("Ib", df.format(Double.parseDouble(map.get("Ib"))/1000 * ct));
        map.put("Ic", df.format(Double.parseDouble(map.get("Ic"))/1000 * ct));

        map.put("W", df.format(Double.parseDouble(map.get("W")) * ct/1000));
        map.put("Wa", df.format(Double.parseDouble(map.get("Wa")) * ct/1000));
        map.put("Wb", df.format(Double.parseDouble(map.get("Wb")) * ct/1000));
        map.put("Wc", df.format(Double.parseDouble(map.get("Wc")) * ct/1000));
        map.put("Q", df.format(Double.parseDouble(map.get("Q")) * ct/1000));
        map.put("Qa", df.format(Double.parseDouble(map.get("Qa")) * ct/1000));
        map.put("Qb", df.format(Double.parseDouble(map.get("Qb")) * ct/1000));
        map.put("Qc", df.format(Double.parseDouble(map.get("Qc")) * ct/1000));
        map.put("S", df.format(Double.parseDouble(map.get("S")) * ct /1000));
        map.put("Sa", df.format(Double.parseDouble(map.get("Sa")) * ct /1000));
        map.put("Sb", df.format(Double.parseDouble(map.get("Sb")) * ct /1000));
        map.put("Sc", df.format(Double.parseDouble(map.get("Sc")) * ct /1000));

        map.put("ADSP", df.format(Double.parseDouble(map.get("ADSP"))));
        map.put("ADSQ", df.format(Double.parseDouble(map.get("ADSQ"))));

        if (biaoshi ==1){
            //添加电费信息
            //查询名称对应的最新的电量，计算出本小时耗时电量
            Double ZuiXinDL = hdbDao.cxZuiXinDL(map.get("name"));
            if (ZuiXinDL ==0.0){
                //查询不到值，不做电费计算操作
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            Double v = Double.parseDouble(map.get("ADSP")) - ZuiXinDL;
            if (v <0.0){
                //计算出电量是负数，不做电费存入
                hdbDao.setYtmemByAll(map);
                return "600";
            }
            String adsp = df.format(v);
            //if判断出尖峰平谷，计算电费信息，并且存入map集合
            System.out.println("保存的名称："+map.get("name")+"||查询电费"+map.get("ADSP")+"||最大电费"+ZuiXinDL+"||结余电费"+adsp);
            String data = map.get("data");//2025-06-11 15:12:43

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            LocalDateTime dateTime = LocalDateTime.parse(data, formatter);
            // 获取月份和小时
            int month = dateTime.getMonthValue();
            int hour = dateTime.getHour();
            String JinQian ="";
            int jfpg =0;
            if ((month >=2 && month<=6) || (month>=9 && month<=11)){//2-6月电费计算
                if ((hour>=0 && hour<=7) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 8 && hour<= 10)|| (hour==23) || (hour>=14 &&hour<=17 )) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.8);
                }else if((hour>=11 && hour<=13) ||(hour>= 18 && hour <=22)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }
            }
            if ((month==7) || (month==8)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour==10) ||(hour>= 13 && hour <=15)||(hour>= 17 && hour <=21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour>=11 && hour <=12) ||(hour==16)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            if ((month==1) || (month==12)){//7-8月电费计算
                if ((hour>=0 && hour<=6) || (hour==23) ){
                    //电费价格  0.6472  谷 4
                    jfpg =4;
                    JinQian = df.format(Double.parseDouble(adsp)*0.6);
                } else if ((hour >= 7 && hour<= 9)|| (hour>=13 && hour<=16) ||(hour==22)) {
                    //电费价格 0.8864 平 3
                    jfpg =3;
                    JinQian = df.format(Double.parseDouble(adsp)*0.88);
                }else if((hour>=10 && hour<=12) ||(hour==17)||(hour==21)){
                    //电费价格 1.1255 峰 2
                    jfpg =2;
                    JinQian = df.format(Double.parseDouble(adsp)*1.1);
                }else if((hour==12) ||(hour>=18 && hour <=20)){
                    //电费价格 1.25 尖 1
                    jfpg =1;
                    JinQian = df.format(Double.parseDouble(adsp)*1.25);
                }
            }
            //计算完后将数据加入数据库
            hdbDao.setDianLing(map.get("name"),data,jfpg,adsp,JinQian);
        }

        //判断开关值状态更新
        //查询此名称最新开关，跟现在的做对比，如果不一样则添加一条报警数据
//        String newDI1 =hdbDao.getByDI1(map.get("name"));
//        if (newDI1.equals(map.get("DI1"))){
//            System.out.println("此闸开关正常");
//        }else {
//            hdbDao.setalarm(map.get("name"),map.get("data"),"合闸状态异常","1","1");
//        }
        hdbDao.setYtmemByAll(map);
        return "700";
    }

    //向滤波表中添加一条数据
    /**
     * 保存滤波器测量数据。
     */
    public void setLBQByALL(Map<String,String> map){
        DecimalFormat df = new DecimalFormat("#.00");
        map.put("LBQ_status", df.format(Double.parseDouble(map.get("LBQ_status"))  ));//状态
        map.put("LBQ_alarm", df.format(Double.parseDouble(map.get("LBQ_alarm"))));//告警信息
        map.put("bc_Ia", df.format(Double.parseDouble(map.get("bc_Ia"))/10  ));//补偿电流
        map.put("bc_Ib", df.format(Double.parseDouble(map.get("bc_Ib"))/10  ));//补偿电流
        map.put("bc_Ic", df.format(Double.parseDouble(map.get("bc_Ic"))/10  ));//补偿电流
        map.put("temperature", df.format(Double.parseDouble(map.get("temperature")) ));//温度
        map.put("Ia", df.format(Double.parseDouble(map.get("Ia")) ));//负载电流
        map.put("Ib", df.format(Double.parseDouble(map.get("Ib")) ));//负载电流
        map.put("Ic", df.format(Double.parseDouble(map.get("Ic")) ));//负载电流
        map.put("Uan", df.format(Double.parseDouble(map.get("Uan"))/10 ));//线电压
        map.put("Ubn", df.format(Double.parseDouble(map.get("Ubn"))/10 ));//线电压
        map.put("Ucn", df.format(Double.parseDouble(map.get("Ucn"))/10 ));//线电压
        map.put("DLJBL", df.format(Double.parseDouble(map.get("DLJBL"))/10 ));//畸变率
        map.put("PF", df.format(Double.parseDouble(map.get("PF"))/1000 ));//功率
        map.put("FZL", df.format(Double.parseDouble(map.get("FZL")) ));//功率
        hdbDao.setLBQByALL(map);

    }
    /**
     * 保存高压侧测量数据。
     */
    public void setgyYtmemByAll(Map<String, String> gytm, int biaoshi) {

        DecimalFormat df = new DecimalFormat("#.00");
        gytm.put("F", df.format(1000000/  Double.parseDouble(gytm.get("F"))));
        gytm.put("Q", df.format(  Double.parseDouble(gytm.get("Q"))));
        gytm.put("S", df.format( Double.parseDouble(gytm.get("S"))));
        gytm.put("W", df.format( Double.parseDouble(gytm.get("W"))));
        gytm.put("Ia", df.format(  Double.parseDouble(gytm.get("Ia"))/100   ));
        gytm.put("Ib", df.format(  Double.parseDouble(gytm.get("Ib"))/100  ));
        gytm.put("Ic", df.format(  Double.parseDouble(gytm.get("Ic"))/100  ));
        gytm.put("PF", df.format(  Double.parseDouble(gytm.get("PF"))  ));
        gytm.put("Uan", df.format(  Double.parseDouble(gytm.get("Uan"))/17  ));
        gytm.put("Ubn", df.format(  Double.parseDouble(gytm.get("Ubn"))/17  ));
        gytm.put("Ucn", df.format(  Double.parseDouble(gytm.get("Ucn"))/17  ));
        gytm.put("ZXYG", df.format(  Double.parseDouble(gytm.get("ZXYG"))  ));
        gytm.put("ZXWG", df.format(  Double.parseDouble(gytm.get("ZXWG"))  ));
        gytm.put("FXWG", df.format(  Double.parseDouble(gytm.get("FXWG"))  ));
        gytm.put("FXYG", df.format(  Double.parseDouble(gytm.get("FXYG"))  ));
//        double Ia = Double.parseDouble(gytm.get("Ia"))/100 ;
//        double Ib = Double.parseDouble(gytm.get("Ib"))/100 ;
//        double Ic = Double.parseDouble(gytm.get("Ic"))/100 ;
//        double Uan = Double.parseDouble(gytm.get("Uan"))/17 ;
//        double Ubn = Double.parseDouble(gytm.get("Ubn"))/17 ;
//        double Ucn = Double.parseDouble(gytm.get("Ucn"))/17 ;

//        double pow = Math.pow(Uan, Ia);

        hdbDao.setYtmemByAll(gytm);

    }

    /**
     * 从报警点位键中提取设备显示名称。
     */
    private String extractAlarmDeviceName(String pointKey) {
        if (pointKey == null) {
            return "";
        }
        int dotIndex = pointKey.indexOf(".");
        return dotIndex > 0 ? pointKey.substring(0, dotIndex) : pointKey;
    }

    /**
     * 构建并持久化新的报警记录。
     */
    private PowerAlarm createAlarm(String alarmName, String alarmTitle, String alarmContent, String alarmLevel) {
        LocalDateTime currentTime = LocalDateTime.now();
        PowerAlarm alarm = new PowerAlarm();
        alarm.setAlarmName(alarmName);
        alarm.setAlarmTitle(alarmTitle);
        alarm.setAlarmContent(alarmContent);
        alarm.setAlarmLevel(alarmLevel);
        alarm.setCreateTime(currentTime.atZone(ZoneId.systemDefault()).toInstant());
        alarm.setUpdateTime(currentTime.atZone(ZoneId.systemDefault()).toInstant());
        alarm.setAlarmStatus(ALARM_STATUS_UNCONFIRMED);
        hdbDao.insertAlarm(alarm);
        return alarm;
    }

    /**
     * 将报警实体转换为 WebSocket 推送事件数据。
     */
    private Map<String, Object> buildAlarmEvent(PowerAlarm alarm, String eventType, String message) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("alarmId", alarm.getId());
        event.put("message", message);
        event.put("name", alarm.getAlarmName());
        event.put("alarmTitle", alarm.getAlarmTitle());
        event.put("alarmContent", alarm.getAlarmContent());
        event.put("alarmLevel", alarm.getAlarmLevel());
        event.put("alarmStatus", alarm.getAlarmStatus());
        return event;
    }

    /**
     * 将 DI 值标准化为 0/1 状态文本。
     */
    private String normalizeDiValue(String value) {
        if (value == null) {
            return "";
        }
        value = value.trim();
        if ("1.0".equals(value)) {
            return "1";
        }
        if ("0.0".equals(value)) {
            return "0";
        }
        return value;
    }

    /**
     * 根据首次确认的正常 DI 状态修正未恢复报警状态。
     */
    public void reconcileInitialDiNormal(String pointKey) {
        String alarmName = extractAlarmDeviceName(pointKey);
        if (alarmName.isEmpty() || "2AA11-1".equals(alarmName)) return;
        int updated = hdbDao.reconcileOpenAlarms(alarmName, TRIP_ALARM_TITLE, LocalDateTime.now());
        if (updated > 0) {
            System.out.println("启动状态核对：已按当前合闸状态恢复报警，设备=" + alarmName + ", 数量=" + updated);
        }
    }

    /**
     * 依据 DI 状态迁移创建跳闸报警或将现有报警标记恢复。
     */
    public Map<String, Object> handleDiAlarmChange(String pointKey, String oldValue, String newValue) {
        String alarmName = extractAlarmDeviceName(pointKey);
        if (alarmName.isEmpty() || "2AA11-1".equals(alarmName)) {
            return null;
        }

        oldValue = normalizeDiValue(oldValue);
        newValue = normalizeDiValue(newValue);

        System.out.println("handleDiAlarmChange -> pointKey=" + pointKey + ", oldValue=" + oldValue + ", newValue=" + newValue);

        if ("1".equals(oldValue) && "0".equals(newValue)) {
            PowerAlarm existingAlarm = hdbDao.findLatestOpenAlarm(alarmName, TRIP_ALARM_TITLE);
            if (existingAlarm != null) {
                System.out.println("已存在未恢复跳闸报警，抑制重复弹窗: " + existingAlarm.getId());
                return null;
            }

            PowerAlarm alarm = createAlarm(alarmName, TRIP_ALARM_TITLE, alarmName + "-" + TRIP_ALARM_CONTENT, "1");
            System.out.println("新建跳闸报警成功: " + alarm.getId());
            return buildAlarmEvent(alarm, "trip", "跳闸报警");
        }

        if ("0".equals(oldValue) && "1".equals(newValue)) {
            PowerAlarm openAlarm = hdbDao.findLatestOpenAlarm(alarmName, TRIP_ALARM_TITLE);
            if (openAlarm == null) {
                System.out.println("检测到合闸恢复，但未找到对应的未恢复跳闸报警: " + alarmName);
                return null;
            }

            LocalDateTime now = LocalDateTime.now();
            String recoveryContent = openAlarm.getAlarmContent() != null
                    && openAlarm.getAlarmContent().startsWith(alarmName + "-")
                    ? alarmName + "-" + RECOVER_ALARM_CONTENT : RECOVER_ALARM_CONTENT;
            hdbDao.updateAlarmState(openAlarm.getId(), ALARM_STATUS_RECOVERED, now, recoveryContent);

            openAlarm.setAlarmStatus(ALARM_STATUS_RECOVERED);
            openAlarm.setAlarmContent(recoveryContent);
            openAlarm.setUpdateTime(now.atZone(ZoneId.systemDefault()).toInstant());

            System.out.println("跳闸报警已更新为恢复: " + openAlarm.getId());
            // Recovery updates the existing history row; only a confirmed trip opens a popup.
            return null;
        }

        System.out.println("DI变化未匹配报警规则: oldValue=" + oldValue + ", newValue=" + newValue);
        return null;
    }



    //向电量表中添加一条数据
    /**
     * 保存电量统计记录。
     */
    public String setDianLiangAll(Map<String,String> map){
        //传递name，电量，时间
        //查询电量最大值，使用此值减去最大值得出此小时内耗电量
        //使用时间区分出尖峰平谷，计算小时内耗电量应该对应的电费值

//        hdbDao.setDianLiangAll(map);
        return "700";
    }

    public List<Map<String,Object>> getById41() {
        // 1AA1-1  1AA2-1 1AA3-1 1AA4-1 1AA5-1 1AA6-1到1AA6-4 1AA7-1到4 1AA8-1到5 1AA9-1到4 1AA10-1到7
        //时间最新的DI1的值和Ia，Ib，Ic
        //返回
        return hdbDao.getById41();
    }

    public List<Map<String, Object>> getBYId42() {

        return hdbDao.getById42();
    }

    /**
     * 按日查询并组装多个测点的电量统计结果。
     */
    public List<String> getdltjcxdayList2(List<String> cmsname, String datatest,int i) {//name,data
        //通过后端拿到数据和时间

        String cmsliststr =convertListToInString(cmsname);
        List<String> list =new ArrayList<>();
        if (i == 2){
            list= hdbDao.getdltjcxList2(cmsliststr,datatest);
        }else if (i == 3){
            list= hdbDao.getdltjcxList3(cmsliststr,datatest);
        }else if (i == 4){
                Map<String, Double> stringObjectMap = hdbDao.getdltjcxList4(cmsliststr, datatest);
                list.add(stringObjectMap.get("jfpg_1")+"");
                list.add(stringObjectMap.get("jfpg_2")+"");
                list.add(stringObjectMap.get("jfpg_3")+"");
                list.add(stringObjectMap.get("jfpg_4")+"");
        }else if (i == 5){
                Map<String, Double> stringObjectMap = hdbDao.getdltjcxList5(cmsliststr, datatest);
                list.add(stringObjectMap.get("jfpg_1")+"");
                list.add(stringObjectMap.get("jfpg_2")+"");
                list.add(stringObjectMap.get("jfpg_3")+"");
                list.add(stringObjectMap.get("jfpg_4")+"");
        }
        return list;
    }
    // 工具方法：List<String> → 'a','b','c' 格式字符串
    private String convertListToInString(List<String> list) {
        if (list == null || list.isEmpty()) {
            // 空列表处理：返回无意义值（避免 SQL 语法错误，业务层可提前校验非空）
            return "''";
        }
        // 遍历列表，每个元素加单引号，再用逗号拼接
        return list.stream()
                .map(name -> "'" + name + "'")  // 给每个 name 包裹单引号（避免 SQL 语法错误）
                .collect(Collectors.joining(","));  // 用逗号分隔元素
    }

    /**
     * 按月查询并组装多个测点的电量统计结果。
     */
    public List<String> getdltjcxmonthList2(List<String> cmsname, String datatest,int i) {
        //通过后端拿到数据和时间
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M");
        // 处理第一个日期
        YearMonth yearMonth1 = YearMonth.parse(datatest, formatter);
        YearMonth nextMonth1 = yearMonth1.plusMonths(1);
//        System.out.println("当前日期"+ datatest +"|| 明天日期"+nextMonth1.toString());
        List<String> list =new ArrayList<>();
        String cmsliststr =convertListToInString(cmsname);
        if (i ==2){
            datatest =datatest +"-01";
            String datats =nextMonth1 + "-01";

            System.out.println("cmsname抵达:"+cmsliststr+"|"+datatest+"|"+datats);
        list= hdbDao.getdltjcxmonthsList2(cmsliststr,datatest,datats);
        }else if (i == 3){

            System.out.println("cmsname抵达2:"+cmsliststr);
            list= hdbDao.getdltjcxmonthsList3(cmsliststr,datatest+"-01",nextMonth1+"-01");
        }else if (i == 4){

//            System.out.println("cmsname抵达3:"+cmsname.toString());
                Map<String, Double> stringObjectMap= hdbDao.getdltjcxmonthsList4(cmsliststr,datatest+"-01",nextMonth1+"-01");
                list.add(stringObjectMap.get("jfpg_1")+"");
                list.add(stringObjectMap.get("jfpg_2")+"");
                list.add(stringObjectMap.get("jfpg_3")+"");
                list.add(stringObjectMap.get("jfpg_4")+"");
        }else if (i == 5){

//            System.out.println("cmsname抵达4:"+cmsname.toString());
                Map<String, Double> stringObjectMap= hdbDao.getdltjcxmonthsList5(cmsliststr,datatest+"-01",nextMonth1+"-01");
                list.add(stringObjectMap.get("jfpg_1")+"");
                list.add(stringObjectMap.get("jfpg_2")+"");
                list.add(stringObjectMap.get("jfpg_3")+"");
                list.add(stringObjectMap.get("jfpg_4")+"");
        }

        System.out.println(list);
        //日期加一，日期后加天数，传递两个时间值过去  6.10任务
        return list;
    }

    /**
     * 按年查询并组装多个测点的电量统计结果。
     */
    public List<String> getdltjcxyearList2(List<String> cmsname, String datatest,int i) {
        List<String>  list =new ArrayList<>();
        String result = String.valueOf(Integer.parseInt(datatest) + 1);

        String cmsliststr =convertListToInString(cmsname);
        if (i == 2){
            list= hdbDao.getdltjcxyearList2(cmsliststr,datatest+"-01-01",result+"-01-01");
        }else if (i==3){
            list= hdbDao.getdltjcxyearList3(cmsliststr,datatest+"-01-01",result+"-01-01");
        }else if (i==4){
            Map<String, Double> stringObjectMap=hdbDao.getdltjcxmonthsList4(cmsliststr,datatest+"-01-01",result+"-01-01");
            list.add(stringObjectMap.get("jfpg_1")+"");
            list.add(stringObjectMap.get("jfpg_2")+"");
            list.add(stringObjectMap.get("jfpg_3")+"");
            list.add(stringObjectMap.get("jfpg_4")+"");
        }else if (i==5){
            Map<String, Double> stringObjectMap=hdbDao.getdltjcxmonthsList5(cmsliststr,datatest+"-01-01",result+"-01-01");
            list.add(stringObjectMap.get("jfpg_1")+"");
            list.add(stringObjectMap.get("jfpg_2")+"");
            list.add(stringObjectMap.get("jfpg_3")+"");
            list.add(stringObjectMap.get("jfpg_4")+"");
        }
        return list;
    }

    private String resolvePdsName(String meterName) {
        if (meterName == null || meterName.trim().isEmpty()) {
            return "";
        }

        if (meterName.startsWith("2-")) {
            return "2#配电室";
        }

        if (meterName.startsWith("6-")) {
            return "6#配电室";
        }

        return "1#配电室";
    }

    /**
     * 按当前能耗树配置生成能耗统计报表数据。
     */
    public List<Map<String,String>> getnhtj(List<String> cmsname, String startDate, String endDate, String pdsMapJson) {
        //接收数据： name集合，开始时间，结束时间
        List<Map<String,String>> maps = new LinkedList<>();
        Map<String, String> pdsMap = new HashMap<>();
        if (pdsMapJson != null && !pdsMapJson.trim().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                pdsMap = objectMapper.readValue(
                        pdsMapJson,
                        new TypeReference<Map<String, String>>() {}
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DecimalFormat df = new DecimalFormat("#.##");
        // 1. 一次查询所有name的数据（替代循环查询）
        List<Map<String, Object>> batchData = hdbDao.batchGetNhtj(cmsname, startDate, endDate);

        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        for (Map<String, Object> row : batchData) {
            Object nameObj = row.get("name");
            if (nameObj != null) {
                dataMap.put(String.valueOf(nameObj), row);
            }
        }
        for (String name : cmsname) {
            Map<String, Object> data = dataMap.get(name);

            Map<String, String> item = new LinkedHashMap<>();

            Number startNumber = data == null ? null : (Number) data.get("startValue");
            Number endNumber = data == null ? null : (Number) data.get("endValue");
            boolean hasExactStart = startNumber != null;
            boolean hasExactEnd = endNumber != null;

            item.put("name", name);
            item.put("起始日期", startDate);
            item.put("结束日期", endDate);

            String pdsName = pdsMap.get(name);
            if (pdsName == null || pdsName.trim().isEmpty()) {
                pdsName = resolvePdsName(name); // 再走你现在那个前缀兜底
            }

            item.put("配电室", pdsName);
            item.put("起始表读", hasExactStart ? df.format(startNumber.doubleValue()) : "N/A");
            item.put("结束表读", hasExactEnd ? df.format(endNumber.doubleValue()) : "N/A");

            if (!hasExactStart || !hasExactEnd) {
                item.put("采电量(kWh)", "N/A");
            } else {
                double diff = endNumber.doubleValue() - startNumber.doubleValue();
                if (diff < 0) {
                    diff = 0.0;
                }
                item.put("采电量(kWh)", df.format(diff));
            }

            maps.add(item);
        }

        return maps;
    }

    /**
     * 生成实时用电统计结果。
     */
    public Map<String,Object> getrtj(String clwz, String clsj) {
        //处理前端传递名称数据

        String realclwz = clwz;//处理名称
//        String realclwz = clwz.substring(0, clwz.indexOf(":"));//处理名称
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(clsj, formatter);
        // 计算前一天的日期
        LocalDate previousDay = date.minusDays(1);

        // 计算去年今日的日期
        LocalDate lastYearToday = date.minusYears(1);

        //根据名称和时间查询数据库本日数据
        String brStart = clsj + " 00:00:00";
        String brEnd = date.plusDays(1) + " 00:00:00";

        String zrStart = previousDay + " 00:00:00";
        String zrEnd = date + " 00:00:00";

        String qnStart = lastYearToday + " 00:00:00";
        String qnEnd = lastYearToday.plusDays(1) + " 00:00:00";

        List<Double> brsj = hdbDao.getdltjcxList22(realclwz, brStart, brEnd);
        List<Double> qn = hdbDao.getdltjcxList22(realclwz, qnStart, qnEnd);
        List<Double> zr = hdbDao.getdltjcxList22(realclwz, zrStart, zrEnd);

        //时间切换，做去年本日和昨日数据
        double brzl = calculateSumWithTwoDecimals(brsj);
        double qnzl = calculateSumWithTwoDecimals(qn);
        double zrzl = calculateSumWithTwoDecimals(zr);
        //有功:  当日电量 昨日电量 环比差  环比 去年同期电量 同比差 同比
        Map<Object, Object> ygmap = new LinkedHashMap<>();
        ygmap.put("brzl",brzl);
        ygmap.put("qnzl",qnzl);
        ygmap.put("zrzl",zrzl);
        Double hbc =brzl-zrzl;
        ygmap.put("hbc",String.format("%.2f", hbc));
        Double hb =hbc/zrzl;
        ygmap.put("hb",String.format("%.2f%%", hb));
        Double tbc =brzl -qnzl;
        ygmap.put("tbc",String.format("%.2f", tbc));
        if (qnzl ==0){
            ygmap.put("tb","/");
        }else {
            Double tb =tbc/qnzl;
            ygmap.put("tb",String.format("%.2f%%", tb));
        }

        HashMap<Object, Object> wgmap = new HashMap<>();
        wgmap.put("brzl",0);
        wgmap.put("qnzl",0);
        wgmap.put("zrzl",0);
        wgmap.put("hbc",0);
        wgmap.put("hb",0);
        wgmap.put("ybc",0);
        wgmap.put("tb",0);

        //拿到本日和昨日、前日的电量值
//            Double brdl = hdbDao.getADSPByname(realclwz,clsj);
//            Double zrdl = hdbDao.getADSPByname(realclwz,lastYearToday+"");
//            Double hb = brdl -zrdl;
//            Double hbc = (hb/zrdl)*100;
//        // 创建保留两位小数的格式化对象
//        DecimalFormat df = new DecimalFormat("#.00");
//
//        // 格式化后返回字符串（会自动四舍五入）
//        String yghb = df.format(hb); //
//        String yghbc = df.format(hbc); //
        //无功:  当日电量 昨日电量 环比差  环比 去年同期电量 同比差 同比


        Map<String, Object> map = new HashMap<>();
        map.put("qn", qn); //去年数据集合
        map.put("zr", zr); //昨日数据集合
        map.put("br", brsj); //本日数据集合
        map.put("yg", ygmap);
        map.put("wg", wgmap);
//        map.put("brdl", brdl); //本日电量
//        map.put("zrdl", zrdl); //昨日电量
//        map.put("yghb", yghb); //环比差
//        map.put("yghbc", yghbc); //环比%


        return map;
    }

    public static double calculateSumWithTwoDecimals(List<Double> list) {
        if (list == null || list.isEmpty()) {
            return 0.00; // 空集合返回0.00
        }

        // 累加总和（使用double临时存储）
        double total = 0.0;
        for (double num : list) {
            total += num;
        }

        // 使用BigDecimal处理四舍五入，确保保留两位小数
        BigDecimal bigDecimal = new BigDecimal(total);
        // RoundingMode.HALF_UP：四舍五入模式
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    public List<Map<String, Object>> getBYId51() {
        return hdbDao.getById51();
    }

    public List<Map<String, Object>> getBYId52() {
        return hdbDao.getBYId52();
    }

    public List<Map<String, Object>> getBYId61() {
        return hdbDao.getBYId61();
    }

    public List<Map<String, Object>> getBYId62() {
        return hdbDao.getBYId62();
    }

    public List<Map<String, Object>> getBYId71() {
        return hdbDao.getBYId71();
    }

    public List<Map<String, Object>> getBYId72() {
        return hdbDao.getBYId72();
    }

    /**
     * 按报警状态查询报警记录。
     */
    public Map<String,Object> getBystatus(String status) {
        Map<String,Object> bystatus = hdbDao.getBystatus(status);
        return bystatus;
    }

    /**
     * 更新指定报警记录状态。
     */
    public void updatealarmById(String alarmId) {
        hdbDao.updatealarmById(alarmId);
    }

    /**
     * 按编号查询单条报警记录。
     */
    public Map<String, Object> getAlarmById(String alarmId) {
        return hdbDao.getAlarmById(alarmId);
    }

    /**
     * 查询最新未确认报警。
     */
    public Map<String, Object> getLatestUnconfirmedAlarm() {
        return hdbDao.getLatestUnconfirmedAlarm();
    }

    /**
     * 按等级、状态、设备和时间条件查询报警列表。
     */
    public List<Map<String, String>> getALLalarm(String alarmLevel1, String alarmLevel2, String status, String device, String startDate, String endDate) {

        return hdbDao.getALLalarm(alarmLevel1,alarmLevel2,status,device,startDate,endDate);
    }


    // 发送消息给指定客户端
    /**
     * 向指定 WebSocket 会话发送消息。
     */
    public void sendMessage(String sessionId, String message) {
        MyWebSocketHandler.sendMessageToClient(sessionId, message);
    }

    // 广播消息给所有客户端
    /**
     * 广播报警或状态事件。
     */
    public void broadcastMessage(Map<String, Object> messageMap) {
        MyWebSocketHandler.broadcastMessage(messageMap);
    }

    public Map<String, Object> getfhdbfx(String kssj, String sjsj, String dbrq, String clwz) {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        //获取时间段内指定名字的负荷
        List<Map<String,String>> fhsj = hdbDao.getfhdbfx(kssj,sjsj,clwz);
        System.out.println(fhsj);
        //获取指定名字当日最大负荷
        String dbsj =hdbDao.getfhdbfx2(clwz,dbrq);
        stringObjectHashMap.put("data1",fhsj);
        stringObjectHashMap.put("data2",dbsj);
        return stringObjectHashMap;
    }

    public List<Map<String,String>> demo() {
     return hdbDao.demodata();
    }

    public Map<String,Object> getfhsbfx(String dysj, String desj, String clwz) {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        //查询第一和第二时间的数据
        List<Map<String,String>> condysj = hdbDao.getfhsbfx(dysj,clwz);
        stringObjectHashMap.put("dysj",condysj);
        List<Map<String,String>> condesj = hdbDao.getfhsbfx(desj,clwz);
        stringObjectHashMap.put("desj",condesj);
        return stringObjectHashMap;
    }

    public Map<String, Object> getytj(String clwz, String clsj) {

        //处理前端传递名称数据

        String realclwz = clwz;//处理名称
//        String realclwz = clwz.substring(0, clwz.indexOf(":"));//处理名称
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M");
        // 处理第一个日期
        YearMonth yearMonth1 = YearMonth.parse(clsj, formatter);  //比如是2025-6
        YearMonth nextMonth1 = yearMonth1.plusMonths(1); //加一变成2025-7
        YearMonth lastMonth = yearMonth1.minusMonths(1); //上个月的数据
        YearMonth lastYearSameMonth = yearMonth1.minusYears(1); //去年同一个月
        YearMonth lastYearNextMonth = lastYearSameMonth.plusMonths(1);//去年下一个月
        //根据名称和时间查询数据库本日数据
        List<Double> brsj =hdbDao.getdltjcxmonthsList22(realclwz,clsj+"-01",nextMonth1+"-01");
        List<Double> qn =hdbDao.getdltjcxmonthsList22(realclwz,lastYearSameMonth+"-01",lastYearNextMonth+"-01");
        List<Double> zr =hdbDao.getdltjcxmonthsList22(realclwz,lastMonth+"-01",clsj+"-01");
        //时间切换，做去年本日和昨日数据

        //有功:  当日电量 昨日电量 环比差  环比 去年同期电量 同比差 同比

        double brzl = calculateSumWithTwoDecimals(brsj);
        double qnzl = calculateSumWithTwoDecimals(qn);
        double zrzl = calculateSumWithTwoDecimals(zr);
        Map<Object, Object> ygmap = new LinkedHashMap<>();
        ygmap.put("brzl",brzl);
        ygmap.put("qnzl",qnzl);
        ygmap.put("zrzl",zrzl);
        Double hbc =brzl-zrzl;
        ygmap.put("hbc",String.format("%.2f", hbc));
        Double hb =hbc/zrzl;
        ygmap.put("hb",String.format("%.2f%%", hb));
        Double tbc =brzl -qnzl;
        ygmap.put("tbc",String.format("%.2f", tbc));
        if (qnzl ==0){
            ygmap.put("tb","/");
        }else {
            Double tb =tbc/qnzl;
            ygmap.put("tb",String.format("%.2f%%", tb));
        }

        HashMap<Object, Object> wgmap = new HashMap<>();
        wgmap.put("brzl",0);
        wgmap.put("qnzl",0);
        wgmap.put("zrzl",0);
        wgmap.put("hbc",0);
        wgmap.put("hb",0);
        wgmap.put("ybc",0);
        wgmap.put("tb",0);
        //无功:  当日电量 昨日电量 环比差  环比 去年同期电量 同比差 同比
        Map<String, Object> map = new HashMap<>();
        map.put("qn", qn); //去年数据集合
        map.put("zr", zr); //昨日数据集合
        map.put("br", brsj); //本日数据集合
        map.put("yg",ygmap);
        map.put("wg",wgmap);
//        System.out.println(map.get("br"));
        return map;
    }

    public Map<String, Object> getntj(String clwz, String clsj) {

        String realclwz = clwz;//处理名称
//        String realclwz = clwz.substring(0, clwz.indexOf(":"));//处理名称
        String result = String.valueOf(Integer.parseInt(clsj) + 1);
        String results = String.valueOf(Integer.parseInt(clsj) - 1);
        List<Double> brsj = hdbDao.getdltjcxyearList22(realclwz,clsj+"-01-01",result+"-01-01");
        List<Double> qn = hdbDao.getdltjcxyearList22(realclwz,results+"-01-01",clsj+"-01-01");

        double brzl = calculateSumWithTwoDecimals(brsj);
        double qnzl = calculateSumWithTwoDecimals(qn);
        Map<Object, Object> ygmap = new LinkedHashMap<>();
        ygmap.put("brzl",brzl);
        ygmap.put("qnzl",qnzl);
        Double tbc =brzl -qnzl;
        ygmap.put("hbc",String.format("%.2f", tbc));
        if (qnzl ==0){
            ygmap.put("tb","/");
        }else {
            Double tb =tbc/qnzl;
            ygmap.put("hb",String.format("%.2f%%", tb));
        }

        HashMap<Object, Object> wgmap = new HashMap<>();
        wgmap.put("brzl",0);
        wgmap.put("qnzl",0);
        wgmap.put("zrzl",0);
        wgmap.put("hbc",0);
        wgmap.put("hb",0);
        wgmap.put("ybc",0);
        wgmap.put("tb",0);
        Map<String, Object> map = new HashMap<>();
        map.put("qn", qn); //去年数据集合
        map.put("jn", brsj); //本日数据集合
        map.put("yg", ygmap);
        map.put("wg", wgmap);
//        System.out.println(map.get("jn"));
        return map;
    }

    public void setOnePDS(String data ,int i) {
        //添加大区信息库内容，四个总表的有功减去无功
        //查询出1号配四个总表最新的w和q
       Double OnePDS = hdbDao.getOnePDS();
       if (i ==1){
           //整点数据，计算电费
           //查询出四个总表最新的耗电量,总电量
           Map<String,Object> OnePDShdl =hdbDao.getOnePDShdl();
           Double zdl = hdbDao.getzdl();
           //向数据库内添加信息
           hdbDao.setOnePDS(OnePDS,OnePDShdl.get("ygdl"),data,zdl);
       }else {
           //非整点，不计算电量
           //向数据库内添加信息
           hdbDao.setOnePDS(OnePDS,null,data,null);
       }

       //查询出2号配四个总表最新的w和q 负荷
        Double TwoPDS = hdbDao.getTwoPDS();
        Double SixPDS = hdbDao.getSixPDS();

        if (i ==1){
            //整点数据，计算电费
            //查询出四个总表最新的耗电量,总电量
            Map<String,Object> OnePDShdl =hdbDao.getTwoPDShdl();
            Map<String,Object> SixPDShdl =hdbDao.getSixPDShdl();
            Double zdl = hdbDao.getTwozdl();
            Double Szdl = hdbDao.getSixzdl();
            //向数据库内添加信息
            hdbDao.setTwoPDS(TwoPDS,OnePDShdl.get("ygdl"),data,zdl,"2号配电室");
            hdbDao.setTwoPDS(SixPDS,SixPDShdl.get("ygdl"),data,Szdl,"6号配电室");

        }else {
            //非整点，不计算电量
            //向数据库内添加信息
            hdbDao.setTwoPDS(TwoPDS,null,data,null,"2号配电室");
            hdbDao.setTwoPDS(SixPDS,null,data,null,"6号配电室");
        }


    }

    public Map<String, Object> getlbqByName(String name) {
        return hdbDao.getlbqByName(name);
    }

    public List<Map<String, Object>> getbyqxq(String name) {
        List<Map<String,Object>> objects = new ArrayList<>();
        //添加四个变压器的数据
        //1.查询四个变压器的数据
        String cmsliststr ="";
        if (name.equals("1号配电室")){
            cmsliststr ="'1AA1-1','2AA15-1','3AA1-1','4AA17-1'";
        }else if (name.equals("2号配电室")){
            cmsliststr ="'2-1AA1-1','2-2AA14-1','2-3AA1-1','2-4AA14-1'";
        }else if (name.equals("6号配电室")){
            cmsliststr ="'6-1AA1-1','6-2AA11-1'";
        }
        List<String> list = hdbDao.getbyqxq(cmsliststr);

        double sum = 0.0;
        List<Double> doubleList = new ArrayList<>();
        for (String str : list) {
            Double value = Double.parseDouble(str); // 转换为Double类型
            doubleList.add(value);
            sum += value;
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Double value : doubleList) {
            Map<String, Object> map = new HashMap<>();
            map.put("originalValue", value); // 原始值
            double ratio = (value / sum) * 100; // 占比（百分比形式）
            map.put("ratio", String.format("%.2f", ratio)); // 保留两位小数并格式化
            double fuzai =(value/1600) * 100;
            map.put("fuzai", String.format("%.2f", fuzai)); // 保留两位小数并格式化
            resultList.add(map);
        }
        //2.计算占比和负载值
        //3.封装条数据
        return resultList;
    }

    /**
     * 查询近期报警记录。
     */
    public List<Map<String, Object>> getrecentalarm() {

        return hdbDao.getrecentalarm();
    }

    /**
     * 按日汇总电量数据并生成图表结果。
     */
    public List<Map<String, Object>> getday(String pds, String date) {
        pds = normalizeDlfxjlPds(pds);
        if ("all".equals(pds)) {
            return getdayAll(date);
        }
        if ("2".equals(pds) || "6".equals(pds)) {
            return getdayByConfig(pds, date);
        }

        List<Map<String, Object>> maps = new ArrayList<>();


        if (pds.equals("1")){
            System.out.println("查询"+pds+"号配电室");
        }
        //办公用电及照明
        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA7-1", "1AA8-1"
                , "1AA9-1",  "1AA12-2", "1AA12-3", "1AA13-1", "1AA13-2", "1AA13-3", "1AA13-4"
                , "1AA13-5", "1AA14-1", "1AA14-2", "1AA14-3", "1AA14-4", "1AA14-5", "1AA14-6", "1AA14-7", "1AA14-8"
                , "2AA2-1", "2AA2-2", "2AA2-3", "2AA2-4", "2AA2-5", "2AA2-6", "2AA2-7", "2AA2-8"
                , "2AA3-1", "2AA3-2", "2AA3-3", "2AA3-4", "2AA3-5", "2AA4-2", "2AA4-3", "2AA8-1", "2AA9-1", "2AA10-1", "2AA11-1"
       ));
//        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA1-2"));
        List<Map<String, Object>> bangongZM = hdbDao.getbyday(zhaoming, date);
        Map<String, Object> bangongZMJH = new LinkedHashMap<>();
        bangongZMJH.put("id", "11");
        bangongZMJH.put("item_name", "办公用电及照明");
        bangongZMJH.put("pid", "1");
//        getbyday.add(data1);
//        maps.add(data1);

        //公共区域照明
        List<String> gonggongZM = new ArrayList<>(Arrays.asList(
                "1AA10-1","1AA10-2","1AA10-3","1AA10-4","1AA11-1","1AA11-2","2AA5-1","2AA5-2","4AA9-1","4AA9-2",
                 "3AA9-1", "3AA9-2"

        ));
        List<Map<String, Object>> gonggongZMSJ = hdbDao.getbyday(gonggongZM, date);
        Map<String, Object> gonggongZMSJJH = new LinkedHashMap<>();
        gonggongZMSJJH.put("id", "12");
        gonggongZMSJJH.put("item_name","公共区域照明");
        gonggongZMSJJH.put("pid","1");


        //泛光照明
        List<String> fanguangZM = new ArrayList<>(Arrays.asList(
                "1AA6-2"
        ));
        List<Map<String, Object>> fanguangZMSJ = hdbDao.getbyday(fanguangZM, date);
        Map<String, Object> fanguangZMSJJH = new LinkedHashMap<>();
        fanguangZMSJJH.put("id", "13");
        fanguangZMSJJH.put("item_name","泛光照明");
        fanguangZMSJJH.put("pid","1");

        Map<String, Object> data11 = new LinkedHashMap<>();
        data11.put("id","1");
        data11.put("pid","-1");
        data11.put("item_name", "照明");
        List<Map<String, Object>> zhaomingAllMaps = new ArrayList<>();
        zhaomingAllMaps.add(xunhuanBL(bangongZM,bangongZMJH));
        zhaomingAllMaps.add(xunhuanBL(gonggongZMSJ,gonggongZMSJJH));
        zhaomingAllMaps.add(xunhuanBL(fanguangZMSJ,fanguangZMSJJH));
        Map<String, Object> ZZZM = sumHourMaps(zhaomingAllMaps);
        data11.putAll(ZZZM);
        //添加照明区域
        maps.add(data11);
        maps.add(xunhuanBL(bangongZM,bangongZMJH));
        maps.add(xunhuanBL(gonggongZMSJ,gonggongZMSJJH));
        maps.add(xunhuanBL(fanguangZMSJ,fanguangZMSJJH));


//        data11.add(xunhuanBL(xiaofangFJSJ,xiaofangFJSJJH));
//        maps.add(data11);

        //电梯模块
        List<String> dianti = new ArrayList<>(Arrays.asList("1AA15-1","1AA16-1", "1AA16-2", "1AA16-3", "1AA16-4", "2AA1-1"
                , "2AA1-2", "2AA1-3", "2AA1-4", "3AA17-4", "3AA17-5", "3AA18-1", "3AA18-2", "4AA2-1","4AA2-2","4AA2-3","4AA2-4", "2AA7-1"));
        List<Map<String, Object>> diantimk = hdbDao.getbyday(dianti, date);
        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("id", "21");
        data2.put("item_name","电梯设备");
        data2.put("pid","2");
//        System.out.println(maps);

        //消防风机设备
        List<String> xiaofangFJ = new ArrayList<>(Arrays.asList("3AA15-1","3AA9-3", "3AA10-1", "3AA10-2", "3AA10-3", "3AA11-1", "3AA11-2", "3AA11-3"
                , "3AA11-4", "3AA11-5", "3AA12-1", "3AA12-2", "3AA12-3", "3AA12-4", "3AA12-5", "3AA12-6",
                "4AA8-1","4AA8-3","4AA8-2","4AA8-4","4AA8-5","4AA8-6","4AA8-7","4AA7-1"
                ,"4AA7-2","4AA7-3","4AA7-4","4AA7-5","4AA4-1","4AA6-1"));
        List<Map<String, Object>> xiaofangFJSJ = hdbDao.getbyday(xiaofangFJ, date);
        Map<String, Object> xiaofangFJSJJH = new LinkedHashMap<>();
        xiaofangFJSJJH.put("id", "22");
        xiaofangFJSJJH.put("item_name","消防风机设备");
        xiaofangFJSJJH.put("pid","2");

        //空调设备
        List<String> kongtiaoSB = new ArrayList<>(Arrays.asList("3AA7-2","3AA7-3","3AA8-1","3AA8-2","3AA8-3","3AA14-2"
                ,"4AA12-1","4AA12-2","4AA12-3","4AA13-1","4AA13-2","4AA13-3","4AA5-2"
                ,"1AA8-2","1AA8-3","2AA9-3"
        ));
        List<Map<String, Object>> kongtiaoSBSJ = hdbDao.getbyday(kongtiaoSB, date);
        Map<String, Object> kongtiaoSBSJJH = new LinkedHashMap<>();
        kongtiaoSBSJJH.put("id", "23");
        kongtiaoSBSJJH.put("item_name","空调设备");
        kongtiaoSBSJJH.put("pid","2");

        //潜污泵设备
        List<String> qianwubengSB = new ArrayList<>(Arrays.asList("1AA16-5","1AA16-6","2AA1-5","2AA1-6","4AA6-2"
                ,"4AA6-3","4AA6-4","4AA6-5","3AA13-2","3AA13-3"));
        List<Map<String, Object>> qianwubengSBSJ = hdbDao.getbyday(qianwubengSB, date);
        Map<String, Object> qianwubengSBSJJH = new LinkedHashMap<>();
        qianwubengSBSJJH.put("id", "24");
        qianwubengSBSJJH.put("item_name","潜污泵设备");
        qianwubengSBSJJH.put("pid","2");

        //电动窗帘设备
        List<String> diandongCL = new ArrayList<>(Arrays.asList("4AA1-1","4AA1-2","4AA1-4","3AA17-1","3AA17-2","3AA17-3"));
        List<Map<String, Object>> diandongCLSJ = hdbDao.getbyday(diandongCL, date);
        Map<String, Object> diandongCLSJJH = new LinkedHashMap<>();
        diandongCLSJJH.put("id", "25");
        diandongCLSJJH.put("item_name","电动窗帘");
        diandongCLSJJH.put("pid","2");

        //电伴热设备
        List<String> dianbanreSB = new ArrayList<>(Arrays.asList("4AA1-6","4AA5-5","4AA5-4","3AA18-5"));
        List<Map<String, Object>> dianbanreSBSJ = hdbDao.getbyday(dianbanreSB, date);
        Map<String, Object> dianbanreSBSJJH = new LinkedHashMap<>();
        dianbanreSBSJJH.put("id", "26");
        dianbanreSBSJJH.put("item_name","电伴热");
        dianbanreSBSJJH.put("pid","2");

        //洗车房设备
        List<String> xichefangSB = new ArrayList<>(Arrays.asList("1AA9-3"));
        List<Map<String, Object>> xichefangSBSJ = hdbDao.getbyday(xichefangSB, date);
        Map<String, Object> xichefangSBSJJH = new LinkedHashMap<>();
        xichefangSBSJJH.put("id", "27");
        xichefangSBSJJH.put("item_name","洗车房");
        xichefangSBSJJH.put("pid","2");

        //洗浴设备
        List<String> xiyuSB = new ArrayList<>(Arrays.asList("1AA12-6"));
        List<Map<String, Object>> xiyuSBSJ = hdbDao.getbyday(xiyuSB, date);
        Map<String, Object> xiyuSBSJJH = new LinkedHashMap<>();
        xiyuSBSJJH.put("id", "28");
        xiyuSBSJJH.put("item_name","洗浴");
        xiyuSBSJJH.put("pid","2");
        //供水设备
        List<String> gongshuiSB = new ArrayList<>(Arrays.asList("1AA15-2","1AA16-7","2AA1-7","2AA7-4","2AA7-2"));
        List<Map<String, Object>> gongshuiSBSJ = hdbDao.getbyday(gongshuiSB, date);
        Map<String, Object> gongshuiSBSJJH = new LinkedHashMap<>();
        gongshuiSBSJJH.put("id", "29");
        gongshuiSBSJJH.put("item_name","供水");
        gongshuiSBSJJH.put("pid","2");
        //景观水设备
//        List<String> jingguanshuiSB = new ArrayList<>(Arrays.asList("2AA7-2"));
//        List<Map<String, Object>> jingguanshuiSBSJ = hdbDao.getbyday(qianwubengSB, date);
//        Map<String, Object> jingguanshuiSBSJJH = new LinkedHashMap<>();
//        jingguanshuiSBSJJH.put("id", "29");
//        jingguanshuiSBSJJH.put("item_name","供水");
//        jingguanshuiSBSJJH.put("pid","2");

        //充电桩设备
        List<String> chongdianzSB = new ArrayList<>(Arrays.asList("3AA6-1","3AA6-2","3AA6-3","3AA6-4","3AA7-1"));
        List<Map<String, Object>> chongdianzSBSJ = hdbDao.getbyday(chongdianzSB, date);
        Map<String, Object> chongdianzSBSJJH = new LinkedHashMap<>();
        chongdianzSBSJJH.put("id", "210");
        chongdianzSBSJJH.put("item_name","充电桩");
        chongdianzSBSJJH.put("pid","2");


        //厨房设备
        List<String> chufangSB = new ArrayList<>(Arrays.asList("4AA11-1"));
        List<Map<String, Object>> chufangSBSJ = hdbDao.getbyday(chufangSB, date);
        Map<String, Object> chufangSBSJJH = new LinkedHashMap<>();
        chufangSBSJJH.put("id", "211");
        chufangSBSJJH.put("item_name","厨房");
        chufangSBSJJH.put("pid","2");

        //设备模块总计
        Map<String, Object> data12 = new LinkedHashMap<>();
        List<Map<String, Object>> shebeiAllMaps = new ArrayList<>();
//        shebeiAllMaps.add(diantimk);

        shebeiAllMaps.add(xunhuanBL(diantimk,data2));
        shebeiAllMaps.add(xunhuanBL(xiaofangFJSJ,xiaofangFJSJJH));
        shebeiAllMaps.add(xunhuanBL(kongtiaoSBSJ,kongtiaoSBSJJH));
        shebeiAllMaps.add(xunhuanBL(qianwubengSBSJ,qianwubengSBSJJH));
        shebeiAllMaps.add(xunhuanBL(diandongCLSJ,diandongCLSJJH));
        shebeiAllMaps.add(xunhuanBL(dianbanreSBSJ,dianbanreSBSJJH));
        shebeiAllMaps.add(xunhuanBL(xichefangSBSJ,xichefangSBSJJH));
        shebeiAllMaps.add(xunhuanBL(xiyuSBSJ,xiyuSBSJJH));
        shebeiAllMaps.add(xunhuanBL(gongshuiSBSJ,gongshuiSBSJJH));
        shebeiAllMaps.add(xunhuanBL(chongdianzSBSJ,chongdianzSBSJJH));
        shebeiAllMaps.add(xunhuanBL(chufangSBSJ,chufangSBSJJH));

        Map<String, Object> data13 = sumHourMaps(shebeiAllMaps);
        data12.put("id","2");
        data12.put("pid","-1");
        data12.put("item_name","设备");
        data12.putAll(data13);
        maps.add(data12);


        maps.add(xunhuanBL(diantimk,data2));
        maps.add(xunhuanBL(xiaofangFJSJ,xiaofangFJSJJH));
        maps.add(xunhuanBL(kongtiaoSBSJ,kongtiaoSBSJJH));
        maps.add(xunhuanBL(qianwubengSBSJ,qianwubengSBSJJH));
        maps.add(xunhuanBL(diandongCLSJ,diandongCLSJJH));
        maps.add(xunhuanBL(dianbanreSBSJ,dianbanreSBSJJH));
        maps.add(xunhuanBL(xichefangSBSJ,xichefangSBSJJH));
        maps.add(xunhuanBL(xiyuSBSJ,xiyuSBSJJH));
        maps.add(xunhuanBL(gongshuiSBSJ,gongshuiSBSJJH));
        maps.add(xunhuanBL(chongdianzSBSJ,chongdianzSBSJJH));
        maps.add(xunhuanBL(chufangSBSJ,chufangSBSJJH));

//        System.out.println("传递的电量分项信息"+maps);


        //档案室
        List<String> danganshSB = new ArrayList<>(Arrays.asList("1AA11-5","2AA5-5"));
        List<Map<String, Object>> danganshSB_SJ = hdbDao.getbyday(danganshSB, date);
        Map<String, Object> danganshSB_SJ_JH = new LinkedHashMap<>();
        danganshSB_SJ_JH.put("id", "31");
        danganshSB_SJ_JH.put("item_name","档案室");
        danganshSB_SJ_JH.put("pid","3");
        //网络机房
        List<String> wangluojfSB = new ArrayList<>(Arrays.asList("3AA14-1","3AA15-2","3AA15-3"
                ,"4AA4-2","4AA4-3","4AA4-4"));
        List<Map<String, Object>> wangluojfSB_SJ = hdbDao.getbyday(wangluojfSB, date);
        Map<String, Object> wangluojfSB_SJ_JH = new LinkedHashMap<>();
        wangluojfSB_SJ_JH.put("id", "32");
        wangluojfSB_SJ_JH.put("item_name","网络机房");
        wangluojfSB_SJ_JH.put("pid","3");
        //弱电机房
        List<String> ruodianjfSB = new ArrayList<>(Arrays.asList("4AA3-1","4AA3-2","4AA3-3","4AA3-4"
                ,"3AA16-1","3AA16-2","3AA16-3","3AA16-4"));
        List<Map<String, Object>> ruodianjfSB_SJ = hdbDao.getbyday(ruodianjfSB, date);
        Map<String, Object> ruodianjfSB_SJ_JH = new LinkedHashMap<>();
        ruodianjfSB_SJ_JH.put("id", "33");
        ruodianjfSB_SJ_JH.put("item_name","弱电机房");
        ruodianjfSB_SJ_JH.put("pid","3");

        //机房模块统计
//        Map<String, Object> jifang = new LinkedHashMap<>();
        List<Map<String, Object>> jifangAllMaps = new ArrayList<>();
        jifangAllMaps.add(xunhuanBL(danganshSB_SJ,danganshSB_SJ_JH));
        jifangAllMaps.add(xunhuanBL(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        jifangAllMaps.add(xunhuanBL(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));
        Map<String, Object> jifang = sumHourMaps(jifangAllMaps);
        jifang.put("id","3");
        jifang.put("pid","-1");
        jifang.put("item_name","机房");

        maps.add(jifang);
        maps.add(xunhuanBL(danganshSB_SJ,danganshSB_SJ_JH));
        maps.add(xunhuanBL(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        maps.add(xunhuanBL(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));

        //--------------------------分项分割线----------------------------------
        List<Map<String, Object>> allMaps = new ArrayList<>();
        allMaps.add(data11);
        allMaps.add(data12);
        allMaps.add(jifang);
        // 计算总和
        Map<String, Object> total = sumHourMaps(allMaps);
        total.put("id",99);
        total.put("pid","-1");
        total.put("isTotal",true);
        total.put("item_name","总计");
        //添加总计，尖峰平谷
        //查询电量表的jfpg
        List<Map<String, Object>> jfpglist = hdbDao.getDLjfpgBydate(date);
//        total.put("jfpg",jfpglist);
        for (Map<String, Object> map :jfpglist){
            if (map != null) {
                total.putAll(map);
            }
        }
        maps.add(total);
        return maps;
    }

    //按月处理数据
    public static Map<String, Object> xunhuanBLByMonth(List<Map<String, Object>> SJ, Map<String, Object> data) {
        for (Map<String, Object> cms : SJ) {
            Integer day = null;
            Double adspSum = null;
            for (Map.Entry<String, Object> entry : cms.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if ("hour".equals(key)) {
                    LocalDate localDate = toLocalDate(value);
                    if (localDate != null) {
                        day = localDate.getDayOfMonth();
                    }
                } else if ("adsp_sum".equals(key)) {
                    adspSum = parseValueToDouble(value);
                }
            }
            if (day != null && adspSum != null) {
                data.put("day_" + day, String.valueOf(adspSum));
            }
        }
        return data;
    }
//根据月获取电量分析计量
/**
 * 按月汇总电量数据并生成图表结果。
 */
public List<Map<String, Object>> getmonth(String pds, String date) {
    pds = normalizeDlfxjlPds(pds);
    if ("all".equals(pds)) {
        return getmonthAll(date);
    }
    if ("2".equals(pds) || "6".equals(pds)) {
        return getmonthByConfig(pds, date);
    }

    List<Map<String, Object>> maps = new ArrayList<>();


    if (pds.equals("1")){
            System.out.println("查询"+pds+"号配电室");
        }
        //办公用电及照明
        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA7-1", "1AA8-1"
                , "1AA9-1",  "1AA12-2", "1AA12-3", "1AA13-1", "1AA13-2", "1AA13-3", "1AA13-4"
                , "1AA13-5", "1AA14-1", "1AA14-2", "1AA14-3", "1AA14-4", "1AA14-5", "1AA14-6", "1AA14-7", "1AA14-8"
                , "2AA2-1", "2AA2-2", "2AA2-3", "2AA2-4", "2AA2-5", "2AA2-6", "2AA2-7", "2AA2-8"
                , "2AA3-1", "2AA3-2", "2AA3-3", "2AA3-4", "2AA3-5", "2AA4-2", "2AA4-3", "2AA8-1", "2AA9-1", "2AA10-1", "2AA11-1"
        ));
//        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA1-2"));
        List<Map<String, Object>> bangongZM = hdbDao.getbymonth(zhaoming, date);
        Map<String, Object> bangongZMJH = new LinkedHashMap<>();
        bangongZMJH.put("id", "11");
        bangongZMJH.put("item_name", "办公用电及照明");
        bangongZMJH.put("pid", "1");
//        getbyday.add(data1);
//        maps.add(data1);

        //公共区域照明
        List<String> gonggongZM = new ArrayList<>(Arrays.asList(
                "1AA10-1","1AA10-2","1AA10-3","1AA10-4","1AA11-1","1AA11-2","2AA5-1","2AA5-2","4AA9-1","4AA9-2",
                "3AA9-1", "3AA9-2"

        ));
        List<Map<String, Object>> gonggongZMSJ = hdbDao.getbymonth(gonggongZM, date);
        Map<String, Object> gonggongZMSJJH = new LinkedHashMap<>();
        gonggongZMSJJH.put("id", "12");
        gonggongZMSJJH.put("item_name","公共区域照明");
        gonggongZMSJJH.put("pid","1");


        //泛光照明
        List<String> fanguangZM = new ArrayList<>(Arrays.asList(
                "1AA6-2"
        ));
        List<Map<String, Object>> fanguangZMSJ = hdbDao.getbymonth(fanguangZM, date);
        Map<String, Object> fanguangZMSJJH = new LinkedHashMap<>();
        fanguangZMSJJH.put("id", "13");
        fanguangZMSJJH.put("item_name","泛光照明");
        fanguangZMSJJH.put("pid","1");

        Map<String, Object> data11 = new LinkedHashMap<>();
        data11.put("id","1");
        data11.put("pid","-1");
        data11.put("item_name", "照明");
        List<Map<String, Object>> zhaomingAllMaps = new ArrayList<>();
        zhaomingAllMaps.add(xunhuanBLByMonth(bangongZM,bangongZMJH));
        zhaomingAllMaps.add(xunhuanBLByMonth(gonggongZMSJ,gonggongZMSJJH));
        zhaomingAllMaps.add(xunhuanBLByMonth(fanguangZMSJ,fanguangZMSJJH));
        Map<String, Object> ZZZM = sumIndexedMaps(zhaomingAllMaps, "day_", 1, 31);
        data11.putAll(ZZZM);
        //添加照明区域
        maps.add(data11);
        maps.add(xunhuanBLByMonth(bangongZM,bangongZMJH));
        maps.add(xunhuanBLByMonth(gonggongZMSJ,gonggongZMSJJH));
        maps.add(xunhuanBLByMonth(fanguangZMSJ,fanguangZMSJJH));


//        data11.add(xunhuanBL(xiaofangFJSJ,xiaofangFJSJJH));
//        maps.add(data11);

        //电梯模块
        List<String> dianti = new ArrayList<>(Arrays.asList("1AA15-1","1AA16-1", "1AA16-2", "1AA16-3", "1AA16-4", "2AA1-1"
                , "2AA1-2", "2AA1-3", "2AA1-4", "3AA17-4", "3AA17-5", "3AA18-1", "3AA18-2", "4AA2-1","4AA2-2","4AA2-3","4AA2-4", "2AA7-1"));
        List<Map<String, Object>> diantimk = hdbDao.getbymonth(dianti, date);
        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("id", "21");
        data2.put("item_name","电梯设备");
        data2.put("pid","2");
//        System.out.println(maps);

        //消防风机设备
        List<String> xiaofangFJ = new ArrayList<>(Arrays.asList("3AA15-1","3AA9-3", "3AA10-1", "3AA10-2", "3AA10-3", "3AA11-1", "3AA11-2", "3AA11-3"
                , "3AA11-4", "3AA11-5", "3AA12-1", "3AA12-2", "3AA12-3", "3AA12-4", "3AA12-5", "3AA12-6",
                "4AA8-1","4AA8-3","4AA8-2","4AA8-4","4AA8-5","4AA8-6","4AA8-7","4AA7-1"
                ,"4AA7-2","4AA7-3","4AA7-4","4AA7-5","4AA4-1","4AA6-1"));
        List<Map<String, Object>> xiaofangFJSJ = hdbDao.getbymonth(xiaofangFJ, date);
        Map<String, Object> xiaofangFJSJJH = new LinkedHashMap<>();
        xiaofangFJSJJH.put("id", "22");
        xiaofangFJSJJH.put("item_name","消防风机设备");
        xiaofangFJSJJH.put("pid","2");

        //空调设备
        List<String> kongtiaoSB = new ArrayList<>(Arrays.asList("3AA7-2","3AA7-3","3AA8-1","3AA8-2","3AA8-3","3AA14-2"
                ,"4AA12-1","4AA12-2","4AA12-3","4AA13-1","4AA13-2","4AA13-3","4AA5-2"
                ,"1AA8-2","1AA8-3","2AA9-3"
        ));
        List<Map<String, Object>> kongtiaoSBSJ = hdbDao.getbymonth(kongtiaoSB, date);
        Map<String, Object> kongtiaoSBSJJH = new LinkedHashMap<>();
        kongtiaoSBSJJH.put("id", "23");
        kongtiaoSBSJJH.put("item_name","空调设备");
        kongtiaoSBSJJH.put("pid","2");

        //潜污泵设备
        List<String> qianwubengSB = new ArrayList<>(Arrays.asList("1AA16-5","1AA16-6","2AA1-5","2AA1-6","4AA6-2"
                ,"4AA6-3","4AA6-4","4AA6-5","3AA13-2","3AA13-3"));
        List<Map<String, Object>> qianwubengSBSJ = hdbDao.getbymonth(qianwubengSB, date);
        Map<String, Object> qianwubengSBSJJH = new LinkedHashMap<>();
        qianwubengSBSJJH.put("id", "24");
        qianwubengSBSJJH.put("item_name","潜污泵设备");
        qianwubengSBSJJH.put("pid","2");

        //电动窗帘设备
        List<String> diandongCL = new ArrayList<>(Arrays.asList("4AA1-1","4AA1-2","4AA1-4","3AA17-1","3AA17-2","3AA17-3"));
        List<Map<String, Object>> diandongCLSJ = hdbDao.getbymonth(diandongCL, date);
        Map<String, Object> diandongCLSJJH = new LinkedHashMap<>();
        diandongCLSJJH.put("id", "25");
        diandongCLSJJH.put("item_name","电动窗帘");
        diandongCLSJJH.put("pid","2");

        //电伴热设备
        List<String> dianbanreSB = new ArrayList<>(Arrays.asList("4AA1-6","4AA5-5","4AA5-4","3AA18-5"));
        List<Map<String, Object>> dianbanreSBSJ = hdbDao.getbymonth(dianbanreSB, date);
        Map<String, Object> dianbanreSBSJJH = new LinkedHashMap<>();
        dianbanreSBSJJH.put("id", "26");
        dianbanreSBSJJH.put("item_name","电伴热");
        dianbanreSBSJJH.put("pid","2");

        //洗车房设备
        List<String> xichefangSB = new ArrayList<>(Arrays.asList("1AA9-3"));
        List<Map<String, Object>> xichefangSBSJ = hdbDao.getbymonth(xichefangSB, date);
        Map<String, Object> xichefangSBSJJH = new LinkedHashMap<>();
        xichefangSBSJJH.put("id", "27");
        xichefangSBSJJH.put("item_name","洗车房");
        xichefangSBSJJH.put("pid","2");

        //洗浴设备
        List<String> xiyuSB = new ArrayList<>(Arrays.asList("1AA12-6"));
        List<Map<String, Object>> xiyuSBSJ = hdbDao.getbymonth(xiyuSB, date);
        Map<String, Object> xiyuSBSJJH = new LinkedHashMap<>();
        xiyuSBSJJH.put("id", "28");
        xiyuSBSJJH.put("item_name","洗浴");
        xiyuSBSJJH.put("pid","2");
        //供水设备
        List<String> gongshuiSB = new ArrayList<>(Arrays.asList("1AA15-2","1AA16-7","2AA1-7","2AA7-4","2AA7-2"));
        List<Map<String, Object>> gongshuiSBSJ = hdbDao.getbymonth(gongshuiSB, date);
        Map<String, Object> gongshuiSBSJJH = new LinkedHashMap<>();
        gongshuiSBSJJH.put("id", "29");
        gongshuiSBSJJH.put("item_name","供水");
        gongshuiSBSJJH.put("pid","2");
        //景观水设备
//        List<String> jingguanshuiSB = new ArrayList<>(Arrays.asList("2AA7-2"));
//        List<Map<String, Object>> jingguanshuiSBSJ = hdbDao.getbymonth(qianwubengSB, date);
//        Map<String, Object> jingguanshuiSBSJJH = new LinkedHashMap<>();
//        jingguanshuiSBSJJH.put("id", "29");
//        jingguanshuiSBSJJH.put("item_name","供水");
//        jingguanshuiSBSJJH.put("pid","2");

        //充电桩设备
        List<String> chongdianzSB = new ArrayList<>(Arrays.asList("3AA6-1","3AA6-2","3AA6-3","3AA6-4","3AA7-1"));
        List<Map<String, Object>> chongdianzSBSJ = hdbDao.getbymonth(chongdianzSB, date);
        Map<String, Object> chongdianzSBSJJH = new LinkedHashMap<>();
        chongdianzSBSJJH.put("id", "210");
        chongdianzSBSJJH.put("item_name","充电桩");
        chongdianzSBSJJH.put("pid","2");


        //厨房设备
        List<String> chufangSB = new ArrayList<>(Arrays.asList("4AA11-1"));
        List<Map<String, Object>> chufangSBSJ = hdbDao.getbymonth(chufangSB, date);
        Map<String, Object> chufangSBSJJH = new LinkedHashMap<>();
        chufangSBSJJH.put("id", "211");
        chufangSBSJJH.put("item_name","厨房");
        chufangSBSJJH.put("pid","2");

        //设备模块总计
        Map<String, Object> data12 = new LinkedHashMap<>();
        List<Map<String, Object>> shebeiAllMaps = new ArrayList<>();
//        shebeiAllMaps.add(diantimk);

        shebeiAllMaps.add(xunhuanBLByMonth(diantimk,data2));
        shebeiAllMaps.add(xunhuanBLByMonth(xiaofangFJSJ,xiaofangFJSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(kongtiaoSBSJ,kongtiaoSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(qianwubengSBSJ,qianwubengSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(diandongCLSJ,diandongCLSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(dianbanreSBSJ,dianbanreSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(xichefangSBSJ,xichefangSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(xiyuSBSJ,xiyuSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(gongshuiSBSJ,gongshuiSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(chongdianzSBSJ,chongdianzSBSJJH));
        shebeiAllMaps.add(xunhuanBLByMonth(chufangSBSJ,chufangSBSJJH));

        Map<String, Object> data13 = sumIndexedMaps(shebeiAllMaps, "day_", 1, 31);
        data12.put("id","2");
        data12.put("pid","-1");
        data12.put("item_name","设备");
        data12.putAll(data13);
        maps.add(data12);


        maps.add(xunhuanBLByMonth(diantimk,data2));
        maps.add(xunhuanBLByMonth(xiaofangFJSJ,xiaofangFJSJJH));
        maps.add(xunhuanBLByMonth(kongtiaoSBSJ,kongtiaoSBSJJH));
        maps.add(xunhuanBLByMonth(qianwubengSBSJ,qianwubengSBSJJH));
        maps.add(xunhuanBLByMonth(diandongCLSJ,diandongCLSJJH));
        maps.add(xunhuanBLByMonth(dianbanreSBSJ,dianbanreSBSJJH));
        maps.add(xunhuanBLByMonth(xichefangSBSJ,xichefangSBSJJH));
        maps.add(xunhuanBLByMonth(xiyuSBSJ,xiyuSBSJJH));
        maps.add(xunhuanBLByMonth(gongshuiSBSJ,gongshuiSBSJJH));
        maps.add(xunhuanBLByMonth(chongdianzSBSJ,chongdianzSBSJJH));
        maps.add(xunhuanBLByMonth(chufangSBSJ,chufangSBSJJH));

//        System.out.println("传递的电量分项信息"+maps);


        //档案室
        List<String> danganshSB = new ArrayList<>(Arrays.asList("1AA11-5","2AA5-5"));
        List<Map<String, Object>> danganshSB_SJ = hdbDao.getbymonth(danganshSB, date);
        Map<String, Object> danganshSB_SJ_JH = new LinkedHashMap<>();
        danganshSB_SJ_JH.put("id", "31");
        danganshSB_SJ_JH.put("item_name","档案室");
        danganshSB_SJ_JH.put("pid","3");
        //网络机房
        List<String> wangluojfSB = new ArrayList<>(Arrays.asList("3AA14-1","3AA15-2","3AA15-3"
                ,"4AA4-2","4AA4-3","4AA4-4"));
        List<Map<String, Object>> wangluojfSB_SJ = hdbDao.getbymonth(wangluojfSB, date);
        Map<String, Object> wangluojfSB_SJ_JH = new LinkedHashMap<>();
        wangluojfSB_SJ_JH.put("id", "32");
        wangluojfSB_SJ_JH.put("item_name","网络机房");
        wangluojfSB_SJ_JH.put("pid","3");
        //弱电机房
        List<String> ruodianjfSB = new ArrayList<>(Arrays.asList("4AA3-1","4AA3-2","4AA3-3","4AA3-4"
                ,"3AA16-1","3AA16-2","3AA16-3","3AA16-4"));
        List<Map<String, Object>> ruodianjfSB_SJ = hdbDao.getbymonth(ruodianjfSB, date);
        Map<String, Object> ruodianjfSB_SJ_JH = new LinkedHashMap<>();
        ruodianjfSB_SJ_JH.put("id", "33");
        ruodianjfSB_SJ_JH.put("item_name","弱电机房");
        ruodianjfSB_SJ_JH.put("pid","3");

        //机房模块统计
//        Map<String, Object> jifang = new LinkedHashMap<>();
        List<Map<String, Object>> jifangAllMaps = new ArrayList<>();
        jifangAllMaps.add(xunhuanBLByMonth(danganshSB_SJ,danganshSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByMonth(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByMonth(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));
        Map<String, Object> jifang = sumIndexedMaps(jifangAllMaps, "day_", 1, 31);
        jifang.put("id","3");
        jifang.put("pid","-1");
        jifang.put("item_name","机房");

        maps.add(jifang);
        maps.add(xunhuanBLByMonth(danganshSB_SJ,danganshSB_SJ_JH));
        maps.add(xunhuanBLByMonth(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        maps.add(xunhuanBLByMonth(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));

        //--------------------------分项分割线----------------------------------
        List<Map<String, Object>> allMaps = new ArrayList<>();
        allMaps.add(data11);
        allMaps.add(data12);
        allMaps.add(jifang);
        // 计算总和
        Map<String, Object> total = sumIndexedMaps(allMaps, "day_", 1, 31);
        total.put("id",99);
        total.put("pid","-1");
        total.put("isTotal",true);
        total.put("item_name","总计");
        //添加总计，尖峰平谷
        //查询电量表的jfpg
        List<Map<String, Object>> jfpglist = hdbDao.getDLjfpgBymonth(date);
//        total.put("jfpg",jfpglist);
        for (Map<String, Object> map :jfpglist){
            if (map != null) {
                total.putAll(map);
            }
        }
        maps.add(total);
        return maps;
    }


    //根据年份处理数据
    public static Map<String, Object> xunhuanBLByYear(List<Map<String, Object>> SJ, Map<String, Object> data) {
        for (Map<String, Object> cms : SJ) {
            Integer month = null;
            Double adspSum = null;
            for (Map.Entry<String, Object> entry : cms.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if ("hour".equals(key)) {
                    YearMonth yearMonth = toYearMonth(value);
                    if (yearMonth != null) {
                        month = yearMonth.getMonthValue();
                    }
                } else if ("adsp_sum".equals(key)) {
                    adspSum = parseValueToDouble(value);
                }
            }
            if (month != null && adspSum != null) {
                data.put("month_" + month, String.valueOf(adspSum));
            }
        }
        return data;
    }

    /**
     * 按年汇总电量数据并生成图表结果。
     */
    public List<Map<String, Object>> getyear(String pds, String date) {
        pds = normalizeDlfxjlPds(pds);
        if ("all".equals(pds)) {
            return getyearAll(date);
        }
        if ("2".equals(pds) || "6".equals(pds)) {
            return getyearByConfig(pds, date);
        }

        List<Map<String, Object>> maps = new ArrayList<>();


        if (pds.equals("1")){
            System.out.println("查询"+pds+"号配电室");
        }
        //办公用电及照明
        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA7-1", "1AA8-1"
                , "1AA9-1",  "1AA12-2", "1AA12-3", "1AA13-1", "1AA13-2", "1AA13-3", "1AA13-4"
                , "1AA13-5", "1AA14-1", "1AA14-2", "1AA14-3", "1AA14-4", "1AA14-5", "1AA14-6", "1AA14-7", "1AA14-8"
                , "2AA2-1", "2AA2-2", "2AA2-3", "2AA2-4", "2AA2-5", "2AA2-6", "2AA2-7", "2AA2-8"
                , "2AA3-1", "2AA3-2", "2AA3-3", "2AA3-4", "2AA3-5", "2AA4-2", "2AA4-3", "2AA8-1", "2AA9-1", "2AA10-1", "2AA11-1"
        ));
//        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA1-2"));
        List<Map<String, Object>> bangongZM = hdbDao.getbyyear(zhaoming, date);
        Map<String, Object> bangongZMJH = new LinkedHashMap<>();
        bangongZMJH.put("id", "11");
        bangongZMJH.put("item_name", "办公用电及照明");
        bangongZMJH.put("pid", "1");
//        getbyday.add(data1);
//        maps.add(data1);

        //公共区域照明
        List<String> gonggongZM = new ArrayList<>(Arrays.asList(
                "1AA10-1","1AA10-2","1AA10-3","1AA10-4","1AA11-1","1AA11-2","2AA5-1","2AA5-2","4AA9-1","4AA9-2",
                "3AA9-1", "3AA9-2"

        ));
        List<Map<String, Object>> gonggongZMSJ = hdbDao.getbyyear(gonggongZM, date);
        Map<String, Object> gonggongZMSJJH = new LinkedHashMap<>();
        gonggongZMSJJH.put("id", "12");
        gonggongZMSJJH.put("item_name","公共区域照明");
        gonggongZMSJJH.put("pid","1");


        //泛光照明
        List<String> fanguangZM = new ArrayList<>(Arrays.asList(
                "1AA6-2"
        ));
        List<Map<String, Object>> fanguangZMSJ = hdbDao.getbyyear(fanguangZM, date);
        Map<String, Object> fanguangZMSJJH = new LinkedHashMap<>();
        fanguangZMSJJH.put("id", "13");
        fanguangZMSJJH.put("item_name","泛光照明");
        fanguangZMSJJH.put("pid","1");

        Map<String, Object> data11 = new LinkedHashMap<>();
        data11.put("id","1");
        data11.put("pid","-1");
        data11.put("item_name", "照明");
        List<Map<String, Object>> zhaomingAllMaps = new ArrayList<>();
        zhaomingAllMaps.add(xunhuanBLByYear(bangongZM,bangongZMJH));
        zhaomingAllMaps.add(xunhuanBLByYear(gonggongZMSJ,gonggongZMSJJH));
        zhaomingAllMaps.add(xunhuanBLByYear(fanguangZMSJ,fanguangZMSJJH));
        Map<String, Object> ZZZM = sumIndexedMaps(zhaomingAllMaps, "month_", 1, 12);
        data11.putAll(ZZZM);
        //添加照明区域
        maps.add(data11);
        maps.add(xunhuanBLByYear(bangongZM,bangongZMJH));
        maps.add(xunhuanBLByYear(gonggongZMSJ,gonggongZMSJJH));
        maps.add(xunhuanBLByYear(fanguangZMSJ,fanguangZMSJJH));


//        data11.add(xunhuanBL(xiaofangFJSJ,xiaofangFJSJJH));
//        maps.add(data11);

        //电梯模块
        List<String> dianti = new ArrayList<>(Arrays.asList("1AA15-1","1AA16-1", "1AA16-2", "1AA16-3", "1AA16-4", "2AA1-1"
                , "2AA1-2", "2AA1-3", "2AA1-4", "3AA17-4", "3AA17-5", "3AA18-1", "3AA18-2", "4AA2-1","4AA2-2","4AA2-3","4AA2-4", "2AA7-1"));
        List<Map<String, Object>> diantimk = hdbDao.getbyyear(dianti, date);
        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("id", "21");
        data2.put("item_name","电梯设备");
        data2.put("pid","2");
//        System.out.println(maps);

        //消防风机设备
        List<String> xiaofangFJ = new ArrayList<>(Arrays.asList("3AA15-1","3AA9-3", "3AA10-1", "3AA10-2", "3AA10-3", "3AA11-1", "3AA11-2", "3AA11-3"
                , "3AA11-4", "3AA11-5", "3AA12-1", "3AA12-2", "3AA12-3", "3AA12-4", "3AA12-5", "3AA12-6",
                "4AA8-1","4AA8-3","4AA8-2","4AA8-4","4AA8-5","4AA8-6","4AA8-7","4AA7-1"
                ,"4AA7-2","4AA7-3","4AA7-4","4AA7-5","4AA4-1","4AA6-1"));
        List<Map<String, Object>> xiaofangFJSJ = hdbDao.getbyyear(xiaofangFJ, date);
        Map<String, Object> xiaofangFJSJJH = new LinkedHashMap<>();
        xiaofangFJSJJH.put("id", "22");
        xiaofangFJSJJH.put("item_name","消防风机设备");
        xiaofangFJSJJH.put("pid","2");

        //空调设备
        List<String> kongtiaoSB = new ArrayList<>(Arrays.asList("3AA7-2","3AA7-3","3AA8-1","3AA8-2","3AA8-3","3AA14-2"
                ,"4AA12-1","4AA12-2","4AA12-3","4AA13-1","4AA13-2","4AA13-3","4AA5-2"
                ,"1AA8-2","1AA8-3","2AA9-3"
        ));
        List<Map<String, Object>> kongtiaoSBSJ = hdbDao.getbyyear(kongtiaoSB, date);
        Map<String, Object> kongtiaoSBSJJH = new LinkedHashMap<>();
        kongtiaoSBSJJH.put("id", "23");
        kongtiaoSBSJJH.put("item_name","空调设备");
        kongtiaoSBSJJH.put("pid","2");

        //潜污泵设备
        List<String> qianwubengSB = new ArrayList<>(Arrays.asList("1AA16-5","1AA16-6","2AA1-5","2AA1-6","4AA6-2"
                ,"4AA6-3","4AA6-4","4AA6-5","3AA13-2","3AA13-3"));
        List<Map<String, Object>> qianwubengSBSJ = hdbDao.getbyyear(qianwubengSB, date);
        Map<String, Object> qianwubengSBSJJH = new LinkedHashMap<>();
        qianwubengSBSJJH.put("id", "24");
        qianwubengSBSJJH.put("item_name","潜污泵设备");
        qianwubengSBSJJH.put("pid","2");

        //电动窗帘设备
        List<String> diandongCL = new ArrayList<>(Arrays.asList("4AA1-1","4AA1-2","4AA1-4","3AA17-1","3AA17-2","3AA17-3"));
        List<Map<String, Object>> diandongCLSJ = hdbDao.getbyyear(diandongCL, date);
        Map<String, Object> diandongCLSJJH = new LinkedHashMap<>();
        diandongCLSJJH.put("id", "25");
        diandongCLSJJH.put("item_name","电动窗帘");
        diandongCLSJJH.put("pid","2");

        //电伴热设备
        List<String> dianbanreSB = new ArrayList<>(Arrays.asList("4AA1-6","4AA5-5","4AA5-4","3AA18-5"));
        List<Map<String, Object>> dianbanreSBSJ = hdbDao.getbyyear(dianbanreSB, date);
        Map<String, Object> dianbanreSBSJJH = new LinkedHashMap<>();
        dianbanreSBSJJH.put("id", "26");
        dianbanreSBSJJH.put("item_name","电伴热");
        dianbanreSBSJJH.put("pid","2");

        //洗车房设备
        List<String> xichefangSB = new ArrayList<>(Arrays.asList("1AA9-3"));
        List<Map<String, Object>> xichefangSBSJ = hdbDao.getbyyear(xichefangSB, date);
        Map<String, Object> xichefangSBSJJH = new LinkedHashMap<>();
        xichefangSBSJJH.put("id", "27");
        xichefangSBSJJH.put("item_name","洗车房");
        xichefangSBSJJH.put("pid","2");

        //洗浴设备
        List<String> xiyuSB = new ArrayList<>(Arrays.asList("1AA12-6"));
        List<Map<String, Object>> xiyuSBSJ = hdbDao.getbyyear(xiyuSB, date);
        Map<String, Object> xiyuSBSJJH = new LinkedHashMap<>();
        xiyuSBSJJH.put("id", "28");
        xiyuSBSJJH.put("item_name","洗浴");
        xiyuSBSJJH.put("pid","2");
        //供水设备
        List<String> gongshuiSB = new ArrayList<>(Arrays.asList("1AA15-2","1AA16-7","2AA1-7","2AA7-4","2AA7-2"));
        List<Map<String, Object>> gongshuiSBSJ = hdbDao.getbyyear(gongshuiSB, date);
        Map<String, Object> gongshuiSBSJJH = new LinkedHashMap<>();
        gongshuiSBSJJH.put("id", "29");
        gongshuiSBSJJH.put("item_name","供水");
        gongshuiSBSJJH.put("pid","2");
        //景观水设备
//        List<String> jingguanshuiSB = new ArrayList<>(Arrays.asList("2AA7-2"));
//        List<Map<String, Object>> jingguanshuiSBSJ = hdbDao.getbymonth(qianwubengSB, date);
//        Map<String, Object> jingguanshuiSBSJJH = new LinkedHashMap<>();
//        jingguanshuiSBSJJH.put("id", "29");
//        jingguanshuiSBSJJH.put("item_name","供水");
//        jingguanshuiSBSJJH.put("pid","2");

        //充电桩设备
        List<String> chongdianzSB = new ArrayList<>(Arrays.asList("3AA6-1","3AA6-2","3AA6-3","3AA6-4","3AA7-1"));
        List<Map<String, Object>> chongdianzSBSJ = hdbDao.getbyyear(chongdianzSB, date);
        Map<String, Object> chongdianzSBSJJH = new LinkedHashMap<>();
        chongdianzSBSJJH.put("id", "210");
        chongdianzSBSJJH.put("item_name","充电桩");
        chongdianzSBSJJH.put("pid","2");


        //厨房设备
        List<String> chufangSB = new ArrayList<>(Arrays.asList("4AA11-1"));
        List<Map<String, Object>> chufangSBSJ = hdbDao.getbyyear(chufangSB, date);
        Map<String, Object> chufangSBSJJH = new LinkedHashMap<>();
        chufangSBSJJH.put("id", "211");
        chufangSBSJJH.put("item_name","厨房");
        chufangSBSJJH.put("pid","2");

        //设备模块总计
        Map<String, Object> data12 = new LinkedHashMap<>();
        List<Map<String, Object>> shebeiAllMaps = new ArrayList<>();
//        shebeiAllMaps.add(diantimk);

        shebeiAllMaps.add(xunhuanBLByYear(diantimk,data2));
        shebeiAllMaps.add(xunhuanBLByYear(xiaofangFJSJ,xiaofangFJSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(kongtiaoSBSJ,kongtiaoSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(qianwubengSBSJ,qianwubengSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(diandongCLSJ,diandongCLSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(dianbanreSBSJ,dianbanreSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(xichefangSBSJ,xichefangSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(xiyuSBSJ,xiyuSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(gongshuiSBSJ,gongshuiSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(chongdianzSBSJ,chongdianzSBSJJH));
        shebeiAllMaps.add(xunhuanBLByYear(chufangSBSJ,chufangSBSJJH));

        Map<String, Object> data13 = sumIndexedMaps(shebeiAllMaps, "month_", 1, 12);
        data12.put("id","2");
        data12.put("pid","-1");
        data12.put("item_name","设备");
        data12.putAll(data13);
        maps.add(data12);


        maps.add(xunhuanBLByYear(diantimk,data2));
        maps.add(xunhuanBLByYear(xiaofangFJSJ,xiaofangFJSJJH));
        maps.add(xunhuanBLByYear(kongtiaoSBSJ,kongtiaoSBSJJH));
        maps.add(xunhuanBLByYear(qianwubengSBSJ,qianwubengSBSJJH));
        maps.add(xunhuanBLByYear(diandongCLSJ,diandongCLSJJH));
        maps.add(xunhuanBLByYear(dianbanreSBSJ,dianbanreSBSJJH));
        maps.add(xunhuanBLByYear(xichefangSBSJ,xichefangSBSJJH));
        maps.add(xunhuanBLByYear(xiyuSBSJ,xiyuSBSJJH));
        maps.add(xunhuanBLByYear(gongshuiSBSJ,gongshuiSBSJJH));
        maps.add(xunhuanBLByYear(chongdianzSBSJ,chongdianzSBSJJH));
        maps.add(xunhuanBLByYear(chufangSBSJ,chufangSBSJJH));

//        System.out.println("传递的电量分项信息"+maps);


        //档案室
        List<String> danganshSB = new ArrayList<>(Arrays.asList("1AA11-5","2AA5-5"));
        List<Map<String, Object>> danganshSB_SJ = hdbDao.getbyyear(danganshSB, date);
        Map<String, Object> danganshSB_SJ_JH = new LinkedHashMap<>();
        danganshSB_SJ_JH.put("id", "31");
        danganshSB_SJ_JH.put("item_name","档案室");
        danganshSB_SJ_JH.put("pid","3");
        //网络机房
        List<String> wangluojfSB = new ArrayList<>(Arrays.asList("3AA14-1","3AA15-2","3AA15-3"
                ,"4AA4-2","4AA4-3","4AA4-4"));
        List<Map<String, Object>> wangluojfSB_SJ = hdbDao.getbyyear(wangluojfSB, date);
        Map<String, Object> wangluojfSB_SJ_JH = new LinkedHashMap<>();
        wangluojfSB_SJ_JH.put("id", "32");
        wangluojfSB_SJ_JH.put("item_name","网络机房");
        wangluojfSB_SJ_JH.put("pid","3");
        //弱电机房
        List<String> ruodianjfSB = new ArrayList<>(Arrays.asList("4AA3-1","4AA3-2","4AA3-3","4AA3-4"
                ,"3AA16-1","3AA16-2","3AA16-3","3AA16-4"));
        List<Map<String, Object>> ruodianjfSB_SJ = hdbDao.getbyyear(ruodianjfSB, date);
        Map<String, Object> ruodianjfSB_SJ_JH = new LinkedHashMap<>();
        ruodianjfSB_SJ_JH.put("id", "33");
        ruodianjfSB_SJ_JH.put("item_name","弱电机房");
        ruodianjfSB_SJ_JH.put("pid","3");

        //机房模块统计
//        Map<String, Object> jifang = new LinkedHashMap<>();
        List<Map<String, Object>> jifangAllMaps = new ArrayList<>();
        jifangAllMaps.add(xunhuanBLByYear(danganshSB_SJ,danganshSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByYear(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByYear(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));
        Map<String, Object> jifang = sumIndexedMaps(jifangAllMaps, "month_", 1, 12);
        jifang.put("id","3");
        jifang.put("pid","-1");
        jifang.put("item_name","机房");

        maps.add(jifang);
        maps.add(xunhuanBLByYear(danganshSB_SJ,danganshSB_SJ_JH));
        maps.add(xunhuanBLByYear(wangluojfSB_SJ,wangluojfSB_SJ_JH));
        maps.add(xunhuanBLByYear(ruodianjfSB_SJ,ruodianjfSB_SJ_JH));

        //--------------------------分项分割线----------------------------------
        List<Map<String, Object>> allMaps = new ArrayList<>();
        allMaps.add(data11);
        allMaps.add(data12);
        allMaps.add(jifang);
        // 计算总和
        Map<String, Object> total = sumIndexedMaps(allMaps, "month_", 1, 12);
        total.put("id",99);
        total.put("pid","-1");
        total.put("isTotal",true);
        total.put("item_name","总计");
        //添加总计，尖峰平谷
        //查询电量表的jfpg
        List<Map<String, Object>> jfpglist = hdbDao.getDLjfpgByyear(date);
//        total.put("jfpg",jfpglist);
        for (Map<String, Object> map :jfpglist){
            if (map != null) {
                total.putAll(map);
            }
        }
        maps.add(total);
        return maps;
    }

    /**
     * 按自定义时间范围汇总电量数据。
     */
    public List<Map<String, Object>> getcustom(String pds, String startDate, String endDate) {
        pds = normalizeDlfxjlPds(pds);
        if ("all".equals(pds)) {
            return getcustomAll(startDate, endDate);
        }
        if ("2".equals(pds) || "6".equals(pds)) {
            return getcustomByConfig(pds, startDate, endDate);
        }


        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        String normalizedStartDate = start.toString();
        String normalizedEndDate = end.toString();
        List<String> dateKeys = buildDateKeys(start, end);
        List<Map<String, Object>> maps = new ArrayList<>();

        List<String> zhaoming = new ArrayList<>(Arrays.asList("1AA6-1", "1AA7-1", "1AA8-1"
                , "1AA9-1", "1AA12-2", "1AA12-3", "1AA13-1", "1AA13-2", "1AA13-3", "1AA13-4"
                , "1AA13-5", "1AA14-1", "1AA14-2", "1AA14-3", "1AA14-4", "1AA14-5", "1AA14-6", "1AA14-7", "1AA14-8"
                , "2AA2-1", "2AA2-2", "2AA2-3", "2AA2-4", "2AA2-5", "2AA2-6", "2AA2-7", "2AA2-8"
                , "2AA3-1", "2AA3-2", "2AA3-3", "2AA3-4", "2AA3-5", "2AA4-2", "2AA4-3", "2AA8-1", "2AA9-1", "2AA10-1", "2AA11-1"
        ));
        List<Map<String, Object>> bangongZM = hdbDao.getbycustom(zhaoming, normalizedStartDate, normalizedEndDate);
        Map<String, Object> bangongZMJH = new LinkedHashMap<>();
        bangongZMJH.put("id", "11");
        bangongZMJH.put("item_name", "办公用电及照明");
        bangongZMJH.put("pid", "1");

        List<String> gonggongZM = new ArrayList<>(Arrays.asList(
                "1AA10-1", "1AA10-2", "1AA10-3", "1AA10-4", "1AA11-1", "1AA11-2", "2AA5-1", "2AA5-2", "4AA9-1", "4AA9-2",
                "3AA9-1", "3AA9-2"
        ));
        List<Map<String, Object>> gonggongZMSJ = hdbDao.getbycustom(gonggongZM, normalizedStartDate, normalizedEndDate);
        Map<String, Object> gonggongZMSJJH = new LinkedHashMap<>();
        gonggongZMSJJH.put("id", "12");
        gonggongZMSJJH.put("item_name", "公共区域照明");
        gonggongZMSJJH.put("pid", "1");

        List<String> fanguangZM = new ArrayList<>(Collections.singletonList("1AA6-2"));
        List<Map<String, Object>> fanguangZMSJ = hdbDao.getbycustom(fanguangZM, normalizedStartDate, normalizedEndDate);
        Map<String, Object> fanguangZMSJJH = new LinkedHashMap<>();
        fanguangZMSJJH.put("id", "13");
        fanguangZMSJJH.put("item_name", "泛光照明");
        fanguangZMSJJH.put("pid", "1");

        Map<String, Object> data11 = new LinkedHashMap<>();
        data11.put("id", "1");
        data11.put("pid", "-1");
        data11.put("item_name", "照明");
        List<Map<String, Object>> zhaomingAllMaps = new ArrayList<>();
        zhaomingAllMaps.add(xunhuanBLByCustom(bangongZM, bangongZMJH));
        zhaomingAllMaps.add(xunhuanBLByCustom(gonggongZMSJ, gonggongZMSJJH));
        zhaomingAllMaps.add(xunhuanBLByCustom(fanguangZMSJ, fanguangZMSJJH));
        data11.putAll(sumDateMaps(zhaomingAllMaps, dateKeys));
        maps.add(data11);
        maps.add(xunhuanBLByCustom(bangongZM, bangongZMJH));
        maps.add(xunhuanBLByCustom(gonggongZMSJ, gonggongZMSJJH));
        maps.add(xunhuanBLByCustom(fanguangZMSJ, fanguangZMSJJH));

        List<String> dianti = new ArrayList<>(Arrays.asList("1AA15-1", "1AA16-1", "1AA16-2", "1AA16-3", "1AA16-4", "2AA1-1"
                , "2AA1-2", "2AA1-3", "2AA1-4", "3AA17-4", "3AA17-5", "3AA18-1", "3AA18-2", "4AA2-1", "4AA2-2", "4AA2-3", "4AA2-4", "2AA7-1"));
        List<Map<String, Object>> diantimk = hdbDao.getbycustom(dianti, normalizedStartDate, normalizedEndDate);
        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("id", "21");
        data2.put("item_name", "电梯设备");
        data2.put("pid", "2");

        List<String> xiaofangFJ = new ArrayList<>(Arrays.asList("3AA15-1", "3AA9-3", "3AA10-1", "3AA10-2", "3AA10-3", "3AA11-1", "3AA11-2", "3AA11-3"
                , "3AA11-4", "3AA11-5", "3AA12-1", "3AA12-2", "3AA12-3", "3AA12-4", "3AA12-5", "3AA12-6",
                "4AA8-1", "4AA8-3", "4AA8-2", "4AA8-4", "4AA8-5", "4AA8-6", "4AA8-7", "4AA7-1"
                , "4AA7-2", "4AA7-3", "4AA7-4", "4AA7-5", "4AA4-1", "4AA6-1"));
        List<Map<String, Object>> xiaofangFJSJ = hdbDao.getbycustom(xiaofangFJ, normalizedStartDate, normalizedEndDate);
        Map<String, Object> xiaofangFJSJJH = new LinkedHashMap<>();
        xiaofangFJSJJH.put("id", "22");
        xiaofangFJSJJH.put("item_name", "消防风机设备");
        xiaofangFJSJJH.put("pid", "2");

        List<String> kongtiaoSB = new ArrayList<>(Arrays.asList("3AA7-2", "3AA7-3", "3AA8-1", "3AA8-2", "3AA8-3", "3AA14-2"
                , "4AA12-1", "4AA12-2", "4AA12-3", "4AA13-1", "4AA13-2", "4AA13-3", "4AA5-2"
                , "1AA8-2", "1AA8-3", "2AA9-3"
        ));
        List<Map<String, Object>> kongtiaoSBSJ = hdbDao.getbycustom(kongtiaoSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> kongtiaoSBSJJH = new LinkedHashMap<>();
        kongtiaoSBSJJH.put("id", "23");
        kongtiaoSBSJJH.put("item_name", "空调设备");
        kongtiaoSBSJJH.put("pid", "2");

        List<String> qianwubengSB = new ArrayList<>(Arrays.asList("1AA16-5", "1AA16-6", "2AA1-5", "2AA1-6", "4AA6-2"
                , "4AA6-3", "4AA6-4", "4AA6-5", "3AA13-2", "3AA13-3"));
        List<Map<String, Object>> qianwubengSBSJ = hdbDao.getbycustom(qianwubengSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> qianwubengSBSJJH = new LinkedHashMap<>();
        qianwubengSBSJJH.put("id", "24");
        qianwubengSBSJJH.put("item_name", "潜污泵设备");
        qianwubengSBSJJH.put("pid", "2");

        List<String> diandongCL = new ArrayList<>(Arrays.asList("4AA1-1", "4AA1-2", "4AA1-4", "3AA17-1", "3AA17-2", "3AA17-3"));
        List<Map<String, Object>> diandongCLSJ = hdbDao.getbycustom(diandongCL, normalizedStartDate, normalizedEndDate);
        Map<String, Object> diandongCLSJJH = new LinkedHashMap<>();
        diandongCLSJJH.put("id", "25");
        diandongCLSJJH.put("item_name", "电动窗帘");
        diandongCLSJJH.put("pid", "2");

        List<String> dianbanreSB = new ArrayList<>(Arrays.asList("4AA1-6", "4AA5-5", "4AA5-4", "3AA18-5"));
        List<Map<String, Object>> dianbanreSBSJ = hdbDao.getbycustom(dianbanreSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> dianbanreSBSJJH = new LinkedHashMap<>();
        dianbanreSBSJJH.put("id", "26");
        dianbanreSBSJJH.put("item_name", "电伴热");
        dianbanreSBSJJH.put("pid", "2");

        List<String> xichefangSB = new ArrayList<>(Collections.singletonList("1AA9-3"));
        List<Map<String, Object>> xichefangSBSJ = hdbDao.getbycustom(xichefangSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> xichefangSBSJJH = new LinkedHashMap<>();
        xichefangSBSJJH.put("id", "27");
        xichefangSBSJJH.put("item_name", "洗车房");
        xichefangSBSJJH.put("pid", "2");

        List<String> xiyuSB = new ArrayList<>(Collections.singletonList("1AA12-6"));
        List<Map<String, Object>> xiyuSBSJ = hdbDao.getbycustom(xiyuSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> xiyuSBSJJH = new LinkedHashMap<>();
        xiyuSBSJJH.put("id", "28");
        xiyuSBSJJH.put("item_name", "洗浴");
        xiyuSBSJJH.put("pid", "2");

        List<String> gongshuiSB = new ArrayList<>(Arrays.asList("1AA15-2", "1AA16-7", "2AA1-7", "2AA7-4", "2AA7-2"));
        List<Map<String, Object>> gongshuiSBSJ = hdbDao.getbycustom(gongshuiSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> gongshuiSBSJJH = new LinkedHashMap<>();
        gongshuiSBSJJH.put("id", "29");
        gongshuiSBSJJH.put("item_name", "供水");
        gongshuiSBSJJH.put("pid", "2");

        List<String> chongdianzSB = new ArrayList<>(Arrays.asList("3AA6-1", "3AA6-2", "3AA6-3", "3AA6-4", "3AA7-1"));
        List<Map<String, Object>> chongdianzSBSJ = hdbDao.getbycustom(chongdianzSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> chongdianzSBSJJH = new LinkedHashMap<>();
        chongdianzSBSJJH.put("id", "210");
        chongdianzSBSJJH.put("item_name", "充电桩");
        chongdianzSBSJJH.put("pid", "2");

        List<String> chufangSB = new ArrayList<>(Collections.singletonList("4AA11-1"));
        List<Map<String, Object>> chufangSBSJ = hdbDao.getbycustom(chufangSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> chufangSBSJJH = new LinkedHashMap<>();
        chufangSBSJJH.put("id", "211");
        chufangSBSJJH.put("item_name", "厨房");
        chufangSBSJJH.put("pid", "2");

        Map<String, Object> data12 = new LinkedHashMap<>();
        List<Map<String, Object>> shebeiAllMaps = new ArrayList<>();
        shebeiAllMaps.add(xunhuanBLByCustom(diantimk, data2));
        shebeiAllMaps.add(xunhuanBLByCustom(xiaofangFJSJ, xiaofangFJSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(kongtiaoSBSJ, kongtiaoSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(qianwubengSBSJ, qianwubengSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(diandongCLSJ, diandongCLSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(dianbanreSBSJ, dianbanreSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(xichefangSBSJ, xichefangSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(xiyuSBSJ, xiyuSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(gongshuiSBSJ, gongshuiSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(chongdianzSBSJ, chongdianzSBSJJH));
        shebeiAllMaps.add(xunhuanBLByCustom(chufangSBSJ, chufangSBSJJH));
        data12.put("id", "2");
        data12.put("pid", "-1");
        data12.put("item_name", "设备");
        data12.putAll(sumDateMaps(shebeiAllMaps, dateKeys));
        maps.add(data12);
        maps.add(xunhuanBLByCustom(diantimk, data2));
        maps.add(xunhuanBLByCustom(xiaofangFJSJ, xiaofangFJSJJH));
        maps.add(xunhuanBLByCustom(kongtiaoSBSJ, kongtiaoSBSJJH));
        maps.add(xunhuanBLByCustom(qianwubengSBSJ, qianwubengSBSJJH));
        maps.add(xunhuanBLByCustom(diandongCLSJ, diandongCLSJJH));
        maps.add(xunhuanBLByCustom(dianbanreSBSJ, dianbanreSBSJJH));
        maps.add(xunhuanBLByCustom(xichefangSBSJ, xichefangSBSJJH));
        maps.add(xunhuanBLByCustom(xiyuSBSJ, xiyuSBSJJH));
        maps.add(xunhuanBLByCustom(gongshuiSBSJ, gongshuiSBSJJH));
        maps.add(xunhuanBLByCustom(chongdianzSBSJ, chongdianzSBSJJH));
        maps.add(xunhuanBLByCustom(chufangSBSJ, chufangSBSJJH));

        List<String> danganshSB = new ArrayList<>(Arrays.asList("1AA11-5", "2AA5-5"));
        List<Map<String, Object>> danganshSB_SJ = hdbDao.getbycustom(danganshSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> danganshSB_SJ_JH = new LinkedHashMap<>();
        danganshSB_SJ_JH.put("id", "31");
        danganshSB_SJ_JH.put("item_name", "档案室");
        danganshSB_SJ_JH.put("pid", "3");

        List<String> wangluojfSB = new ArrayList<>(Arrays.asList("3AA14-1", "3AA15-2", "3AA15-3", "4AA4-2", "4AA4-3", "4AA4-4"));
        List<Map<String, Object>> wangluojfSB_SJ = hdbDao.getbycustom(wangluojfSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> wangluojfSB_SJ_JH = new LinkedHashMap<>();
        wangluojfSB_SJ_JH.put("id", "32");
        wangluojfSB_SJ_JH.put("item_name", "网络机房");
        wangluojfSB_SJ_JH.put("pid", "3");

        List<String> ruodianjfSB = new ArrayList<>(Arrays.asList("4AA3-1", "4AA3-2", "4AA3-3", "4AA3-4", "3AA16-1", "3AA16-2", "3AA16-3", "3AA16-4"));
        List<Map<String, Object>> ruodianjfSB_SJ = hdbDao.getbycustom(ruodianjfSB, normalizedStartDate, normalizedEndDate);
        Map<String, Object> ruodianjfSB_SJ_JH = new LinkedHashMap<>();
        ruodianjfSB_SJ_JH.put("id", "33");
        ruodianjfSB_SJ_JH.put("item_name", "弱电机房");
        ruodianjfSB_SJ_JH.put("pid", "3");

        List<Map<String, Object>> jifangAllMaps = new ArrayList<>();
        jifangAllMaps.add(xunhuanBLByCustom(danganshSB_SJ, danganshSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByCustom(wangluojfSB_SJ, wangluojfSB_SJ_JH));
        jifangAllMaps.add(xunhuanBLByCustom(ruodianjfSB_SJ, ruodianjfSB_SJ_JH));
        Map<String, Object> jifang = new LinkedHashMap<>();
        jifang.put("id", "3");
        jifang.put("pid", "-1");
        jifang.put("item_name", "机房");
        jifang.putAll(sumDateMaps(jifangAllMaps, dateKeys));
        maps.add(jifang);
        maps.add(xunhuanBLByCustom(danganshSB_SJ, danganshSB_SJ_JH));
        maps.add(xunhuanBLByCustom(wangluojfSB_SJ, wangluojfSB_SJ_JH));
        maps.add(xunhuanBLByCustom(ruodianjfSB_SJ, ruodianjfSB_SJ_JH));

        List<Map<String, Object>> allMaps = new ArrayList<>();
        allMaps.add(data11);
        allMaps.add(data12);
        allMaps.add(jifang);
        Map<String, Object> total = new LinkedHashMap<>();
        total.put("id", 99);
        total.put("pid", "-1");
        total.put("isTotal", true);
        total.put("item_name", "总计");
        total.putAll(sumDateMaps(allMaps, dateKeys));
        List<Map<String, Object>> jfpglist = hdbDao.getDLjfpgBycustom(normalizedStartDate, normalizedEndDate);
        for (Map<String, Object> map : jfpglist) {
            if (map != null) {
                total.putAll(map);
            }
        }
        maps.add(total);
        return maps;
    }

    @SuppressWarnings("unchecked")
    private List<String> fxMeters(Map<String, Object> item) {
        return (List<String>) item.get("meters");
    }

    private Map<String, Object> fxBaseRow(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", item.get("id"));
        row.put("pid", item.get("pid"));
        row.put("item_name", item.get("item_name"));
        return row;
    }

    /**
     * 构建二号配电室的能耗统计配置。
     */
    private List<Map<String, Object>> buildPds2Config() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(fx("201", "-1", "照明", "照明", "2"));
        list.add(fx("2011", "201", "办公照明", "照明", "2",
                "2-1AA6-1", "2-1AA7-1", "2-1AA8-1", "2-1AA11-6", "2-1AA11-7",
                "2-1AA12-1", "2-1AA12-2", "2-1AA12-3", "2-1AA12-4", "2-2AA10-1",
                "2-2AA9-1", "2-2AA8-1", "2-2AA5-6", "2-2AA5-7", "2-2AA4-1",
                "2-2AA4-2", "2-2AA4-3", "2-2AA4-4", "2-3AA9-1"
        ));
        list.add(fx("2012", "201", "公共照明", "照明", "2",
                "2-1AA9-1", "2-1AA9-2", "2-1AA11-1", "2-2AA5-1", "2-4AA6-1"
        ));
        list.add(fx("2013", "201", "景观照明", "照明", "2",
                "2-1AA10-2"
        ));

        list.add(fx("202", "-1", "设备", "设备", "2"));
        list.add(fx("2021", "202", "多功能厅", "设备", "2",
                "2-1AA9-3", "2-1AA9-4", "2-2AA7-1", "2-2AA7-4", "2-2AA7-5"
        ));
        list.add(fx("2022", "202", "擦窗机", "设备", "2",
                "2-1AA10-5", "2-2AA7-3"
        ));
        list.add(fx("2023", "202", "安检", "设备", "2",
                "2-1AA11-3", "2-2AA5-3"
        ));
        list.add(fx("2024", "202", "报告厅", "设备", "2",
                "2-1AA13-1", "2-2AA3-1", "2-1AA13-2", "2-2AA3-2",
                "2-1AA13-3", "2-2AA3-3", "2-1AA13-4", "2-2AA3-4"
        ));
        list.add(fx("2025", "202", "直流屏", "设备", "2",
                "2-1AA13-6", "2-2AA4-5"
        ));
        list.add(fx("2026", "202", "电梯设备", "设备", "2",
                "2-1AA14-2", "2-1AA15-1", "2-1AA15-2", "2-1AA15-3", "2-2AA2-2",
                "2-2AA1-1", "2-2AA1-2", "2-2AA1-3", "2-3AA14-3", "2-3AA14-4",
                "2-3AA14-5", "2-4AA1-3", "2-4AA1-4", "2-4AA1-5"
        ));
        list.add(fx("2027", "202", "潜污泵", "设备", "2",
                "2-1AA14-3", "2-2AA2-3", "2-3AA13-5", "2-3AA14-1", "2-3AA14-2",
                "2-4AA1-1", "2-4AA1-2", "2-4AA1-7"
        ));
        list.add(fx("2028", "202", "电气火灾柜电源", "设备", "2",
                "2-1AA15-5"
        ));
        list.add(fx("2029", "202", "咖啡吧", "设备", "2",
                "2-2AA7-8"
        ));
        list.add(fx("20210", "202", "充电桩", "设备", "2",
                "2-3AA6-1", "2-3AA6-2", "2-3AA6-3", "2-3AA6-4"
        ));
        list.add(fx("20211", "202", "厨房", "设备", "2",
                "2-3AA7-1", "2-3AA8-4", "2-4AA8-1", "2-4AA8-2", "2-4AA8-3",
                "2-1AA14-4", "2-2AA2-4"
        ));
        list.add(fx("20212", "202", "空调设备", "设备", "2",
                "2-3AA7-2", "2-3AA7-3", "2-3AA7-4", "2-3AA8-1", "2-3AA8-2",
                "2-3AA8-3", "2-4AA10-1", "2-4AA10-2", "2-4AA10-3",
                "2-4AA9-1", "2-4AA9-2", "2-4AA9-3"
        ));
        list.add(fx("20213", "202", "消防风机设备", "设备", "2",
                "2-3AA9-6", "2-3AA10-1", "2-3AA10-2", "2-3AA10-3", "2-3AA10-4",
                "2-3AA10-5", "2-3AA10-6", "2-3AA10-7", "2-3AA11-1", "2-3AA11-5",
                "2-3AA14-6", "2-4AA6-6", "2-4AA5-1", "2-4AA5-2", "2-4AA5-3",
                "2-4AA5-4", "2-4AA5-7", "2-4AA4-3", "2-4AA4-4", "2-4AA4-5",
                "2-4AA2-3", "2-4AA2-4", "2-4AA2-5", "2-4AA1-8"
        ));
        list.add(fx("20214", "202", "柴发机应急配电设备", "设备", "2",
                "2-3AA10-8", "2-3AA10-9", "2-4AA5-8", "2-4AA5-9"
        ));
        list.add(fx("20215", "202", "消防设备", "设备", "2",
                "2-3AA11-3", "2-3AA11-4"
        ));
        list.add(fx("20216", "202", "电动窗", "设备", "2",
                "2-3AA13-2", "2-3AA13-3", "2-3AA13-4", "2-4AA2-7", "2-4AA2-8", "2-4AA1-6"
        ));
        list.add(fx("20217", "202", "唯美便利店", "设备", "2",
                "2-4AA10-4"
        ));
        list.add(fx("20218", "202", "热融雪设备", "设备", "2",
                "2-4AA5-5"
        ));
        list.add(fx("20219", "202", "电伴热", "设备", "2",
                "2-4AA3-5"
        ));

        list.add(fx("203", "-1", "机房", "机房", "2"));
        list.add(fx("2031", "203", "配电室", "机房", "2",
                "2-3AA9-2", "2-4AA6-2", "2-4AA4-7"
        ));
        list.add(fx("2032", "203", "弱电机房", "机房", "2",
                "2-3AA12-1", "2-3AA12-2", "2-3AA12-3", "2-4AA3-1", "2-4AA3-2", "2-4AA3-3"
        ));

        return list;
    }



    /**
     * 构建六号配电室的能耗统计配置。
     */
    private List<Map<String, Object>> buildPds6Config() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(fx("601", "-1", "用户冷却水机房", "用户冷却水机房", "6"));
        list.add(fx("6011", "601", "冷却水机房", "用户冷却水机房", "6",
                "6-1AA6-1", "6-1AA7-1", "6-1AA8-1", "6-1AA8-2", "6-1AA8-5",
                "6-1AA9-1", "6-1AA10-1", "6-1AA10-2", "6-1AA10-4", "6-2AA1-1",
                "6-2AA1-2", "6-2AA1-4", "6-2AA4-1", "6-2AA4-2", "6-2AA4-5",
                "6-2AA5-1", "6-2AA6-1", "6-2AA7-1"
        ));

        return list;
    }


    private List<Map<String, Object>> getdayByConfig(String pds, String date) {
        return buildDayResult(getFenxiangConfigByPds(pds), date, false);
    }


    private List<Map<String, Object>> getmonthByConfig(String pds, String date) {
        return buildMonthResult(getFenxiangConfigByPds(pds), date, false);
    }


    private List<Map<String, Object>> getyearByConfig(String pds, String date) {
        return buildYearResult(getFenxiangConfigByPds(pds), date, false);
    }


    private List<Map<String, Object>> getcustomByConfig(String pds, String startDate, String endDate) {
        return buildCustomResult(getFenxiangConfigByPds(pds), startDate, endDate, false);
    }



    private static String normalizeDlfxjlPds(String pds) {
        if (pds == null || pds.trim().isEmpty() || "all".equalsIgnoreCase(pds)) {
            return "all";
        }
        if ("1".equals(pds)) {
            return "1";
        }
        if ("2".equals(pds)) {
            return "2";
        }
        if ("6".equals(pds)) {
            return "6";
        }
        return "all";
    }

    private Map<String, Object> fx(String id, String pid, String itemName, String topName, String pds, String... meters) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("pid", pid);
        item.put("item_name", itemName);
        item.put("top_name", topName);
        item.put("pds", pds);
        item.put("meters", meters == null ? Collections.emptyList() : Arrays.asList(meters));
        return item;
    }

    @SuppressWarnings("unchecked")


    private boolean hasMeters(Map<String, Object> item) {
        List<String> meters = fxMeters(item);
        return meters != null && !meters.isEmpty();
    }

    private List<String> collectAllMeters(List<Map<String, Object>> config) {
        List<String> meters = new ArrayList<>();
        for (Map<String, Object> item : config) {
            if (!hasMeters(item)) {
                continue;
            }
            meters.addAll(fxMeters(item));
        }
        return meters;
    }

    private boolean isTopRow(Map<String, Object> item) {
        return "-1".equals(String.valueOf(item.get("pid")));
    }


    private List<Map<String, Object>> getFenxiangConfigByPds(String pds) {
        if ("2".equals(pds)) {
            return buildPds2Config();
        }
        if ("6".equals(pds)) {
            return buildPds6Config();
        }
        return buildPds1Config();
    }

    private List<Map<String, Object>> getAllFenxiangConfig() {
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(buildPds1Config());
        all.addAll(buildPds2Config());
        all.addAll(buildPds6Config());
        return all;
    }

    private List<String> getAllTopNames() {
        return Arrays.asList("照明", "设备", "机房", "用户冷却水机房");
    }

    private String getAllTopId(String topName) {
        if ("照明".equals(topName)) {
            return "1";
        }
        if ("设备".equals(topName)) {
            return "2";
        }
        if ("机房".equals(topName)) {
            return "3";
        }
        if ("用户冷却水机房".equals(topName)) {
            return "4";
        }
        return "999";
    }

    /**
     * 构建一号配电室的能耗统计配置。
     */
    private List<Map<String, Object>> buildPds1Config() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(fx("101", "-1", "照明", "照明", "1"));
        list.add(fx("1011", "101", "办公用电及照明", "照明", "1",
                "1AA6-1", "1AA7-1", "1AA8-1", "1AA9-1", "1AA12-2", "1AA12-3",
                "1AA13-1", "1AA13-2", "1AA13-3", "1AA13-4", "1AA13-5",
                "1AA14-1", "1AA14-2", "1AA14-3", "1AA14-4", "1AA14-5", "1AA14-6", "1AA14-7", "1AA14-8",
                "2AA2-1", "2AA2-2", "2AA2-3", "2AA2-4", "2AA2-5", "2AA2-6", "2AA2-7", "2AA2-8",
                "2AA3-1", "2AA3-2", "2AA3-3", "2AA3-4", "2AA3-5",
                "2AA4-2", "2AA4-3", "2AA8-1", "2AA9-1", "2AA10-1", "2AA11-1"
        ));
        list.add(fx("1012", "101", "公共区域照明", "照明", "1",
                "1AA10-1", "1AA10-2", "1AA10-3", "1AA10-4", "1AA11-1", "1AA11-2",
                "2AA5-1", "2AA5-2", "4AA9-1", "4AA9-2", "3AA9-1", "3AA9-2"
        ));
        list.add(fx("1013", "101", "泛光照明", "照明", "1",
                "1AA6-2"
        ));

        list.add(fx("102", "-1", "设备", "设备", "1"));
        list.add(fx("1021", "102", "电梯设备", "设备", "1",
                "1AA15-1", "1AA16-1", "1AA16-2", "1AA16-3", "1AA16-4",
                "2AA1-1", "2AA1-2", "2AA1-3", "2AA1-4",
                "3AA17-4", "3AA17-5", "3AA18-1", "3AA18-2",
                "4AA2-1", "4AA2-2", "4AA2-3", "4AA2-4", "2AA7-1"
        ));
        list.add(fx("1022", "102", "消防风机设备", "设备", "1",
                "3AA15-1", "3AA9-3", "3AA10-1", "3AA10-2", "3AA10-3",
                "3AA11-1", "3AA11-2", "3AA11-3", "3AA11-4", "3AA11-5",
                "3AA12-1", "3AA12-2", "3AA12-3", "3AA12-4", "3AA12-5", "3AA12-6",
                "4AA8-1", "4AA8-2", "4AA8-3", "4AA8-4", "4AA8-5", "4AA8-6", "4AA8-7",
                "4AA7-1", "4AA7-2", "4AA7-3", "4AA7-4", "4AA7-5",
                "4AA4-1", "4AA6-1"
        ));
        list.add(fx("1023", "102", "空调设备", "设备", "1",
                "3AA7-2", "3AA7-3", "3AA8-1", "3AA8-2", "3AA8-3", "3AA14-2",
                "4AA12-1", "4AA12-2", "4AA12-3", "4AA13-1", "4AA13-2", "4AA13-3",
                "4AA5-2", "1AA8-2", "1AA8-3", "2AA9-3"
        ));
        list.add(fx("1024", "102", "潜污泵设备", "设备", "1",
                "1AA16-5", "1AA16-6", "2AA1-5", "2AA1-6",
                "4AA6-2", "4AA6-3", "4AA6-4", "4AA6-5",
                "3AA13-2", "3AA13-3"
        ));
        list.add(fx("1025", "102", "电动窗帘", "设备", "1",
                "4AA1-1", "4AA1-2", "4AA1-4", "3AA17-1", "3AA17-2", "3AA17-3"
        ));
        list.add(fx("1026", "102", "电伴热", "设备", "1",
                "4AA1-6", "4AA5-5", "4AA5-4", "3AA18-5"
        ));
        list.add(fx("1027", "102", "洗车房", "设备", "1",
                "1AA9-3"
        ));
        list.add(fx("1028", "102", "洗浴", "设备", "1",
                "1AA12-6"
        ));
        list.add(fx("1029", "102", "供水", "设备", "1",
                "1AA15-2", "1AA16-7", "2AA1-7", "2AA7-4", "2AA7-2"
        ));
        list.add(fx("10210", "102", "充电桩", "设备", "1",
                "3AA6-1", "3AA6-2", "3AA6-3", "3AA6-4", "3AA7-1"
        ));
        list.add(fx("10211", "102", "厨房", "设备", "1",
                "4AA11-1"
        ));

        list.add(fx("103", "-1", "机房", "机房", "1"));
        list.add(fx("1031", "103", "档案室", "机房", "1",
                "1AA11-5", "2AA5-5"
        ));
        list.add(fx("1032", "103", "网络机房", "机房", "1",
                "3AA14-1", "3AA15-2", "3AA15-3", "4AA4-2", "4AA4-3", "4AA4-4"
        ));
        list.add(fx("1033", "103", "弱电机房", "机房", "1",
                "4AA3-1", "4AA3-2", "4AA3-3", "4AA3-4",
                "3AA16-1", "3AA16-2", "3AA16-3", "3AA16-4"
        ));

        return list;
    }


    private List<Map<String, Object>> getdayAll(String date) {
        return buildDayResult(getAllFenxiangConfig(), date, true);
    }

    /**
     * 按照能耗树配置计算日统计结果并保留树节点顺序。
     */
    private List<Map<String, Object>> buildDayResult(List<Map<String, Object>> config, String date, boolean allMode) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<Map<String, Object>>> topChildRows = new LinkedHashMap<>();
        List<Map<String, Object>> topRows = new ArrayList<>();

        for (Map<String, Object> item : config) {
            if (!hasMeters(item)) {
                continue;
            }
            Map<String, Object> row = xunhuanBL(hdbDao.getbyday(fxMeters(item), date), fxBaseRow(item));
            String topName = String.valueOf(item.get("top_name"));
            if (allMode) {
                row.put("pid", getAllTopId(topName));
            }
            topChildRows.computeIfAbsent(topName, k -> new ArrayList<>()).add(row);
        }

        if (allMode) {
            for (String topName : getAllTopNames()) {
                Map<String, Object> topRow = new LinkedHashMap<>();
                topRow.put("id", getAllTopId(topName));
                topRow.put("pid", "-1");
                topRow.put("item_name", topName);
                topRow.putAll(sumHourMaps(topChildRows.getOrDefault(topName, Collections.emptyList())));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        } else {
            for (Map<String, Object> item : config) {
                if (!isTopRow(item)) {
                    continue;
                }
                String topName = String.valueOf(item.get("item_name"));
                Map<String, Object> topRow = fxBaseRow(item);
                topRow.putAll(sumHourMaps(topChildRows.getOrDefault(topName, Collections.emptyList())));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("id", 99);
        total.put("pid", "-1");
        total.put("isTotal", true);
        total.put("item_name", "总计");
        total.putAll(sumHourMaps(topRows));
        List<String> allMeters = collectAllMeters(config);
        List<Map<String, Object>> jfpgList = hdbDao.getDLjfpgBydateAndNames(allMeters, date);
        for (Map<String, Object> jfpg : jfpgList) {
            if (jfpg != null) {
                total.putAll(jfpg);
            }
        }

        result.add(total);

        return result;
    }

    private List<Map<String, Object>> getmonthAll(String date) {
        return buildMonthResult(getAllFenxiangConfig(), date, true);
    }

    /**
     * 按照能耗树配置计算月统计结果并保留树节点顺序。
     */
    private List<Map<String, Object>> buildMonthResult(List<Map<String, Object>> config, String date, boolean allMode) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<Map<String, Object>>> topChildRows = new LinkedHashMap<>();
        List<Map<String, Object>> topRows = new ArrayList<>();

        for (Map<String, Object> item : config) {
            if (!hasMeters(item)) {
                continue;
            }
            Map<String, Object> row = xunhuanBLByMonth(hdbDao.getbymonth(fxMeters(item), date), fxBaseRow(item));
            String topName = String.valueOf(item.get("top_name"));
            if (allMode) {
                row.put("pid", getAllTopId(topName));
            }
            topChildRows.computeIfAbsent(topName, k -> new ArrayList<>()).add(row);
        }

        if (allMode) {
            for (String topName : getAllTopNames()) {
                Map<String, Object> topRow = new LinkedHashMap<>();
                topRow.put("id", getAllTopId(topName));
                topRow.put("pid", "-1");
                topRow.put("item_name", topName);
                topRow.putAll(sumIndexedMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), "day_", 1, 31));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        } else {
            for (Map<String, Object> item : config) {
                if (!isTopRow(item)) {
                    continue;
                }
                String topName = String.valueOf(item.get("item_name"));
                Map<String, Object> topRow = fxBaseRow(item);
                topRow.putAll(sumIndexedMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), "day_", 1, 31));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("id", 99);
        total.put("pid", "-1");
        total.put("isTotal", true);
        total.put("item_name", "总计");
        total.putAll(sumIndexedMaps(topRows, "day_", 1, 31));
        List<String> allMeters = collectAllMeters(config);
        List<Map<String, Object>> jfpgList = hdbDao.getDLjfpgBymonthAndNames(allMeters, date);
        for (Map<String, Object> jfpg : jfpgList) {
            if (jfpg != null) {
                total.putAll(jfpg);
            }
        }

        result.add(total);

        return result;
    }

    private List<Map<String, Object>> getyearAll(String date) {
        return buildYearResult(getAllFenxiangConfig(), date, true);
    }

    /**
     * 按照能耗树配置计算年统计结果并保留树节点顺序。
     */
    private List<Map<String, Object>> buildYearResult(List<Map<String, Object>> config, String date, boolean allMode) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<Map<String, Object>>> topChildRows = new LinkedHashMap<>();
        List<Map<String, Object>> topRows = new ArrayList<>();

        for (Map<String, Object> item : config) {
            if (!hasMeters(item)) {
                continue;
            }
            Map<String, Object> row = xunhuanBLByYear(hdbDao.getbyyear(fxMeters(item), date), fxBaseRow(item));
            String topName = String.valueOf(item.get("top_name"));
            if (allMode) {
                row.put("pid", getAllTopId(topName));
            }
            topChildRows.computeIfAbsent(topName, k -> new ArrayList<>()).add(row);
        }

        if (allMode) {
            for (String topName : getAllTopNames()) {
                Map<String, Object> topRow = new LinkedHashMap<>();
                topRow.put("id", getAllTopId(topName));
                topRow.put("pid", "-1");
                topRow.put("item_name", topName);
                topRow.putAll(sumIndexedMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), "month_", 1, 12));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        } else {
            for (Map<String, Object> item : config) {
                if (!isTopRow(item)) {
                    continue;
                }
                String topName = String.valueOf(item.get("item_name"));
                Map<String, Object> topRow = fxBaseRow(item);
                topRow.putAll(sumIndexedMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), "month_", 1, 12));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("id", 99);
        total.put("pid", "-1");
        total.put("isTotal", true);
        total.put("item_name", "总计");
        total.putAll(sumIndexedMaps(topRows, "month_", 1, 12));
        List<String> allMeters = collectAllMeters(config);
        List<Map<String, Object>> jfpgList = hdbDao.getDLjfpgByyearAndNames(allMeters, date);
        for (Map<String, Object> jfpg : jfpgList) {
            if (jfpg != null) {
                total.putAll(jfpg);
            }
        }

        result.add(total);

        return result;
    }


    private List<Map<String, Object>> getcustomAll(String startDate, String endDate) {
        return buildCustomResult(getAllFenxiangConfig(), startDate, endDate, true);
    }

    /**
     * 按照能耗树配置计算自定义区间统计结果。
     */
    private List<Map<String, Object>> buildCustomResult(List<Map<String, Object>> config, String startDate, String endDate, boolean allMode) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        String normalizedStartDate = start.toString();
        String normalizedEndDate = end.toString();
        List<String> dateKeys = buildDateKeys(start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<Map<String, Object>>> topChildRows = new LinkedHashMap<>();
        List<Map<String, Object>> topRows = new ArrayList<>();

        for (Map<String, Object> item : config) {
            if (!hasMeters(item)) {
                continue;
            }
            Map<String, Object> row = xunhuanBLByCustom(
                    hdbDao.getbycustom(fxMeters(item), normalizedStartDate, normalizedEndDate),
                    fxBaseRow(item)
            );
            String topName = String.valueOf(item.get("top_name"));
            if (allMode) {
                row.put("pid", getAllTopId(topName));
            }
            topChildRows.computeIfAbsent(topName, k -> new ArrayList<>()).add(row);
        }

        if (allMode) {
            for (String topName : getAllTopNames()) {
                Map<String, Object> topRow = new LinkedHashMap<>();
                topRow.put("id", getAllTopId(topName));
                topRow.put("pid", "-1");
                topRow.put("item_name", topName);
                topRow.putAll(sumDateMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), dateKeys));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        } else {
            for (Map<String, Object> item : config) {
                if (!isTopRow(item)) {
                    continue;
                }
                String topName = String.valueOf(item.get("item_name"));
                Map<String, Object> topRow = fxBaseRow(item);
                topRow.putAll(sumDateMaps(topChildRows.getOrDefault(topName, Collections.emptyList()), dateKeys));
                result.add(topRow);
                topRows.add(topRow);
                result.addAll(topChildRows.getOrDefault(topName, Collections.emptyList()));
            }
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("id", 99);
        total.put("pid", "-1");
        total.put("isTotal", true);
        total.put("item_name", "总计");
        total.putAll(sumDateMaps(topRows, dateKeys));
        List<String> allMeters = collectAllMeters(config);
        List<Map<String, Object>> jfpgList = hdbDao.getDLjfpgBycustomAndNames(allMeters, normalizedStartDate, normalizedEndDate);
        for (Map<String, Object> jfpg : jfpgList) {
            if (jfpg != null) {
                total.putAll(jfpg);
            }
        }

        result.add(total);

        return result;
    }



    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        String text = value.toString();
        if (text.length() >= 10) {
            text = text.substring(0, 10);
        }
        return LocalDate.parse(text);
    }

    private static YearMonth toYearMonth(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof YearMonth) {
            return (YearMonth) value;
        }
        String text = value.toString();
        if (text.length() >= 7) {
            text = text.substring(0, 7);
        }
        return YearMonth.parse(text);
    }

    /**
     * 生成起止日期之间连续的日期键列表。
     */
    private static List<String> buildDateKeys(LocalDate start, LocalDate end) {
        List<String> keys = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            keys.add(current.toString());
            current = current.plusDays(1);
        }
        return keys;
    }

    /**
     * 按自定义日期键把查询结果整理为分项统计数据。
     */
    public static Map<String, Object> xunhuanBLByCustom(List<Map<String, Object>> SJ, Map<String, Object> data) {
        for (Map<String, Object> cms : SJ) {
            String dateKey = null;
            Double adspSum = null;
            for (Map.Entry<String, Object> entry : cms.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if ("hour".equals(key)) {
                    LocalDate localDate = toLocalDate(value);
                    if (localDate != null) {
                        dateKey = localDate.toString();
                    }
                } else if ("adsp_sum".equals(key)) {
                    adspSum = parseValueToDouble(value);
                }
            }
            if (dateKey != null && adspSum != null) {
                data.put(dateKey, String.valueOf(adspSum));
            }
        }
        return data;
    }

    /**
     * 汇总查询结果中指定编号范围内的分项数值。
     */
    public static Map<String, Object> sumIndexedMaps(List<Map<String, Object>> mapList, String prefix, int start, int end) {
        Map<String, Object> totalMap = new LinkedHashMap<>();
        for (int i = start; i <= end; i++) {
            totalMap.put(prefix + i, 0.0);
        }
        if (mapList == null || mapList.isEmpty()) {
            return totalMap;
        }
        for (Map<String, Object> dataMap : mapList) {
            if (dataMap == null) {
                continue;
            }
            for (int i = start; i <= end; i++) {
                String key = prefix + i;
                double total = parseValueToDouble(totalMap.get(key));
                double currentValue = parseValueToDouble(dataMap.get(key));
                totalMap.put(key, total + currentValue);
            }
        }
        return totalMap;
    }

    /**
     * 按日期键汇总多条统计映射。
     */
    public static Map<String, Object> sumDateMaps(List<Map<String, Object>> mapList, List<String> dateKeys) {
        Map<String, Object> totalMap = new LinkedHashMap<>();
        for (String dateKey : dateKeys) {
            totalMap.put(dateKey, 0.0);
        }
        if (mapList == null || mapList.isEmpty()) {
            return totalMap;
        }
        for (Map<String, Object> dataMap : mapList) {
            if (dataMap == null) {
                continue;
            }
            for (String dateKey : dateKeys) {
                double total = parseValueToDouble(totalMap.get(dateKey));
                double currentValue = parseValueToDouble(dataMap.get(dateKey));
                totalMap.put(dateKey, total + currentValue);
            }
        }
        return totalMap;
    }

    /**
     * 按小时字段汇总多条统计映射。
     */
    public static Map<String, Object> sumHourMaps(List<Map<String, Object>> mapList) {
        // 初始化结果Map，存储24小时的总和（默认值为0.0）
        Map<String, Object> totalMap = new HashMap<>(24);
        for (int i = 0; i <= 23; i++) {
            totalMap.put("hour_" + i, 0.0);
        }

        // 空集合直接返回初始结果
        if (mapList == null || mapList.isEmpty()) {
            return totalMap;
        }

        // 遍历每个Map，累加对应hour的值
        for (Map<String, Object> dataMap : mapList) {
            // 跳过空Map，避免空指针
            if (dataMap == null) {
                continue;
            }

            for (int hour = 0; hour <= 23; hour++) {
                String key = "hour_" + hour;
                // 获取当前Map中该小时的值
                Object valueObj = dataMap.get(key);
                double currentValue = parseValueToDouble(valueObj);

                // 累加至总Map
                double total = (Double) totalMap.get(key);
                totalMap.put(key, total + currentValue);
            }
        }

        return totalMap;
    }

    public static Map<String, Object> xunhuanBL(List<Map<String, Object>> SJ ,Map<String, Object> data){

        for (Map<String, Object> cms : SJ){
            Integer hour = null;  // 用Integer存储小时数
            Double adspSum = null;  // 用Double存储总和
            for (Map.Entry<String, Object> entry : cms.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();  // 修正3：先以Object接收
                if (key.equals("hour")) {
                    // 处理小时（转为Integer）
                    hour = (Integer) value;  // 数据库返回的hour是整数类型
                } else if (key.equals("adsp_sum")) {
                    // 处理总和（兼容多种数值类型）
                    adspSum = parseValueToDouble(value);
                }
            }
            // 确保hour和adspSum都有值后，存入data1
            if (hour != null && adspSum != null) {
                // 正确存储：key为"month_小时数"，value为总和的字符串形式
                data.put("hour_" + hour, String.valueOf(adspSum));
            }
        }
        return data;
    }
    /**
     * 将Object类型的值安全转换为double
     * 支持Number、String类型，其他类型或转换失败返回0.0
     */
    private static double parseValueToDouble(Object valueObj) {
        if (valueObj == null) {
            return 0.0;
        }

        if (valueObj instanceof Number) {
            // 处理Integer、Double、Long等数字类型
            return ((Number) valueObj).doubleValue();
        } else if (valueObj instanceof String) {
            // 处理字符串类型的数值
            try {
                return Double.parseDouble((String) valueObj);
            } catch (NumberFormatException e) {
                // 字符串无法转为数字时返回0.0
                return 0.0;
            }
        } else {
            // 不支持的类型返回0.0
            return 0.0;
        }
    }

    public Map<String,Object> getDBnewDate(String name) {
        Map<String,Object> dBnewDate = hdbDao.getDBnewDate(name);

        System.out.println(dBnewDate+"}}"+name);
        return dBnewDate;
    }

    public List<Map<String, Object>> getYthQH(String name, String startDate, String endDate) {
        if (endDate == null || endDate.trim().isEmpty()) {
            return hdbDao.getYthQHByPoint(name, startDate);
        }
        return hdbDao.getYthQH(name, startDate, endDate);
    }


    public void getSYshujuxiufu(String names) {
        List<Map<String, Object>> result  = hdbDao.getSYshujuxiufu(names);
        for (Map<String, Object> row : result) {
            // 从Map中获取数据
            String name = (String) row.get("name");
            LocalDateTime currentHourTime =  (LocalDateTime) row.get("current_hour_time");

            Double currentReading = (Double) row.get("current_reading");
            Double previousReading = (Double) row.get("previous_hour_reading");
            Double hourlyConsumption = (Double) row.get("hourly_consumption");

            // 打印调试信息
            System.out.println(name+"时间点: " + currentHourTime+"||"+"耗电量: " + hourlyConsumption);
            System.out.println("---");

            // 这里可以添加更新dianliang表的逻辑
            if (hourlyConsumption != null && hourlyConsumption<=10000) {
                hdbDao.setSYshuju(name, currentHourTime, hourlyConsumption);
            }
        }
    }

    public List<Map<String, Object>> getTowBYId42() {
        return hdbDao.getTowBYId42();
    }

    public List<Map<String, Object>> getTowBYId422() {
        return hdbDao.getTowBYId422();
    }

    public List<Map<String, Object>> getTowBYId51() {
        return hdbDao.getTowBYId51();
    }

    public List<Map<String, Object>> getTowBYId52() {
        return hdbDao.getTowBYId52();
    }

    public List<Map<String, Object>> getTowBYId61() {
        return hdbDao.getTowBYId61();
    }

    public List<Map<String, Object>> getTowBYId62() {
        return hdbDao.getTowBYId62();
    }

    public List<Map<String, Object>> getTowBYId71() {
        return hdbDao.getTowBYId71();
    }

    public List<Map<String, Object>> getTowBYId72() {
        return hdbDao.getTowBYId72();
    }

    public List<Map<String, Object>> getSixBYId64() {
        return hdbDao.getSixBYId64();
    }

    public List<Map<String, Object>> getSixBYId65() {
        return hdbDao.getSixBYId65();
    }

    public List<Double> geiTowhbxq(String name) {

        List<Double> hbxq = new ArrayList<>();
        Double zdl = hdbDao.getTwozdl();//最新电量
        if(zdl == null){
            zdl = 0.0;
        }
        String cmsliststr ="";
        if (name.equals("1号配电室")){
            cmsliststr ="'1AA1-1','2AA15-1','3AA1-1','4AA17-1'";
        }else if (name.equals("2号配电室")){
            cmsliststr ="'2-1AA1-1','2-2AA14-1','2-3AA1-1','2-4AA14-1'";
        }else if (name.equals("6号配电室")){
            cmsliststr ="'6-1AA1-1','6-2AA11-1'";
            zdl =hdbDao.getSixzdl();
        }
        Double byzxdl = hdbDao.getbyzxdl(cmsliststr);//本月1号电量
        if(byzxdl == null){
            byzxdl = 0.0;
        }
        Double syzxdl = hdbDao.getsyzxdl(cmsliststr);//上月1号电量
        if(syzxdl == null){
            syzxdl = 0.0;
        }
        Double bnzxdl = hdbDao.getbnzxdl(cmsliststr);//本年1号电量
        if(bnzxdl == null){
            bnzxdl = 0.0;
        }
        Double qnzxdl = hdbDao.getqnzxdl(cmsliststr);//去年最新
        if(qnzxdl == null){
            qnzxdl = 0.0;
        }
        if (byzxdl <1){
            hbxq.add(0.0);
        }else {
            String byzdl = String.format("%.2f", (zdl-byzxdl)/1000);
            hbxq.add(Double.parseDouble(byzdl));//本月总电量
        }
        if (syzxdl <1){
            hbxq.add(0.0);
            hbxq.add(0.0);
        }else {
            Double hbdl =(zdl-byzxdl)-(byzxdl-syzxdl);
//            String hbdls = String.format("%.2f", hbdl);
//            hbxq.add(Double.parseDouble(hbdls));//环比电量
            String format = String.format("%.2f", (byzxdl - syzxdl) / 1000);
            hbxq.add(Double.parseDouble(format));//上月电量
            String result = String.format("%.2f", hbdl/(byzxdl-syzxdl));
            hbxq.add(Double.parseDouble(result));//月环比
        }
        //年总电量
        hbxq.add(Double.parseDouble(String.format("%.2f", (zdl- bnzxdl)/1000 )));

        if (qnzxdl<1){
            hbxq.add(0.0);
            hbxq.add(0.0);
        }else {
            //环比电量
            Double nhbdl =(zdl-bnzxdl)-(bnzxdl-qnzxdl);
            String nhbdls = String.format("%.2f", nhbdl);
            String qnz = String.format("%.2f", (bnzxdl - qnzxdl) / 1000);
            hbxq.add(Double.parseDouble(qnz));//去年总电量
            String nresult = String.format("%.2f", nhbdl/(zdl- bnzxdl));
            hbxq.add(Double.parseDouble(nresult));//年环比
        }
        return hbxq;
    }
//public List<Double> geiTowhbxq(String name) {
//    List<Double> hbxq = new ArrayList<>();
//
//    // 获取最新电量
//    long startTime = System.currentTimeMillis();
//    Double zdl = hdbDao.getTwozdl(); // 最新电量
//    long duration = System.currentTimeMillis() - startTime;
//    System.out.println(String.format("SQL [getTwozdl] 执行耗时: %d ms", duration));
//    if (zdl == null) {
//        zdl = 0.0;
//    }
//
//    String cmsliststr = "";
//    if (name.equals("1号配电室")) {
//        cmsliststr = "'1AA1-1','2AA15-1','3AA1-1','4AA17-1'";
//    } else if (name.equals("2号配电室")) {
//        cmsliststr = "'2-1AA1-1','2-2AA14-1','2-3AA1-1','2-4AA14-1'";
//    } else if (name.equals("6号配电室")) {
//        cmsliststr = "'6-1AA1-1','6-2AA11-1'";
//
//        // 获取6号配电室的电量
//        startTime = System.currentTimeMillis();
//        zdl = hdbDao.getSixzdl();
//        duration = System.currentTimeMillis() - startTime;
//        System.out.println(String.format("SQL [getSixzdl] 执行耗时: %d ms", duration));
//    }
//
//    // 获取本月电量
//    startTime = System.currentTimeMillis();
//    Double byzxdl = hdbDao.getbyzxdl(cmsliststr); // 本月1号电量
//    duration = System.currentTimeMillis() - startTime;
//    System.out.println(String.format("SQL [getbyzxdl] 执行耗时: %d ms", duration));
//    if (byzxdl == null) {
//        byzxdl = 0.0;
//    }
//
//    // 获取上月电量
//    startTime = System.currentTimeMillis();
//    Double syzxdl = hdbDao.getsyzxdl(cmsliststr); // 上月1号电量
//    duration = System.currentTimeMillis() - startTime;
//    System.out.println(String.format("SQL [getsyzxdl] 执行耗时: %d ms", duration));
//    if (syzxdl == null) {
//        syzxdl = 0.0;
//    }
//
//    // 获取本年电量
//    startTime = System.currentTimeMillis();
//    Double bnzxdl = hdbDao.getbnzxdl(cmsliststr); // 本年1号电量
//    duration = System.currentTimeMillis() - startTime;
//    System.out.println(String.format("SQL [getbnzxdl] 执行耗时: %d ms", duration));
//    if (bnzxdl == null) {
//        bnzxdl = 0.0;
//    }
//
//    // 获取去年电量
//    startTime = System.currentTimeMillis();
//    Double qnzxdl = hdbDao.getqnzxdl(cmsliststr); // 去年最新电量
//    duration = System.currentTimeMillis() - startTime;
//    System.out.println(String.format("SQL [getqnzxdl] 执行耗时: %d ms", duration));
//    if (qnzxdl == null) {
//        qnzxdl = 0.0;
//    }
//
//    // 计算本月电量
//    if (byzxdl < 1) {
//        hbxq.add(0.0);
//    } else {
//        String byzdl = String.format("%.2f", (zdl - byzxdl) / 1000);
//        hbxq.add(Double.parseDouble(byzdl)); // 本月总电量
//    }
//
//    // 计算上月电量与月环比
//    if (syzxdl < 1) {
//        hbxq.add(0.0);
//        hbxq.add(0.0);
//    } else {
//        Double hbdl = (zdl - byzxdl) - (byzxdl - syzxdl);
//        String format = String.format("%.2f", (byzxdl - syzxdl) / 1000);
//        hbxq.add(Double.parseDouble(format)); // 上月电量
//        String result = String.format("%.2f", hbdl / (byzxdl - syzxdl));
//        hbxq.add(Double.parseDouble(result)); // 月环比
//    }
//
//    // 计算年总电量
//    hbxq.add(Double.parseDouble(String.format("%.2f", (zdl - bnzxdl) / 1000)));
//
//    // 计算去年总电量与年环比
//    if (qnzxdl < 1) {
//        hbxq.add(0.0);
//        hbxq.add(0.0);
//    } else {
//        Double nhbdl = (zdl - bnzxdl) - (bnzxdl - qnzxdl);
//        String nhbdls = String.format("%.2f", nhbdl);
//        String qnz = String.format("%.2f", (bnzxdl - qnzxdl) / 1000);
//        hbxq.add(Double.parseDouble(qnz)); // 去年总电量
//        String nresult = String.format("%.2f", nhbdl / (zdl - bnzxdl));
//        hbxq.add(Double.parseDouble(nresult)); // 年环比
//    }
//
//    return hbxq;
//}


    public List<Double> gethbxqDemo(Map<String, Object> paramMap) {

        List<Double> hbxq = new ArrayList<>();
        // 前端传递的年月参数（如"2025-07"）
        String yearMonth = (String) paramMap.get("yearMonth");
// 转为起始时间：2025-07-01 00:00:00
        String startDate = yearMonth + "-01 00:00:00";
// 转为结束时间：下一个月的第一天（如2025-08-01 00:00:00）
        LocalDate nextMonth = LocalDate.parse(yearMonth + "-01").plusMonths(1);
        String endDate = nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";

// 将参数放入map
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("cmsList", Arrays.asList("1AA1-1", "2AA15-1", "3AA1-1", "4AA17-1")); // 前端传递的name列表
        Double  cms = hdbDao.gethbxqDemo(paramMap);
        hbxq.add(cms);
        hbxq.add(1.11);

        return  hbxq;
    }
}
