package ru.practicum.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.utils.DateTimeConstants;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalInterceptor implements HandlerInterceptor {

    @Value("${spring.application.name}")
    private String appName;

    private final StatClient statsClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            ResponseEntity<Object> statsResponse = statsClient.save(EndpointHitDto.builder()
                    .app(appName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(DateTimeConstants.toString(LocalDateTime.now()))
                    .build());
            if (!statsResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Ошибка при сохранении статистики: {}", statsResponse.getBody());
            }
        } catch (RuntimeException e) {
            log.error("Исключительная ситуация при сохранении статистики: {}", e.getMessage());
        }
        return true;
    }
}