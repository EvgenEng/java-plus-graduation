package ru.practicum.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
public class ErrorHandlerConfig {

    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String errorMessage = "{\"error\":\"Bad Request\",\"message\":\"Missing required query parameters. Check API documentation.\"}";
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));
        };
    }
}
