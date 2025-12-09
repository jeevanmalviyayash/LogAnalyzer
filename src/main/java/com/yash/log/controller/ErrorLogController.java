package com.yash.log.controller;

import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;
import com.yash.log.entity.User;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.impl.LogFileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.yash.log.dto.ErrorCategoryStatDto;
import com.yash.log.dto.DailyErrorCountDto;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Tag(
        name = "Error Log Management APIs",
        description = "APIs for managing and analyzing error logs"
)

@RestController
@RequestMapping("/api/errors")
@CrossOrigin("*")
public class ErrorLogController {

    private final LogFileServiceImpl logFileServiceImpl;

    private final IUserRepository userRepository; // <-- type matches interface

    public ErrorLogController(LogFileServiceImpl logFileServiceImpl, IUserRepository userRepository) {
        this.logFileServiceImpl = logFileServiceImpl;
        this.userRepository = userRepository;
    }


    @Operation(
            summary = "Upload Log File",
            description = "Upload a log file to parse and store error logs"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadLogFile(@RequestParam("file") MultipartFile file) {
        try {

            //validate file type
            if (!file.getOriginalFilename().endsWith(".log")) {
                throw new IllegalArgumentException("Invalid file type. Only .log files are accepted.");
            }

            // Valudate size (e.g. max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File too large. Max size is 10MB");
            }

            logFileServiceImpl.parseAndSaveLogs(file);
            return ResponseEntity.ok("Logs uploaded and saved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }


    @GetMapping("/all-logs")
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> allLogs = logFileServiceImpl.getAllLogs();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(allLogs);
    }

    @PostMapping(value = "/saveManualError", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveManualError(@RequestBody LogDTO logDto) {
        try {
            if (logDto.getErrorMessage() == null || logDto.getErrorMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error message cannot be empty");
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String userEmail = auth.getName();
            User user = userRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found for email: " + userEmail));
            logDto.setUserId((long) user.getUserId());
            logDto.setTimeStamp(LocalDateTime.now());
            logFileServiceImpl.saveManualError(logDto);
            return ResponseEntity.ok("Manual error added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add error: " + e.getMessage());

        }
    }
}

