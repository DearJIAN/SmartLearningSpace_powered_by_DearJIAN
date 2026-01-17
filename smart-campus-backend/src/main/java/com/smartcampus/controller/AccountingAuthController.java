package com.smartcampus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.Result;
import com.smartcampus.entity.SysUser;
import com.smartcampus.mapper.SysUserMapper;
import com.smartcampus.service.AccCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 记账模块认证接口
 */
@RestController
@RequestMapping("/api/accounting/auth")
@CrossOrigin
public class AccountingAuthController {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private AccCategoryService categoryService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params, HttpSession session) {
        String username = params.get("username");
        String password = params.get("password");

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error("用户名和密码不能为空");
        }

        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 简单密码验证（实际项目应使用加密）
        if (!user.getPassword().equals(password)) {
            return Result.error("密码错误");
        }

        // 登录成功，存入 session
        session.setAttribute("accountingUser", user);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());

        return Result.success("登录成功", data);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        Long count = userMapper.selectCount(wrapper);

        if (count > 0) {
            return Result.error("用户名已存在");
        }

        // 默认角色为学生（2）
        if (user.getRole() == null) {
            user.setRole(2);
        }

        // 默认信用分100
        if (user.getCreditScore() == null) {
            user.setCreditScore(100);
        }

        userMapper.insert(user);

        // 为新用户初始化默认分类
        categoryService.initDefaultCategories(user.getUserId());

        return Result.success("注册成功");

    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.removeAttribute("accountingUser");
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<SysUser> getCurrentUser(HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(user);
    }
}
