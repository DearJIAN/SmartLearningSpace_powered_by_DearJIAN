package com.example.bookkeeping.controller;

import com.example.bookkeeping.entity.User; // 补全
import com.example.bookkeeping.service.BillService; // 补全
import com.example.bookkeeping.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher; // 补全
import java.util.regex.Pattern; // 补全

import static java.lang.Thread.sleep;

@Controller
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private BillService billService;

    // 获取回答 (SSE流)
    @GetMapping(value = "/chat", produces = "text/event-stream")
    public SseEmitter chat(@RequestParam String message, HttpSession session) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3分钟超时

        // 🟢 智能指令拦截 (Function Calling 模拟)
        // 检测用户是否想生成数据，例如："帮我随机生成5个流水"
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Matcher matcher = Pattern.compile(".*(?:生成|造|来)\\s*(\\d+)\\s*(?:个|条)(?:流水|账单).*").matcher(message);
            if (matcher.find()) {
                try {

                    int count = Integer.parseInt(matcher.group(1));
                    if (count > 50) count = count; // 不限制最大生成数量
                    if (count < 1) count = 1;

                    // 👇 修改：在异步线程中模拟思考时间，避免阻塞主线程
                    int finalCount = count;
                    new Thread(() -> {
                        try {
                            // 模拟 AI 思考时间 500ms
                            Thread.sleep(500);

                            // 调用 Service 生成数据
                            billService.generateRandomBills(user.getId(), finalCount);

                            // 立即返回成功消息
                            String reply = "⚡️ 已为您智能生成 " + finalCount + " 条模拟流水数据！\n\n(系统已自动写入数据库，请刷新【账单明细】页面查看)";
                            emitter.send(SseEmitter.event().data(reply));

                            // 👇 发送特殊事件通知前端刷新
                            emitter.send(SseEmitter.event().name("magic").data("reload"));

                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            emitter.completeWithError(e);
                        }
                    }).start();

                    return emitter; // 直接返回，不走后面的 AI 逻辑
                } catch (Exception e) {
                    // 如果解析或执行出错，记录日志，允许程序继续向下走普通的 AI 聊天
                    e.printStackTrace();
                }
            }
        }

        // 1. 获取或初始化历史记录
        List<Map<String, String>> history = (List<Map<String, String>>) session.getAttribute("aiHistory");
        if (history == null) {
            history = new ArrayList<>();
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            
            // 👇 修改：优化 Prompt，禁止输出描述性格式文字
            sys.put("content", "你是一个花火智能理财助手。请按照Markdown格式生成专业的财务分析。严格遵守以下规则：\n" +
                    "1. 标题必须标准：'###' 与文字之间**必须保留一个空格** (例如 '### 收支概况')。\n" +
                    "2. **标题前面必须空一行**，不要紧接上一段文字。\n" +
                    "3. 表格必须标准 Markdown 格式。\n" +
                    "4. 需要分段时，请直接使用换行符，**严禁输出 '(空一行)'、'(newline)' 等描述性文字**。\n" +
                    "5. 允许在末尾添加适量的友好备注（如括号内容），但不要循环输出无意义的结束语。");
            
            history.add(sys);
            session.setAttribute("aiHistory", history);
        }

        // 2. 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        history.add(userMsg);

        // 3. 调用 AI
        aiService.streamChat(history, emitter);

        return emitter;
    }

    // 清空上下文
    @PostMapping("/clear")
    @ResponseBody
    public String clear(HttpSession session) {
        session.removeAttribute("aiHistory");
        return "ok";
    }

    // 文件上传解析
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        try {
            String content = aiService.parseFile(file);
            // 截断一下防止文件过大撑爆 Context
            if(content.length() > 10000) content = content.substring(0, 10000) + "...(内容过长已截断)";
            
            result.put("fileName", file.getOriginalFilename());
            result.put("content", "【分析文件：" + file.getOriginalFilename() + "】\n" + content);
            return result;
        } catch (IOException e) {
            result.put("error", "文件解析失败: " + e.getMessage());
            return result;
        }
    }
}
