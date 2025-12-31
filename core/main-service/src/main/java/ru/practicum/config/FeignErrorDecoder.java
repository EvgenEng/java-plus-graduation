package ru.practicum.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error: {} - {}", response.status(), response.reason());

        return switch (response.status()) {
            case 400 -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный запрос к микросервису");
            case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден в микросервисе");
            case 500 -> new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера микросервиса"
            );
            default -> new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Неизвестная ошибка от микросервиса"
            );
        };
    }
}
