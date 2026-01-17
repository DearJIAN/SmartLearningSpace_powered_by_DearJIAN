package com.smartcampus.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

public interface AccAiService {
    void streamChat(List<Map<String, String>> history, SseEmitter emitter);
}
