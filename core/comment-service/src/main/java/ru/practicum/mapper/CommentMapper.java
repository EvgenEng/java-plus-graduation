package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.CommentDto;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .userId(commentDto.getUserId())
                .eventId(commentDto.getEventId())
                .message(commentDto.getMessage())
                .created(commentDto.getCreated() != null ? commentDto.getCreated() : LocalDateTime.now())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .eventId(comment.getEventId())
                .message(comment.getMessage())
                .created(comment.getCreated())
                .build();
    }
}
