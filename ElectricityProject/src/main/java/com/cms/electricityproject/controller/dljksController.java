package com.cms.electricityproject.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
//@RequestMapping("/ep")
@CrossOrigin(origins = "*", maxAge = 3600)
/**
 * 处理电力监控页面的模板路由和动态页面加载。
 */
public class dljksController {
    @GetMapping("/index")
    /**
     * 返回电力监控系统主页面。
     */
    public String showIndexPage() {
        System.out.println("index被访问了");
        return "dljk/dljks";
    }
    // 通配符路径映射，处理所有/dlik/xxx请求
    @GetMapping("/dlik/{pageName}")
    /**
     * 校验页面名后返回供 iframe 加载的监控模板。
     */
    public String iframePage(@PathVariable String pageName) {
        return "dlik/" + pageName; // 动态返回对应模板
    }
    @GetMapping("/login")
    /**
     * 将根路径重定向到电力监控主页面。
     */
    public String redirectToIndex() {

        System.out.println("index被重定向访问了");
        return "redirect:/dljk/dljks.html";
    }
    @GetMapping("/dynamicPage")
    /**
     * 根据请求参数返回允许访问的动态页面。
     */
    public String getDynamicPage(@RequestParam("pageName") String pageName) {
        // 这里可以添加一些对 pageName 的验证逻辑，防止非法访问
        return "dljk/"+pageName;
    }
}
