package com.yash.log.controller;


import com.yash.log.constants.LogConstant;
import com.yash.log.entity.Log;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.services.LogFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Tag(
        name = "Error Log Management APIs",
        description = "APIs for managing and analyzing error logs"
)
@RestController
@RequestMapping("/api/errors")
public class LogController {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Autowired
    private LogFileService logFileService;

    @Operation(
            summary = "Get Error Statistics",
            description = "Retrieve statistics of error logs grouped by error level"

    )
    @GetMapping("/stats")
    public Map<String,Long> getErrorStats(){
        List<Object[]> results = errorLogRepository.countErrorsByLevel();
        Map<String, Long> stats= new HashMap<>();
        for (Object[] row: results){
            stats.put((String) row[0],(Long) row[1] );
        }
        return stats;
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
            if (!file.getOriginalFilename().endsWith(".log")){
                throw new IllegalArgumentException(LogConstant.INVALID_FILE_TYPE);
            }

            // Validate size (e.g. max 20MB)
            if (file.getSize() > 20 * 1024 * 1024){
                throw new IllegalArgumentException(LogConstant.FILE_TOO_LARGE);
            }

         logFileService.parseAndSaveLogs(file);
            return ResponseEntity.ok(LogConstant.UPLOAD_SUCCESSFULLY);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get Chart Data",
            description = "Retrieve error log data formatted for chart visualization"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP status OK"
    )
    @GetMapping("/chart-data")
    public List<Map<String, Object>> getChartData() {
        List<Object[]> results = errorLogRepository.countErrorsByLevel();
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", row[0]);
            data.put("value", row[1]);
            chartData.add(data);
        }
        return chartData;
    }


    @GetMapping("/error-type")
    public Map<String,Long> getErrorByType(){
        List<Object[]> results = logFileService.countByErrorType() ;
        Map<String, Long> stats= new HashMap<>();
        for (Object[] row: results){
            stats.put((String) row[0],(Long) row[1] );
        }
        return stats;
    }


    @Operation(
            summary = "All all Logs",
            description = "Get all logs from the database"

    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP status OK"
    )
    @GetMapping("/all-logs")
    public ResponseEntity<List<Log>> getAllLogs(){
        List<Log> all = logFileService.getAllLogs();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(all);
    }

}
