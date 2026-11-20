package com.cms.electricityproject.controller;

import com.cms.electricityproject.domain.Heidb;
import com.cms.electricityproject.domain.Ytmem;
import com.cms.electricityproject.server.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hdb")
@CrossOrigin("*")
/**
 * 提供电气数据、能耗统计和报警管理 REST 接口。
 */
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Heidb> getAllUsers() {
        System.out.println("执行了访问");
        return userService.getAllUsers();
    }


    @RequestMapping("/getNow")
    public List<Heidb> getNow() {
        return userService.getNow();
    }

    /**
     * 查询4号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId41")
    public List<Map<String,Object>> getBYId41(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getById41();
    }
    /**
     * 查询4号母线2段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId42")
    public List<Map<String,Object>> getBYId42(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId42();
    }
    /**
     * 查询5号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId51")
    public List<Map<String,Object>> getBYId51(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId51();
    }
 /**
     * 查询5号母线2段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId52")
    public List<Map<String,Object>> getBYId52(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId52();
    }
    /**
     * 查询6号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId61")
    public List<Map<String,Object>> getBYId61(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId61();
    }/**
     * 查询6号母线2段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId62")
    public List<Map<String,Object>> getBYId62(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId62();
    }/**
     * 查询7号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId71")
    public List<Map<String,Object>> getBYId71(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId71();
    }
/**
     * 查询7号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getBYId72")
    public List<Map<String,Object>> getBYId72(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getBYId72();
    }
    /**
     * 查询4号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId42")
    public List<Map<String,Object>> getTowBYId42(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId42();
    }
    /**
     * 查询4号母线2段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId422")
    public List<Map<String,Object>> getTowBYId422(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId422();
    }
    /**
     * 查询5号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId51")
    public List<Map<String,Object>> getTowBYId51(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId51();
    }
    /**
     * 查询5号母线2段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId52")
    public List<Map<String,Object>> getTowBYId52(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId52();
    }
    /**
     * 查询6号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId61")
    public List<Map<String,Object>> getTowBYId61(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId61();
    }
    /**
     * 查询6号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId62")
    public List<Map<String,Object>> getTowBYId62(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId62();
    }
    /**
     * 查询7号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId71")
    public List<Map<String,Object>> getTowBYId71(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId71();
    }
    /**
     * 查询7号母线1段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getTowBYId72")
    public List<Map<String,Object>> getTowBYId72(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getTowBYId72();
    }
    /**
     * 查询6号母线4段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getSixBYId64")
    public List<Map<String,Object>> getSixBYId64(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getSixBYId64();
    }
    /**
     * 查询6号母线4段线路图显示信息
     *
     * @return
     */
    @RequestMapping("getSixBYId65")
    public List<Map<String,Object>> getSixBYId65(){
        //网关延时问题，暂时从数据库获取数据
        return userService.getSixBYId65();
    }

    /**
     *
     *  查询所有的电表数据
     * @return
     */
    @RequestMapping("/getYtmemAll")
    public List<Ytmem> getYtmemAll() {
        return userService.getYtmemAll();
    }
    /**
     *  查询上月的平均负荷
     * @return
     */
    @RequestMapping("/getByLastMonthMeanW")
    public List<Double> getByLastMonthMeanW(@RequestParam("name") String name) {
        return userService.getByLastMonthMeanW(name);
    }
    /**
     *  查询本日和上日的电力负荷
     * @return
     */
    @RequestMapping("/getBydlfh")
    public List<Map<String, Object>> getBydlfh(@RequestParam("date") String date,
                                               @RequestParam("name") String name) {
        // 模拟生成两组各有 24 个 double 类型数据的数组
        System.out.println(date+"获取到的日期");
        // 填充第一组数据
        List<Map<String, Object>> maps = new ArrayList<>();

        maps =userService.getByjrfh(date,name);
        return maps;
    }

    /**
     *  查询本月的平均负荷
     * @return
     */
    @RequestMapping("/getByThisMonthMeanW")
    public List<Double> getByThisMonthMeanW() {
        return userService.getByThisMonthMeanW();
//        return userService.getByThisMonthMeanW();
    }
    /**
     *  查询本月的平均负荷
     * @return
     */
    @RequestMapping("/getByLastMonthMeanEQ")
    public List<Double> getByLastMonthMeanEQ(@RequestParam("name") String name) {
        return userService.getByLastMonthMeanEQ(name);
    }

    /**
     * 查询负荷详情  【总负荷，每日最高负荷，本月最高负荷，本年最高负荷，本年最低负荷】 /getfhxq
     * @return
     */
    @RequestMapping("/getfhxq")
    public List<String> getfhxq(@RequestParam("name") String name) {
        return userService.getfhxq(name);
    }

    /**
     * 查询环比详情 【月总电量，环比电量，月环比，年总电量，环比电量，年环比】 /gethbxq
     *
     * @return
     */
    @RequestMapping("/gethbxq")
    public List<Double> gethbxq(@RequestParam("name") String name) {
        if (name.equals("2号配电室")){
            return userService.geiTowhbxq(name);
        }else if (name.equals("6号配电室")){
            return userService.geiTowhbxq(name);
        }
        return userService.gethbxq();
    }
    /**
     * 查询环比详情demo 【月总电量，环比电量，月环比，年总电量，环比电量，年环比】 /gethbxq
     *
     * @return
     */
    @RequestMapping("/gethbxqDemo")
    public List<Double> gethbxqDemo(Map<String, Object> paramMap) {

        return userService.gethbxqDemo(paramMap);
    }


    /**
     * 查询配电设备监测运行工况
     *
     * @return
     */
    @RequestMapping("/sbjcyxgk")
    public Map<String, Object> getsbjcTableData(@RequestParam("id") String id) {
//        System.out.println("设备的id"+id);
        return userService.getsbjcTableData(id);
    }

    /**
     * 查询配电设备监测运行工况
     *
     * @return
     */
    @RequestMapping("/dnnjcs")
    public List<List<Object>>  getdnnjcs(@RequestParam("id") String id) {
//        System.out.println("查询代码运行了");
        return  userService.getdnnjcs(id);
    }
    /**
     * 查询配电设备监测运行工况
     *
     * @return
     */
    @RequestMapping("/getcsjz")
    public List<List<Object>>  getcsjz(@RequestParam("id") String id) {
//        System.out.println("查询代码运行了");
        return  userService.getcsjz(id);
    }
    /**
     * 查询配电设备监测运行工况
     *
     * @return
     */
    @RequestMapping("/dnnjcs2")
    public Map<String, Object> dnnjcs2(String id) {
        return userService.dnnjcs2(id);
    }
    /**
     * 查询配电设备监测运行工况
     *
     * @return
     */
    @RequestMapping("/dnnjcs3")
    public Map<String, Object> dnnjcs3(String id) {
        return userService.dnnjcs3(id);
    }

    /**
     * 查询电量统计分析
     *
     * @return
     */
    @RequestMapping("/getdltjcx")
    public Map<String,List<String>> getdltjcx(@RequestParam("cmsname")List<String> cmsname,@RequestParam("nyr")String nyr,@RequestParam("datatest")String datatest) {
        //返回数据： 字段数据， 有功数据，电费数据
        // 模拟三个 List 集合
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        List<String> list3 = new ArrayList<String>();
        List<String> list4 = new ArrayList<String>();
        List<String> list5 = new ArrayList<String>();
        //测量位置为集合
        if (cmsname == null || cmsname.isEmpty()) {
            Map<String, List<String>> emptyResult = new HashMap<>();
            emptyResult.put("list1", list1);
            emptyResult.put("list2", list2);
            emptyResult.put("list3", list3);
            emptyResult.put("list4", list4);
            emptyResult.put("list5", list5);
            return emptyResult;
        }

//                DFclwz.substring(0,DFclwz.indexOf(":"));//1AA6-1
        if (nyr.equals("year")){
            for (int i = 1; i <= 12; i++) {
                list1.add(String.valueOf(i));
            }
            //获取每个月有功电度值
            list2 =userService.getdltjcxyearList2(cmsname,datatest,2);  //2025
            //电费
            list3 =userService.getdltjcxyearList2(cmsname,datatest,3);  //2025
            list4 =userService.getdltjcxyearList2(cmsname,datatest,4);  //2025
            list5 =userService.getdltjcxyearList2(cmsname,datatest,5);  //2025
            //获取每个月电费值
        }else if (nyr.equals("month")){
            for (int i = 1; i <= 31; i++) {
                list1.add(String.valueOf(i));
            }
            //获取月份内每天的数据
            list2 =userService.getdltjcxmonthList2(cmsname,datatest,2);
            //获取电费
            list3 =userService.getdltjcxmonthList2(cmsname,datatest,3);
            list4 =userService.getdltjcxmonthList2(cmsname,datatest,4);
            list5 =userService.getdltjcxmonthList2(cmsname,datatest,5);


        }else if (nyr.equals("day")){
            for (int i = 0; i <= 23; i++) {
                list1.add(String.valueOf(i));
            }
            //获取每小时有功电度值
            list2 =userService.getdltjcxdayList2(cmsname,datatest,2);
            //获取每小时电费
            list3 =userService.getdltjcxdayList2(cmsname,datatest,3);
            //获取尖峰平谷的值和电费
            list4 =userService.getdltjcxdayList2(cmsname,datatest,4);//电量
            list5 =userService.getdltjcxdayList2(cmsname,datatest,5);//电量
        }
        // 使用 Map 封装三个列表，键名自定义（如 list1, list2, list3）
        Map<String, List<String>> result = new HashMap<>();
        result.put("list1", list1);
        result.put("list2", list2);
        result.put("list3", list3);
        result.put("list4", list4);
        result.put("list5", list5);
        return result;
    }

    /**
     * 查询电能质量监测
     * @return
     */
    @RequestMapping("/getdnzljcsj")
    public List<Map<Object,Object>> getdnzljcsj(@RequestParam("wz")String wz,@RequestParam("ks")String ks,@RequestParam("js")String js,@RequestParam("lx")String lx) {
        //测量位置，开始时间，结束时间，类型（需要做区分）

//        String[] parts = wz.split(":", 2);
//        wz = parts[0];
        if (lx.equals("V")){//电压
            return userService.getdnzljcsj(wz,ks,js,lx);
        }else if (lx.equals("A")){
            return userService.getdnzldlsj(wz,ks,js,lx);
        }else if (lx.equals("kvar")){
            return userService.getdnzlygsj("W","Wa","Wb","Wc",wz,ks,js,lx);
        }else if (lx.equals("Kvar")){
            return userService.getdnzlygsj("Q","Qa","Qb","Qc",wz,ks,js,lx);
        }else if (lx.equals("kva")){
            return userService.getdnzzszgl("S",wz,ks,js,lx);
        }
        //有功电量 kwh 无功电量 kvarh
        return null;
    }


    /**
     * 查询报警信息
     */
    @RequestMapping("/getalarm")
    public Map<String, Object> getalarm(@RequestParam("alarmId") String alarmId) {
        return userService.getAlarmById(alarmId);
    }

    /**
     * 查最新未确认报警
     */
    @RequestMapping("/getLatestUnconfirmedAlarm")
    public Map<String, Object> getLatestUnconfirmedAlarm() {
        return userService.getLatestUnconfirmedAlarm();
    }

    /**
     * 修改报警信息
     */
    @RequestMapping("/updatealarmById")
    public void updatealarmById(@RequestParam("alarmId")String alarmId){

        userService.updatealarmById(alarmId);
    }
    /**
     * 查询报警信息
     */
    @RequestMapping("/getALLalarm")
    public Map<String, Object> getALLalarm(
                             @RequestParam(required = false) String alarmLevel1,
                             @RequestParam(required = false) String alarmLevel2,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String device,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate
    ){
        Map<String,Object> map = new HashMap<>();
        List<Map<String,String>> alarmList = new ArrayList<>();
        alarmList=userService.getALLalarm(alarmLevel1,alarmLevel2,status,device,startDate,endDate);
        map.put("code",200);
        map.put("message","查询成功");
        map.put("data",alarmList);
        System.out.println("报警记录查询完成，记录数=" + alarmList.size());
        System.out.println(startDate+"查询条件");
        return map;
    }

    @RequestMapping("/getnhtj")
    public List<Map<String,String>> getnhtj(@RequestParam("cmsname") List<String> cmsname,
                                            @RequestParam("startDate") String startDate,
                                            @RequestParam("endDate") String endDate,
                                            @RequestParam(value = "pdsMapJson", required = false) String pdsMapJson) {

        return userService.getnhtj(cmsname,startDate,endDate,pdsMapJson);
    }

    @RequestMapping("/getrtj")
    public Map<String,Object> getrtj(@RequestParam("clwz")String clwz,@RequestParam("clsj")String clsj){
//        System.out.println("访问日报接口");
        return userService.getrtj(clwz,clsj);
    }
