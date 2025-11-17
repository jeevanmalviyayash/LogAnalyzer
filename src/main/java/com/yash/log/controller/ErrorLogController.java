package com.yash.log.controller;

import com.yash.log.entity.ErrorLog;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.services.LogFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorLogController {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Autowired
    private LogFileService logFileService;

    @GetMapping("/stats")
    public Map<String,Long> getErrorStats(){
        List<Object[]> results = errorLogRepository.countErrorsByLevel();
        Map<String, Long> stats= new HashMap<>();
        for (Object[] row: results){
            stats.put((String) row[0],(Long) row[1] );
        }
        return stats;
    }




    @PostMapping("/upload")
    public ResponseEntity<String> uploadLogFile(@RequestParam("file") MultipartFile file) {
        try {

            //validate file type
            if (!file.getOriginalFilename().endsWith(".log")){
                throw new IllegalArgumentException("Invalid file type. Only .log files are accepted.");
            }

            // Valudate size (e.g. max 10MB)
            if (file.getSize() > 10 * 1024 * 1024){
                throw new IllegalArgumentException("File too large. Max size is 10MB");
            }

         logFileService.parseAndSaveLogs(file);
            return ResponseEntity.ok("Logs uploaded and saved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }


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

}
