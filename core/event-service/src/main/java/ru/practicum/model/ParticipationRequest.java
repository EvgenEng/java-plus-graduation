package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests", schema = "event_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, CONFIRMED, REJECTED, CANCELED

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
