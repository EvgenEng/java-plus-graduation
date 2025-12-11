package ru.practicum.api_controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.CommentClient;
import ru.practicum.entities.comment.model.CommentDto;
import ru.practicum.entities.comment.model.CommentUpdateDto;
import ru.practicum.utils.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentClient commentClient;

    // Admin API
    // 1.1 Получение всех комментариев
    @GetMapping("/admin/comments")
    public ResponseEntity<List<CommentDto>> getAllCommentsForAdmin(
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("Получение всех комментариев через Feign: rangeStart={}, rangeEnd={}, from={}, size={}",
                rangeStart, rangeEnd, from, size);

        List<CommentDto> comments = commentClient.getAllCommentsForAdmin(rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(comments);
    }

    // 1.2 Удаление комментария администратором
    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Удаление комментария администратором через Feign: ID={}", commentId);

        commentClient.deleteCommentByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }

    // Public API
    // 2.1 Получение комментариев по событию
    @GetMapping("/comments/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEventId(
            @PathVariable Long eventId,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("Получение комментариев по событию через Feign: eventId={}, rangeStart={}, rangeEnd={}",
                eventId, rangeStart, rangeEnd);

        List<CommentDto> comments = commentClient.getCommentsByEventId(eventId, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(comments);
    }

    // Private API
    // 3.1 Получение комментариев пользователя
    @GetMapping("/users/comments/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("Получение комментариев пользователя через Feign: userId={}, rangeStart={}, rangeEnd={}",
                userId, rangeStart, rangeEnd);

        List<CommentDto> comments = commentClient.getCommentsForUser(userId, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(comments);
    }

    // 3.2 Добавление нового комментария
    @PostMapping("/users/comments/{userId}/{eventId}")
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentDto commentNewDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Создание комментария через Feign: userId={}, eventId={}", userId, eventId);

        CommentDto createdComment = commentClient.createComment(commentNewDto, userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    // 3.3 Обновление комментария
    @PatchMapping("/users/comments/{userId}/{commentId}")
    public ResponseEntity<CommentDto> updateCommentByUser(
            @Valid @RequestBody CommentUpdateDto commentUpdateDto,
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        log.info("Обновление комментария через Feign: userId={}, commentId={}", userId, commentId);

        CommentDto updatedComment = commentClient.updateCommentByUser(commentUpdateDto, userId, commentId);
        return ResponseEntity.ok(updatedComment);
    }

    // 3.4 Удаление комментария пользователем
    @DeleteMapping("/users/comments/{userId}/{commentId}")
    public ResponseEntity<Void> deleteCommentByUser(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        log.info("Удаление комментария пользователем через Feign: userId={}, commentId={}", userId, commentId);

        commentClient.deleteCommentByUser(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
