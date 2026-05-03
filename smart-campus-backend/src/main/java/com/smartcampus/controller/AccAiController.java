package com.smartcampus.controller;

import com.smartcampus.entity.SysUser;
import com.smartcampus.service.AccAiService;
import com.smartcampus.service.AccBillService;
import com.smartcampus.service.AccBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 助手控制器 (Mock 实现)
 */
@RestController
@RequestMapping("/api/accounting/ai")
public class AccAiController {

    @Autowired
    private AccBillService billService;

    @Autowired
    private AccBudgetService budgetService;

    @Autowired
    private AccAiService aiService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam String message, HttpSession session) {
        SseEmitter emitter = new SseEmitter(180_000L);
        SysUser user = (SysUser) session.getAttribute("accountingUser");

        if (user == null) {
            try {
                emitter.send("请先登录");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 1. 智能指令拦截：生成流水 (更加宽松的正则，支持：生成10条测试账单)
        Matcher matcher = Pattern.compile(".*(?:生成|造|来).*?(\\d+).*?(?:流水|账单).*").matcher(message);
        if (matcher.find()) {
            String countStr = matcher.group(1);
            int count = (countStr != null) ? Integer.parseInt(countStr) : 10;
            if (count > 20)
                count = 20;
            int finalCount = count;

            new Thread(() -> {
                try {
                    emitter.send(SseEmitter.event().data("🔍 正在为您分析财务上下文..."));
                    Thread.sleep(600);
                    emitter.send(SseEmitter.event().data("⚡️ 正在为您的账户生成 " + finalCount + " 条真实账单流水..."));
                    billService.generateRandomBills(user.getUserId(), finalCount);
                    Thread.sleep(800);
                    emitter.send(SseEmitter.event().data(
                            "✅ 指令执行成功！<br_mark><br_mark>后台已成功注入 " + finalCount + " 条真实测试数据。您可以点击左侧菜单切换到【账单明细】进行核对。"));
                    emitter.send(SseEmitter.event().name("magic").data("reload"));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }).start();
            return emitter;
        }

        // 2. 动态 Context 注入
        StringBuilder context = new StringBuilder("\n--- 实时数据库快照 (真实数据) ---");

        // 如果包含相关词汇，注入实时数据
        String month = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<String, Object> budget = budgetService.getBudgetStatus(user.getUserId(), month);
        Map<String, Object> insight = billService.getInsightDashboard(user.getUserId());

        context.append("\n- 统计月份：" + month);
        context.append("\n- 预算余额：总额 " + budget.get("total") + "元，已支出 " + budget.get("used") + "元，剩余可用 "
                + budget.get("remaining") + "元。");
        context.append("\n- 财务洞察：" + insight.get("summary"));

        // 注入详细风险预警
        List<Map<String, String>> alerts = billService.getRiskAlerts(user.getUserId());
        if (alerts != null && !alerts.isEmpty()) {
            context.append("\n- 风险检测：发现 " + alerts.size() + " 项风险：");
            for (Map<String, String> alert : alerts) {
                context.append("\n  * [" + alert.get("title") + "] " + alert.get("desc"));
            }
        } else {
            context.append("\n- 风险检测：暂无异常发现。");
        }

        // 格式化消费占比，方便AI读取
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topCats = (List<Map<String, Object>>) insight.get("topCategories");
        StringBuilder catStr = new StringBuilder();
        if (topCats != null) {
            for (int i = 0; i < Math.min(topCats.size(), 3); i++) {
                Map<String, Object> cat = topCats.get(i);
                catStr.append(cat.get("name")).append(": ").append(cat.get("value")).append("元 (")
                        .append(cat.get("percent")).append("%); ");
            }
        }
        context.append("\n- 消费分布 (Top3)：" + catStr.toString());
        context.append("\n--- 快照结束 ---");

        List<Map<String, String>> history = new ArrayList<>();

        // 系统提示词
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个极其专业的智能财务管家，集成在校园智慧管理平台中。你的名字叫'火花'。"
                + "\n你的职责：分析数据、提供建议、辅助记账。"
                + "\n当前财务背景：" + context.toString()
                + "\n回答要求：1.务必基于提供的[实时数据]进行回答。2.语气亲切、精炼。3.如果涉及多项数据，请使用Markdown表格展示。");
        history.add(systemMsg);

        // 用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        history.add(userMsg);

        aiService.streamChat(history, emitter);
        return emitter;
    }

}
