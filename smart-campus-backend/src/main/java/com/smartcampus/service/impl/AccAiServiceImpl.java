package com.smartcampus.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.service.AccAiService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AccAiServiceImpl implements AccAiService {

    @Value("${ai.doubao.api-url}")
    private String apiUrl;
    @Value("${ai.doubao.api-key}")
    private String apiKey;
    @Value("${ai.doubao.model-id}")
    private String modelId;

    private final OkHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccAiServiceImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void streamChat(List<Map<String, String>> history, SseEmitter emitter) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelId);
        payload.put("messages", history);
        payload.put("stream", true);
        payload.put("max_tokens", 4096);
        payload.put("temperature", 0.1);
        payload.put("frequency_penalty", 1.2);
        payload.put("presence_penalty", 0.8);

        try {
            String jsonPayload = mapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {
                        emitter.send(SseEmitter.event().name("error").data("网络错误: " + e.getMessage()));
                        emitter.complete();
                    } catch (IOException ex) {
                        /* ignore */ }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            emitter.send(SseEmitter.event().name("error").data("API Error: " + response.code()));
                            emitter.complete();
                            return;
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty())
                                continue;
                            if (line.equals("data: [DONE]"))
                                break;

                            if (line.startsWith("data: ")) {
                                String jsonStr = line.substring(6);
                                JsonNode root = mapper.readTree(jsonStr);
                                if (root.has("choices")) {
                                    JsonNode choice = root.get("choices").get(0);
                                    if (choice.has("delta") && choice.get("delta").has("content")) {
                                        String delta = choice.get("delta").get("content").asText();
                                        // SSE 协议中，data 字段如果包含换行符，客户端可能会解析错误。
                                        // 我们把 \n 替换成一个特殊标记 <br_mark>，前端再换回来。
                                        String safeDelta = delta.replace("\n", "<br_mark>");
                                        emitter.send(SseEmitter.event().data(safeDelta));
                                    }
                                }
                            }
                        }
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
}
