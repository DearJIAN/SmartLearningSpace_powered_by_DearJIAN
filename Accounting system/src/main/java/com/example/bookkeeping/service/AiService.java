package com.example.bookkeeping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    @Value("${ai.doubao.api-url}")
    private String apiUrl;
    @Value("${ai.doubao.api-key}")
    private String apiKey;
    @Value("${ai.doubao.model-id}")
    private String modelId;

    private final OkHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiService() {
        // 设置较长的超时时间，防止 AI 思考时断连
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 流式对话核心逻辑 (翻译自 ai_api.py get_response_stream)
     */
    public void streamChat(List<Map<String, String>> history, SseEmitter emitter) {
        // 构造请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelId);
        payload.put("messages", history);
        payload.put("stream", true);
        payload.put("max_tokens", 4096);
        
        // 👇 新增配置：关键修改！
        payload.put("temperature", 0.1);       // 极低温度，让回答非常确定，不胡言乱语
        payload.put("frequency_penalty", 1.2); // 频率惩罚，严厉禁止重复说过的话（解决循环(完)(over)的问题）
        payload.put("presence_penalty", 0.8);  // 话题新鲜度惩罚

        try {
            String jsonPayload = mapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            // 异步调用
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {
                        emitter.send(SseEmitter.event().name("error").data("网络错误: " + e.getMessage()));
                        emitter.complete();
                    } catch (IOException ex) { /* ignore */ }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            emitter.send(SseEmitter.event().name("error").data("API Error: " + response.code()));
                            emitter.complete();
                            return;
                        }

                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                        String line;
                        StringBuilder fullContent = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            if (line.equals("data: [DONE]")) break;

                            if (line.startsWith("data: ")) {
                                String jsonStr = line.substring(6);
                                JsonNode root = mapper.readTree(jsonStr);
                                if (root.has("choices")) {
                                    JsonNode choice = root.get("choices").get(0);
                                    if(choice.has("delta") && choice.get("delta").has("content")) {
                                        String delta = choice.get("delta").get("content").asText();
                                        fullContent.append(delta);
                                        
                                        // ⚠️ 核心修改：处理换行符
                                        // SSE 协议中，data 字段如果包含换行符，客户端可能会解析错误。
                                        // 我们把 \n 替换成一个特殊标记 <br_mark>，前端再换回来。
                                        String safeDelta = delta.replace("\n", "<br_mark>");
                                        
                                        emitter.send(SseEmitter.event().data(safeDelta));
                                    }
                                }
                            }
                        }
                        
                        // 存入历史记录
                        Map<String, String> assistantMsg = new HashMap<>();
                        assistantMsg.put("role", "assistant");
                        assistantMsg.put("content", fullContent.toString());
                        history.add(assistantMsg);
                        
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
            });

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 文件解析 (支持 text, csv, xls, xlsx)
     */
    public String parseFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return "";
        fileName = fileName.toLowerCase();

        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return readExcel(file.getInputStream());
        } else {
            // 默认当做文本处理
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readExcel(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.toString()).append(" | ");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
