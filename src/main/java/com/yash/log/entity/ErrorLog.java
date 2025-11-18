package com.yash.log.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@Schema(
        name = "ErrorLog",
        description = "Entity representing an error log entry"
)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long errorId;

    @Schema(
            name = "Error Level",
            description = "The severity level of the error",
            example = "ERROR"
    )
    private String errorLevel;

    @Schema(
            name = "Error Message",
            description = "The detailed error message",
            example = "NullPointerException at line 42"
    )
    private String errorMessage;


    @Schema(
            name = "Timestamp",
            description = "The timestamp when the error occurred",
            example = "2024-06-15T14:30:00"
    )
    private LocalDateTime timeStamp;



}
