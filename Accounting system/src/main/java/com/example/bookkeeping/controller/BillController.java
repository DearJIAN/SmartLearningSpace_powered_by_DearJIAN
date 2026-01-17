package com.example.bookkeeping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 补全引用
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookkeeping.entity.Bill;
import com.example.bookkeeping.entity.Category; // 补全引用
import com.example.bookkeeping.entity.User;
import com.example.bookkeeping.mapper.CategoryMapper; // 补全引用
import com.example.bookkeeping.service.BillService;
import org.apache.poi.ss.usermodel.*; // POI引用
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // POI引用
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal; // 补全引用
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.HashMap; // 补全引用
import java.util.List;
import java.util.Map; // 补全引用

@Controller
public class BillController {

    @Autowired
    private BillService billService;

    @Autowired
    private CategoryMapper categoryMapper; // 需要查询分类名导出

    @Autowired
    private com.example.bookkeeping.service.BudgetService budgetService; // 预算服务

    // 首页：账单列表
    @GetMapping({"/", "/index"})
    public String index(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        Page<Bill> page = new Page<>(pageNum, 10);
        Page<Bill> billPage = billService.getBillPage(page, user.getId(), categoryId, startDate, endDate);

        // 👇 新增：获取统计数据
        Map<String, Object> stats = billService.getBillStats(user.getId(), categoryId, startDate, endDate);
        model.addAttribute("stats", stats);

        // 👇 新增：获取当前月预算信息
        String currentMonth = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<String, Object> budgetStatus = budgetService.getBudgetStatus(user.getId(), currentMonth);
        model.addAttribute("budget", budgetStatus);
        model.addAttribute("currentMonth", currentMonth);

        model.addAttribute("user", user); // Added this line to expose User to the View
        model.addAttribute("page", billPage);
        model.addAttribute("categories", billService.getAllCategories());
        return "index";
    }

    // 保存账单
    @PostMapping("/bill/save")
    public String saveBill(Bill bill, HttpSession session) {
        User user = (User) session.getAttribute("user");
        bill.setUserId(user.getId());
        if (bill.getId() == null && bill.getBillDate() == null) {
            bill.setBillDate(LocalDate.now()); // 默认今天
        }
        billService.saveOrUpdate(bill);
        return "redirect:/index";
    }

    // 删除账单
    @GetMapping("/bill/delete/{id}")
    public String deleteBill(@PathVariable Long id) {
        billService.removeById(id);
        return "redirect:/index";
    }

    // 👇 新增：Excel 导出功能
    @GetMapping("/bill/export")
    public void exportExcel(HttpServletResponse response, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        
        // 1. 获取该用户所有账单 (不分页)
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, user.getId()).orderByDesc(Bill::getBillDate);
        List<Bill> list = billService.list(wrapper);

        // 2. 创建 Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("账单明细");
            
            // 表头
            Row header = sheet.createRow(0);
            String[] headers = {"日期", "类型", "分类", "金额", "备注"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // 内容
            int rowNum = 1;
            for (Bill bill : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(bill.getBillDate().toString());
                row.createCell(1).setCellValue(bill.getType() == 1 ? "收入" : "支出");
                
                // 处理分类名称
                String catName = "未知";
                Category c = categoryMapper.selectById(bill.getCategoryId());
                if(c != null) catName = c.getName(); // 简单处理，实际应缓存
                
                row.createCell(2).setCellValue(catName);
                row.createCell(3).setCellValue(bill.getAmount().doubleValue());
                row.createCell(4).setCellValue(bill.getRemark());
            }

            // 输出文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("我的账单本.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
            workbook.write(response.getOutputStream());
        }
    }

    // 👇 新增：随机生成接口
    @PostMapping("/bill/generate")
    @ResponseBody
    public String generate(@RequestParam(defaultValue = "10") Integer count, HttpSession session) {
        User user = (User) session.getAttribute("user");
        billService.generateRandomBills(user.getId(), count);
        return "ok";
    }

    // 👇 新增：清空接口
    @PostMapping("/bill/clear")
    @ResponseBody
    public String clear(HttpSession session) {
        User user = (User) session.getAttribute("user");
        billService.clearBills(user.getId());
        return "ok";
    }

    // 👇 新增：健康体检接口
    @GetMapping("/bill/health")
    @ResponseBody
    public Map<String, Object> healthCheck(HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<Bill> bills = billService.list(new LambdaQueryWrapper<Bill>().eq(Bill::getUserId, user.getId()));
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Bill b : bills) {
            if (b.getType() == 1) totalIncome = totalIncome.add(b.getAmount());
            else totalExpense = totalExpense.add(b.getAmount());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("income", totalIncome);
        result.put("expense", totalExpense);
        
        // 简单评级逻辑
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            result.put("score", "😟");
            result.put("advice", "暂无收入，需要加油哦！");
        } else {
            BigDecimal ratio = totalExpense.divide(totalIncome, 2, BigDecimal.ROUND_HALF_UP);
            if (ratio.doubleValue() > 1.0) {
                result.put("score", "💸 月光族");
                result.put("advice", "警报！支出已超过收入，建议立即开启省钱模式！");
                result.put("type", "danger");
            } else if (ratio.doubleValue() < 0.5) {
                result.put("score", "💰 理财达人");
                result.put("advice", "太棒了！你的储蓄率很高，财务非常健康。");
                result.put("type", "success");
            } else {
                result.put("score", "😐 收支平衡");
                result.put("advice", "还可以，但建议适当控制非必要开支。");
                result.put("type", "warning");
            }
        }
        return result;
    }
}