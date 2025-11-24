package com.yash.log.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import  com.yash.log.dto.ErrorCategory;


@Schema(
        name = "ErrorLog",
        description = "Entity representing an error log entry"

)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ErrorCategory category;

    @Column(length = 512)
    private String message;

    @Column(length = 128)
    private String sourceSystem;


}
