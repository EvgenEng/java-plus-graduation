package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentUpdateDto;
import ru.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // Admin API
    @GetMapping("/admin/comments")
    public ResponseEntity<List<CommentDto>> getAllCommentsForAdmin(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение всех комментариев: rangeStart={}, rangeEnd={}, from={}, size={}",
                rangeStart, rangeEnd, from, size);

        List<CommentDto> comments = commentService.getComments(rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Удаление комментария администратором: ID={}", commentId);

        commentService.deleteAdminComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // Public API
    @GetMapping("/comments/{eventId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEventId(
            @PathVariable Long eventId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение комментариев по событию: eventId={}, rangeStart={}, rangeEnd={}",
                eventId, rangeStart, rangeEnd);

        List<CommentDto> comments = commentService.getCommentsByEventId(rangeStart, rangeEnd, eventId, from, size);
        return ResponseEntity.ok(comments);
    }

    // Private API
    @GetMapping("/users/comments/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение комментариев пользователя: userId={}, rangeStart={}, rangeEnd={}",
                userId, rangeStart, rangeEnd);

        List<CommentDto> comments = commentService.getCommentsByUserId(rangeStart, rangeEnd, userId, from, size);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/users/comments/{userId}/{eventId}")
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentDto commentNewDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Создание комментария: userId={}, eventId={}", userId, eventId);

        CommentDto createdComment = commentService.addComment(userId, eventId, commentNewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/users/comments/{userId}/{commentId}")
    public ResponseEntity<CommentDto> updateCommentByUser(
            @Valid @RequestBody CommentUpdateDto commentUpdateDto,
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        log.info("Обновление комментария: userId={}, commentId={}", userId, commentId);

        CommentDto updatedComment = commentService.updateComment(userId, commentId, commentUpdateDto);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/users/comments/{userId}/{commentId}")
    public ResponseEntity<Void> deleteCommentByUser(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        log.info("Удаление комментария пользователем: userId={}, commentId={}", userId, commentId);

        commentService.deletePrivateComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
