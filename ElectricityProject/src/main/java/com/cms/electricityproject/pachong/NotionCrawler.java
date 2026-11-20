package com.cms.electricityproject.pachong;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * 实验性网页抓取流程，负责启动浏览器并提取页面内容。
 */
public class NotionCrawler {
    public static void main(String[] args) {
        String url = "https://wanping.notion.site/2025-19eb379cee5280bda533f109a8e9ffe4#19eb379cee5280279c3dd9ef57b66321";
        String outputDir = "notion_downloads";
        
        try {
            // 初始化WebDriver
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            // 无头模式（可选，如需可视化可注释）
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            WebDriver driver = new ChromeDriver(options);
            
            // 访问页面
            driver.get(url);
            System.out.println("正在加载页面...");
            
            // 等待页面加载完成（根据Notion加载速度调整）
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("notion-selectable")));
            
            // 滚动页面以加载所有内容（Notion内容可能动态加载）
            scrollToBottom(driver);
            Thread.sleep(2000); // 等待滚动后内容加载
            
            // 提取页面标题
            String title = driver.findElement(By.tagName("title")).getText().trim();
            title = title.replaceAll("[\\/:*?\"<>|]", "_"); // 清理文件名非法字符
            
            // 提取页面内容（Notion内容主要在notion-content类中）
            WebElement contentElement = driver.findElement(By.className("notion-content"));
            String content = contentElement.getAttribute("outerHTML");
            
            // 保存为HTML文件
            saveToFile(content, outputDir, title + ".html");
            System.out.println("下载完成：" + title + ".html");
            
        } catch (Exception e) {
            System.err.println("爬取失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 滚动页面到底部以加载所有内容
    private static void scrollToBottom(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int scrollHeight = (Integer) js.executeScript("return document.body.scrollHeight");
        
        for (int i = 0; i < 10; i++) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(1000);
                int newScrollHeight = (Integer) js.executeScript("return document.body.scrollHeight");
                if (newScrollHeight == scrollHeight) {
                    break; // 滚动到底部，内容不再加载
                }
                scrollHeight = newScrollHeight;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 保存内容到文件
    private static void saveToFile(String content, String dir, String filename) throws IOException {
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        File file = new File(directory, filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}