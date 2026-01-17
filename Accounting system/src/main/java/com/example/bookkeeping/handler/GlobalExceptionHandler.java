package com.example.bookkeeping.handler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        // 在控制台打印详细错误堆栈（重要！请在IDEA控制台查看红色报错信息）
        e.printStackTrace(); 
        
        // 将错误摘要传给页面
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("exceptionType", e.getClass().getSimpleName());
        
        return "error"; // 跳转到 error.html
    }
}
