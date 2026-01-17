package com.example.bookkeeping.config;

import com.example.bookkeeping.entity.Category;
import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.mapper.CategoryMapper;
import com.example.bookkeeping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化数据：修复分类乱码，并创建默认管理员
 */
@Component
public class DataInit implements CommandLineRunner {

    @Autowired
    private CategoryMapper categoryMapper;
    
    // 👇 注入用户服务
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // 1. 检查并创建默认用户
        initDefaultUser();

        // 2. 修复分类数据
        System.out.println("正在修复分类乱码数据...");
        fixCategory(1L, "工资", 1);
        fixCategory(2L, "奖金", 1);
        fixCategory(3L, "餐饮", 2);
        fixCategory(4L, "交通", 2);
        fixCategory(5L, "购物", 2);
        fixCategory(6L, "住房", 2);
        System.out.println("分类数据修复完成！");
    }

    private void initDefaultUser() {
        // 如果用户表是空的，创建一个默认管理员
        if (userService.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456"); // 这里是明文，service层会负责加密
            admin.setEmail("admin@example.com");
            userService.register(admin);
            System.out.println("----------------------------------------------------------");
            System.out.println("⚠️ 检测到当前无用户，已自动创建默认管理员账号：");
            System.out.println("👉 账号：admin");
            System.out.println("👉 密码：123456");
            System.out.println("----------------------------------------------------------");
        }
    }

    private void fixCategory(Long id, String name, Integer type) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            category = new Category();
            category.setId(id);
            category.setUserId(0L); 
            category.setName(name);
            category.setType(type);
            categoryMapper.insert(category);
        } else {
            category.setName(name);
            category.setType(type); 
            categoryMapper.updateById(category);
        }
    }
}
