package com.yash.log.entity;

import com.yash.log.constants.Priority;
import com.yash.log.constants.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "ticket")
@Data
@NoArgsConstructor
public class Ticket {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq")
//    @SequenceGenerator(
//            name = "ticket_seq",
//            sequenceName = "ticket_sequence",
//            initialValue = 1000,
//            allocationSize = 1
//    )

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    private String title;

    @Column(length = 5000)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Priority priority;  // LOW, MEDIUM, HIGH, CRITICAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Status status;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String createdBy;// userId of creator

    @Column(nullable = false)
    private String assignedTo;    // userId of assignee

    private String comments;

    private String reviewer;

    private Long errorId;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;


}
