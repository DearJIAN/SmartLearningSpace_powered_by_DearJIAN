package com.smartcampus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.Result;
import com.smartcampus.entity.AccBill;
import com.smartcampus.entity.AccCategory;
import com.smartcampus.entity.SysUser;
import com.smartcampus.service.AccBillService;
import com.smartcampus.service.AccCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 账单管理接口
 */
@RestController
@RequestMapping("/api/accounting/bills")
@CrossOrigin
public class AccBillController {

    @Autowired
    private AccBillService billService;

    @Autowired
    private AccCategoryService categoryService;

    /**
     * 分页查询账单
     */
    @GetMapping
    public Result<Page<AccBill>> getBillList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer type,
            HttpSession session) {

        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        Page<AccBill> page = new Page<>(pageNum, pageSize);
        Page<AccBill> result = billService.getBillPage(page, user.getUserId(), categoryId, startDate, endDate, type);

        return Result.success(result);
    }

    /**
     * 获取账单统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBillStats(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {

        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        Map<String, Object> stats = billService.getBillStats(user.getUserId(), categoryId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 新增或更新账单
     */
    @PostMapping
    public Result<String> saveBill(@RequestBody AccBill bill, HttpSession session) {
        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        bill.setUserId(user.getUserId());
        billService.saveOrUpdate(bill);

        return Result.success("保存成功");
    }

    /**
     * 删除账单
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteBill(@PathVariable Long id, HttpSession session) {
        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        // 验证账单属于当前用户
        AccBill bill = billService.getById(id);
        if (bill == null || !bill.getUserId().equals(user.getUserId())) {
            return Result.error("无权删除");
        }

        billService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 随机生成账单
     */
    @PostMapping("/generate")
    public Result<String> generateBills(
            @RequestParam(defaultValue = "10") Integer count,
            HttpSession session) {

        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        billService.generateRandomBills(user.getUserId(), count);
        return Result.success("生成成功");
    }

    /**
     * 清空所有账单
     */
    @DeleteMapping("/clear")
    public Result<String> clearBills(HttpSession session) {
        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        billService.clearBills(user.getUserId());
        return Result.success("清空成功");
    }

    /**
     * 获取所有分类（用于下拉选择）
     */
    @GetMapping("/categories")
    public Result<List<AccCategory>> getCategories(HttpSession session) {
        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        List<AccCategory> categories = categoryService.getUserCategories(user.getUserId(), null);
        return Result.success(categories);
    }

    // 辅助方法：获取当前用户
    private SysUser getCurrentUser(HttpSession session) {
        return (SysUser) session.getAttribute("accountingUser");
    }

    /**
     * 导出 Excel 账单
     */
    @GetMapping("/export")
    public void exportBills(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer type,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        SysUser user = getCurrentUser(session);
        if (user == null)
            return;

        List<AccBill> bills = billService.getBillList(user.getUserId(), categoryId, startDate, endDate, type);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("我的账单");
            // 表头
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("日期");
            header.createCell(1).setCellValue("类型");
            header.createCell(2).setCellValue("分类");
            header.createCell(3).setCellValue("金额");
            header.createCell(4).setCellValue("备注");

            // 内容
            int rowIdx = 1;
            for (AccBill bill : bills) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(bill.getBillDate().toString());
                row.createCell(1).setCellValue(bill.getType() == 1 ? "收入" : "支出");
                row.createCell(2).setCellValue(bill.getCategoryName() != null ? bill.getCategoryName() : "-");
                row.createCell(3).setCellValue(bill.getAmount().doubleValue());
                row.createCell(4).setCellValue(bill.getRemark());
            }

            // 输出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("账单导出_" + user.getUsername() + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        }
    }
}
