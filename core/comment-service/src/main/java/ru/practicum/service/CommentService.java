package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentUpdateDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Integer from, Integer size) {
        validateDateRange(rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findFilteredComments(rangeStart, rangeEnd, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAdminComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEventId(LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Long eventId, Integer from, Integer size) {
        validateDateRange(rangeStart, rangeEnd);

        if (rangeStart == null) rangeStart = LocalDateTime.now().minusDays(30);
        if (rangeEnd == null) rangeEnd = LocalDateTime.now();

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findCommentsByEventIdAndDates(
                eventId, rangeStart, rangeEnd, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Long userId, Integer from, Integer size) {
        validateDateRange(rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findCommentsByUserIdAndDates(
                userId, rangeStart, rangeEnd, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CommentDto commentDto) {
        // Проверяем, не оставлял ли пользователь уже комментарий к этому событию
        if (commentRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ValidationException("Пользователь уже оставлял комментарий к этому событию");
        }

        // Создаем комментарий
        Comment comment = Comment.builder()
                .userId(userId)
                .eventId(eventId)
                .message(commentDto.getMessage())
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        if (commentUpdateDto.getMessage() == null || commentUpdateDto.getMessage().trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("Пользователь не имеет прав на редактирование этого комментария");
        }

        comment.setMessage(commentUpdateDto.getMessage());
        Comment updatedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(updatedComment);
    }

    @Transactional
    public void deletePrivateComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("Пользователь не имеет прав на удаление этого комментария");
        }

        commentRepository.delete(comment);
    }

    // Вспомогательные методы
    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateValidationException("Начальная дата не может быть позже конечной");
        }
    }
}
