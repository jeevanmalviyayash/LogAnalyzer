package com.yash.log.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(
        name = "Log",
        description = "Schema that hold Log information."
)
@Data
public class LogDTO {
    @Schema(
            name = "ErrorId",
            description = "Schema that hold Error Id of log.",
            example = "1"
    )
    private Long errorId;

  @Schema(
            name = "ErrorLevel",
            description = "Schema that hold Error Level of log.",
            example = "ERROR"
  )
    private String errorLevel;

    @Schema(
            name = "ErrorMessage",
            description = "Schema that hold Error Message of log.",
            example = "NullPointerException at line 45"
    )
    private String errorMessage;

    @Schema(
            name = "TimeStamp",
            description = "Schema that hold Time Stamp of log.",
            example = "2024-06-15T10:15:30"
    )
    private LocalDateTime timeStamp;

    @Schema(
            name = "UserId",
            description = "Schema that hold User Id associated with the log.",
            example = "1001"
    )
    private Long userId;

    @Schema(
            name = "Source",
            description = "Schema that hold Source of log.",
            example = "com.example.MyClass"
    )
    private String source;

    @Schema(
            name = "ErrorType",
            description = "Schema that hold Error Type of log.",
            example = "NullPointerException"
    )
    private String errorType;
}
