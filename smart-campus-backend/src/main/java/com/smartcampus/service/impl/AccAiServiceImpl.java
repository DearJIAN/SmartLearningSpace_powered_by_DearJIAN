package com.smartcampus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.service.AccAiService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
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

    @Value("${ai.flask.base-url}")
    private String flaskBaseUrl;

    @Value("${ai.flask.chat-stream-path:/api/chat/stream}")
    private String chatStreamPath;

    private final OkHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccAiServiceImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void streamChat(List<Map<String, String>> history, SseEmitter emitter) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messages", history);
        payload.put("scene", "智学空间-个人记账");

        try {
            String jsonPayload = mapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(buildFlaskUrl())
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendError(emitter, "AI 服务连接失败: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            sendError(emitter, "AI 服务返回异常: HTTP " + response.code());
                            return;
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            if (line.startsWith("delta:")) {
                                String delta = line.substring("delta:".length());
                                emitter.send(SseEmitter.event().data(delta.replace("\n", "<br_mark>")));
                            } else if (line.startsWith("error:")) {
                                emitter.send(SseEmitter.event().name("error").data(line.substring("error:".length())));
                                break;
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

    private String buildFlaskUrl() {
        String base = flaskBaseUrl == null ? "" : flaskBaseUrl.trim();
        String path = chatStreamPath == null ? "/api/chat/stream" : chatStreamPath.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return base + path;
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.complete();
        } catch (IOException ignored) {
            emitter.complete();
        }
    }
}
