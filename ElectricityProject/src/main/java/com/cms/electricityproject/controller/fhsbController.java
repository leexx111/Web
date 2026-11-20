package com.cms.electricityproject.controller;

import com.cms.electricityproject.config.BacnetTool;
import com.cms.electricityproject.config.ScheduledTasks;
import com.cms.electricityproject.server.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/fhsb")
@CrossOrigin("*")
/**
 * 提供设备实时数据读取及日/月/年/自定义统计接口。
 */
public class fhsbController {
    @Autowired
    private UserService userService;


    @Autowired
    private BacnetTool bacnetTool;
    @Autowired
    private ScheduledTasks scheduledTasks;
    /**
     *  查询网关中的数据
     * @return
     */

    @RequestMapping("/fhsbData")
    /**
     * 并行读取指定网关下的设备实时数据并合并返回。
     */
    public Map<Object, Object> fhsbData(@RequestParam("wangguanhao") Integer wangguanhao,
                                                @RequestParam("shebeihao") List<Integer> shebeihao) throws ExecutionException, InterruptedException {
        // 调用异步方法，获取Future对象
        Future<Map<Object, Object>> futureResult = fetchDataAsync(wangguanhao, shebeihao);

        // 等待异步任务完成并获取结果（会阻塞当前线程，直到异步任务结束）
        // 注意：这里的阻塞仅针对当前请求线程，不影响异步任务本身的执行线程
        Map<Object, Object>  resultMap = futureResult.get();
        System.out.println("请求网关"+wangguanhao);

        Map<Object, Object> cms = new LinkedHashMap<>();
        //获取两个传递的时间数据
        for (Map.Entry<Object, Object> entry : resultMap.entrySet()) {
//            System.out.println(entry.getKey()+":"+entry.getValue());
            cms.put(entry.getKey()+"", entry.getValue()+"");
        }
        return cms;
    }


    // 异步执行实际业务逻辑，返回Future<Map>
    @Async("asyncTaskExecutor") // 指定线程池
    /**
     * 异步执行一组设备的数据读取并返回 Future 结果。
     */
    public Future<Map<Object, Object>> fetchDataAsync(Integer wangguanhao, List<Integer> shebeihao) {
        try {
            // 模拟耗时操作（比如调用bacnetTool.qiandanduqu）
            Map<Object, Object> resultMap = bacnetTool.qianduanduqu(wangguanhao, shebeihao);
            return new AsyncResult<>(resultMap); // 用AsyncResult包装结果
        } catch (Exception e) {
            // 异常处理
            return new AsyncResult<>(null);
        }
    }


    /**
     * 查询信息
     */
    @RequestMapping("/getdemo")
    public Map<Object,Object> getdemo(){
        LinkedHashMap<Object, Object> stringObjectLinkedHashMap = new LinkedHashMap<>();
        stringObjectLinkedHashMap.put("1AA12-1",12.3);
        stringObjectLinkedHashMap.put("1AA12-2",12.2);
        stringObjectLinkedHashMap.put("1AA12-3",12.1);
        return stringObjectLinkedHashMap;
    }

    @RequestMapping("/SYshujuxiufu")
    /**
     * 接收历史数据修复请求并转交后台处理。
     */
    public String getSYshujuxiufu(@RequestParam("names") String names){
        scheduledTasks.SYshujuxiufu(names);
        return "";
    }

    @RequestMapping("/getday")
    /**
     * 查询指定配电室和日期的日统计数据。
     */
    public List<Map<String,Object >> getday(@RequestParam("pds") String pds,
                                            @RequestParam("date") String date     ){
        return userService.getday(pds,date);
    }
    @RequestMapping("/getmonth")
    /**
     * 查询指定配电室和月份的月统计数据。
     */
    public List<Map<String,Object >> getmonth(@RequestParam("pds") String pds,
                                            @RequestParam("date") String date     ){
        return userService.getmonth(pds,date);
    }
    @RequestMapping("/getyear")
    /**
     * 查询指定配电室和年份的年统计数据。
     */
    public List<Map<String,Object >> getyear(@RequestParam("pds") String pds,
                                              @RequestParam("date") String date     ){
        return userService.getyear(pds,date);
    }

    @RequestMapping("/getcustom")
    /**
     * 查询指定配电室和时间范围的自定义统计数据。
     */
    public List<Map<String, Object>> getcustom(@RequestParam("pds") String pds,
                                               @RequestParam("startDate") String startDate,
                                               @RequestParam("endDate") String endDate) {
        return userService.getcustom(pds, startDate, endDate);
    }
}
