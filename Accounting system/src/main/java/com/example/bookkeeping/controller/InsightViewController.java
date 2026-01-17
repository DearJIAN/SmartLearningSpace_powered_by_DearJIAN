package com.example.bookkeeping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 负责页面跳转的控制器
 */
@Controller
@RequestMapping("/insight/view")
public class InsightViewController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "insight/dashboard"; // 对应 templates/insight/dashboard.html
    }

    @GetMapping("/profile")
    public String profile() {
        return "insight/profile";
    }

    @GetMapping("/timeline")
    public String timeline() {
        return "insight/timeline";
    }

    @GetMapping("/risk")
    public String risk() {
        return "insight/risk";
    }

    @GetMapping("/goal")
    public String goal() {
        return "insight/goal";
    }
}