//        int cmsI =0;
    @RequestMapping("/getytj")
    public Map<String,Object> getytj(@RequestParam("clwz")String clwz,@RequestParam("clsj")String clsj){
//        System.out.println("访问月报接口" +cmsI);
//        cmsI+=1;
        return userService.getytj(clwz,clsj);
    }
    @RequestMapping("/getntj")
    public Map<String,Object> getntj(@RequestParam("clwz")String clwz,@RequestParam("clsj")String clsj){
//        System.out.println(clwz+"|"+clsj+"访问年报接口"+cmsI);
//        cmsI+=1;
        return userService.getntj(clwz,clsj);
    }

    @PostMapping
    public Heidb saveUser(@RequestBody Heidb user) {
        return userService.saveUser(user);
    }

    @GetMapping("/{id}")
    public Heidb getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }

    @RequestMapping("/hello")
    public String toHello(){
        return "hello";
    }


    // 广播消息给所有WebSocket客户端
    @GetMapping("/broadcast")
    public String broadcast() {
//        userService.broadcastMessage("CMS的消息发送-");
        Map<String, Object> cms = new HashMap<>();
        cms.put("message","跳闸报警");
        cms.put("name","1AA6-1");
        userService.broadcastMessage(cms);
        return "模拟消息已广播";
    }

    /***
     * 负荷对比分析接口
     * @param kssj 开始时间
     * @param sjsj 结束时间
     * @param clwz 名字
     * @param dbrq 对比时间
     * @return
     */
    @RequestMapping("getfhdbfx")
    public Map<String,Object> getfhdb(@RequestParam("kssj")String kssj,@RequestParam("sjsj")String sjsj,@RequestParam("clwz")String clwz,@RequestParam("dbrq")String dbrq){

        String[] parts = clwz.split(":", 2);
        clwz = parts[0];
        System.out.println("切割后的clwz:"+clwz);
        return userService.getfhdbfx(kssj,sjsj,dbrq,clwz);
    }



    @RequestMapping("getfhsbfx")
    public Map<String,Object> getfhsbfx(@RequestParam("dysj")String dysj,@RequestParam("desj")String desj,@RequestParam("clwz")String clwz){
        String[] parts = clwz.split(":", 2);
        clwz = parts[0];
        System.out.println("访问了-------------"+ clwz);
        return userService.getfhsbfx(dysj,desj,clwz);
    }

    //查询滤波器的数据
    @RequestMapping("getlbqByName")
    public Map<String,Object> getlbqByName(@RequestParam("name")String name){
        return userService.getlbqByName(name);
    }

    @RequestMapping("getbyqxq")
    public List<Map<String,Object>> getbyqxq(@RequestParam("name") String name){
        return userService.getbyqxq(name);
    }

    @RequestMapping("getbyqfz")
    public String getbyqfz(@RequestParam("id")Integer id,@RequestParam("name")String name){
        //暂时为1号配
        List<Map<String, Object>> getbyqxq = userService.getbyqxq(name);
        Map<String, Object> stringObjectMap = getbyqxq.get(id-1);

        return stringObjectMap.get("fuzai").toString();
    }

    @RequestMapping("getrecentalarm")
    public List<Map<String,Object>> getall(){
        return userService.getrecentalarm();
    }

    //查询指定电表的最新数据
    @RequestMapping("getDBnewDate")
    public Map<String,Object>  getDBnewDate(@RequestParam("name")String name){
        return userService.getDBnewDate(name);
    }

    //清华查询接口
    @RequestMapping("/getYthQH")
    public List<Map<String,Object>> getYthQH(@RequestParam("name") String name,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam(value = "endDate", required = false) String endDate){
        return userService.getYthQH(name, startDate, endDate);
    }


}
