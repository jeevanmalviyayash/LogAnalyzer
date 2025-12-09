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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".log", ".txt");

    private final LogFileServiceImpl logFileServiceImpl;
    private final IUserRepository userRepository;
    public ErrorLogController(LogFileServiceImpl logFileServiceImpl, IUserRepository userRepository) {
        this.logFileServiceImpl = logFileServiceImpl;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Upload Log File",
            description = "Upload a log file to parse and store error logs"
    )


    @ApiResponse(responseCode = "200", description = "HTTP status OK")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLogFile(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file);

            String filename = file.getOriginalFilename();
            log.info("Uploading log file: {} (size: {} bytes)", filename, file.getSize());

            logFileServiceImpl.parseAndSaveLogs(file);

            log.info("File processed and logs saved: {}", filename);
            return ResponseEntity.ok("Logs uploaded and saved successfully!");
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping(value = "/all-logs", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<List<Log>> getAllLogs(){
       List<Log> allLogs = logFileServiceImpl.getAllLogs();

       return ResponseEntity
               .status(HttpStatus.OK)
               .body(allLogs);
   }

    //for the table
    @GetMapping
    public List<Log> getAll(
            @RequestParam(required = false) Integer lastDays
    ) {
        if (lastDays != null) {
            return logFileServiceImpl.getLogsLastNDays(lastDays);
        }
        return logFileServiceImpl.getAllLogs();
    }

    // Bar chart: date vs error count
    @GetMapping("/daily-counts")
    public List<DailyErrorCountDto> getDailyCounts(
            @RequestParam(defaultValue = "10") int lastDays
    ) {
        return logFileServiceImpl.getDailyErrorCounts(lastDays);
    }

    // Pie chart: error categorywise
    @GetMapping("/category-stats")
    public List<ErrorCategoryStatDto> getCategoryStats(
            @RequestParam(defaultValue = "30") int lastDays
    ) {
        return logFileServiceImpl.getErrorCategoryStats(lastDays);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided or file is empty.");
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename is missing.");
        }

        String lowered = filename.toLowerCase(Locale.ROOT);
        boolean allowed = ALLOWED_EXTENSIONS.stream().anyMatch(lowered::endsWith);
        if (!allowed) {
            throw new IllegalArgumentException("Invalid file type. Only .log and .txt files are accepted.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large. Max size is 10MB");
        }
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
