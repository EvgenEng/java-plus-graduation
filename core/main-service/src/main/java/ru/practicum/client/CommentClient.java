package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.entities.comment.model.CommentDto;
import ru.practicum.entities.comment.model.CommentUpdateDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "comment-service")
public interface CommentClient {

    // Admin API
    @GetMapping("/admin/comments")
    List<CommentDto> getAllCommentsForAdmin(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @DeleteMapping("/admin/comments/{commentId}")
    void deleteCommentByAdmin(@PathVariable Long commentId);

    // Public API
    @GetMapping("/comments/{eventId}")
    List<CommentDto> getCommentsByEventId(
            @PathVariable Long eventId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    // Private API
    @GetMapping("/users/comments/{userId}")
    List<CommentDto> getCommentsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @PostMapping("/users/comments/{userId}/{eventId}")
    CommentDto createComment(
            @RequestBody CommentDto commentNewDto,
            @PathVariable Long userId,
            @PathVariable Long eventId);

    @PatchMapping("/users/comments/{userId}/{commentId}")
    CommentDto updateCommentByUser(
            @RequestBody CommentUpdateDto commentUpdateDto,
            @PathVariable Long userId,
            @PathVariable Long commentId);

    @DeleteMapping("/users/comments/{userId}/{commentId}")
    void deleteCommentByUser(
            @PathVariable Long userId,
            @PathVariable Long commentId);
}
