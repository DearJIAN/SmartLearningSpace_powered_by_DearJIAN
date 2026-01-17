package com.example.bookkeeping.controller;

import com.example.bookkeeping.entity.Category;
import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class CalendarController {

    @Autowired
    private BillService billService;

    // 1. 页面跳转
    @GetMapping("/calendar")
    public String calendarPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Category> categories = billService.getAllCategories();
        model.addAttribute("categories", categories);
        return "calendar";
    }

    // 2. 异步数据接口 (FullCalendar 会调用这个)
    @GetMapping("/api/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam String start, // FullCalendar 自动传 ISO 日期
            @RequestParam String end,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        return billService.getCalendarData(user.getId(), type, categoryId, start, end);
    }

    // 3. 异步获取热力图数据
    @GetMapping("/api/calendar/heatmap")
    @ResponseBody
    public Map<String, Map<String, Object>> getHeatMap(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        return billService.getCalendarHeatMap(user.getId(), start, end, type, categoryId);
    }
    // 4. 异步获取某天的流水明细（支持过滤）
    @GetMapping("/api/calendar/day")
    @ResponseBody
    public List<Map<String, Object>> getDayDetails(@RequestParam String date,
                                                   @RequestParam(required = false) Integer type,
                                                   @RequestParam(required = false) Long categoryId,
                                                   HttpSession session) {
        User user = (User) session.getAttribute("user");
        // reuse getCalendarData with start=end=date to fetch that day's transactions
        return billService.getCalendarData(user.getId(), type, categoryId, date, date);
    }
}
