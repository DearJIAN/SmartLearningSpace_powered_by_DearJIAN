package com.example.bookkeeping.controller;

import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper; // ✅ 必须引入这个
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AnalysisController {

    @Autowired
    private BillService billService;

    // ✅ 恢复 ObjectMapper，我们需要它来确保数据安全转换
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/analysis")
    public String analysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 默认给空对象的 JSON 字符串，防止前端报错
        String dataJson = "{}";
        String enhancedDataJson = "{}";

        try {
            // 1. 获取数据 (Map结构)
            Map<String, Object> data = billService.getAnalysisData(user.getId(), startDate, endDate);
            
            Map<String, Object> enhancedData = new HashMap<>();
            enhancedData.put("top5", billService.getExpenditureTop5(user.getId(), startDate, endDate));
            enhancedData.put("yoy", billService.getYoYAnalysis(user.getId(), startDate, endDate));
            enhancedData.put("freq", billService.getConsumptionFrequency(user.getId(), startDate, endDate));
            enhancedData.put("weekly", billService.getWeeklyConsumption(user.getId(), startDate, endDate));
            enhancedData.put("budgetTrend", billService.getBudgetBurnTrend(user.getId(), startDate, endDate));
            enhancedData.put("budgetRisk", billService.getBudgetRisk(user.getId(), startDate, endDate));
            enhancedData.put("health", billService.getFinancialHealth(user.getId(), startDate, endDate));

            // 2. ✅ 手动转成 JSON 字符串
            // 这样如果出错，我们在控制台就能看到，而不是等到渲染页面时才崩
            dataJson = objectMapper.writeValueAsString(data);
            enhancedDataJson = objectMapper.writeValueAsString(enhancedData);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 统计数据序列化失败: " + e.getMessage());
        }

        // 3. 传入 JSON 字符串
        model.addAttribute("dataJson", dataJson);
        model.addAttribute("enhancedDataJson", enhancedDataJson);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "analysis";
    }

    @GetMapping("/treemap")
    public String treemap(@RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        // 这里 TreeData 比较简单，直接传对象通常没问题，或者也可以照搬上面的 convert
        model.addAttribute("treeData", billService.getTreeMapData(user.getId(), startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "treemap";
    }
}