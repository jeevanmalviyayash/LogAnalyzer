package com.yash.log.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Schema(name = "ErrorLog", description = "Entity representing a log entry")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Log extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long errorId;

    private String errorLevel;
    private String errorMessage;
    private LocalDateTime timeStamp;

    private Long userId;
    private String source;
    private String errorType;

    private String title;
    //private String description;
    private String username;
    private LocalDateTime createdDate;
}
