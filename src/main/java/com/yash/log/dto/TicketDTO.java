package com.yash.log.dto;



import com.yash.log.constants.Priority;
import com.yash.log.constants.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TicketDTO {

    private Long ticketId;
    private String title;
    private String errorMessage;
    private Priority priority;   // LOW, MEDIUM, HIGH, CRITICAL
    private Status status;       // OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED
    private Long userId;
    private Long errorId;
    private String createdBy;    // userId of creator
    private String assignedTo;   // userId of assignee
    private String comments;
    private String reviewer;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Boolean isSuccess;

}

