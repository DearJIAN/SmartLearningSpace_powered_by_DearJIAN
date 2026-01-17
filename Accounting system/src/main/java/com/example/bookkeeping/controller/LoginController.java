package com.example.bookkeeping.controller;

import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // 登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 处理登录请求
    @PostMapping("/login")
    public String login(String username, String password, HttpSession session, Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/index"; // 登录成功跳转首页
        }
        model.addAttribute("msg", "用户名或密码错误");
        return "login";
    }

    // 注册页
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 处理注册请求
    // 注意：这里是在业务表 user 中插入记录，不是创建数据库(MySQL)的账号
    @PostMapping("/register")
    public String register(User user, Model model) {
        try {
            userService.register(user);
            model.addAttribute("msg", "注册成功，请登录");
            return "login";
        } catch (Exception e) {
            model.addAttribute("msg", "注册失败，用户名可能已存在");
            return "register";
        }
    }

    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}