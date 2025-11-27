package ru.practicum.entities.comment.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.centralRepository.CommentRepository;
import ru.practicum.centralRepository.EventRepository;
import ru.practicum.centralRepository.UserRepository;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.entities.comment.model.Comment;
import ru.practicum.entities.comment.model.CommentDto;
import ru.practicum.entities.comment.model.CommentMapper;
import ru.practicum.entities.comment.model.CommentUpdateDto;
import ru.practicum.entities.event.model.Event;
import ru.practicum.entities.user.model.User;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.utils.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StatClient statsClient;

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateValidationException("Некорректный диапазон дат: rangeStart не может быть больше rangeEnd");
        }

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
    public List<CommentDto> getCommentsByEventId(LocalDateTime rangeStart, LocalDateTime rangeEnd, Long eventId, Integer from, Integer size) {
        eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Событие с ID " + eventId + " не найдено"));

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateValidationException("Некорректный диапазон дат: rangeStart не может быть больше rangeEnd");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusDays(30);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now();
        }

        List<Comment> comments = commentRepository.findCommentsByEventIdAndDates(
                eventId, rangeStart, rangeEnd, PageRequest.of(from / size, size));

        // Формируем список URI для запроса статистики
        List<String> uris = comments.stream()
                .map(comment -> "/comments/" + comment.getId())
                .toList();

        // Запрашиваем статистику для всех URI (одним запросом)
        List<StatsDto> stats = statsClient.getStats(
                DateTimeConstants.toString(rangeStart),
                DateTimeConstants.toString(rangeEnd),
                uris,
                true
        );

        // Создаем map для быстрого доступа к статистике
        Map<String, Long> statsMap = stats.stream()
                .collect(Collectors.toMap(
                        StatsDto::getUri,
                        StatsDto::getHits
                ));

        // Добавляем статистику в список комментариев
        return comments.stream()
                .map(comment -> {
                    CommentDto dto = CommentMapper.toCommentDto(comment);
                    Long views = statsMap.getOrDefault("/comments/" + comment.getId(), 0L);
                    dto.setViews(views);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(LocalDateTime rangeStart, LocalDateTime rangeEnd, Long userId, Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateValidationException("Некорректный диапазон дат: rangeStart не может быть больше rangeEnd");
        }

        List<Comment> comments = commentRepository.findCommentsByUserIdAndDates(
                userId, rangeStart, rangeEnd, PageRequest.of(from / size, size));

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CommentDto commentNewDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));
        Comment comment = CommentMapper.toComment(commentNewDto, user, event);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        if (commentUpdateDto.getMessage() == null || commentUpdateDto.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Некорректное значение параметра message: не должно быть пустым");
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь с id " + userId + " не имеет прав на редактирование этого комментария");
        }

        comment.setMessage(commentUpdateDto.getMessage());
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updatedComment);
    }

    @Transactional
    public void deletePrivateComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь с id " + userId + " не имеет прав на удаление этого комментария");
        }
        commentRepository.delete(comment);
    }
}