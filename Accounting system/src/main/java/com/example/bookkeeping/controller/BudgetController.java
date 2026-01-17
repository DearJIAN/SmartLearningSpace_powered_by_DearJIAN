package com.example.bookkeeping.controller;

import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping("/budget")
    public String budgetPage(@RequestParam(required = false) String month, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        // 默认当前月
        if (month == null) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        model.addAttribute("data", budgetService.getBudgetStatus(user.getId(), month));
        model.addAttribute("currentMonth", month);
        
        return "budget";
    }

    @PostMapping("/budget/set")
    public String setBudget(String month, BigDecimal amount, HttpSession session) {
        User user = (User) session.getAttribute("user");
        budgetService.saveBudget(user.getId(), month, amount);
        return "redirect:/budget?month=" + month;
    }

    // 👇 新增：AJAX 接口保存预算
    @PostMapping("/budget/ajax/save")
    @org.springframework.web.bind.annotation.ResponseBody
    public String saveBudgetAjax(String month, BigDecimal amount, HttpSession session) {
        User user = (User) session.getAttribute("user");
        budgetService.saveBudget(user.getId(), month, amount);
        return "ok";
    }
}
