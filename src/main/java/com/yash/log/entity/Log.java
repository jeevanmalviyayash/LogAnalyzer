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


}
