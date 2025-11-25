package com.yash.log.controller;

import com.yash.log.entity.Log;
import com.yash.log.service.impl.LogFileServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.yash.log.dto.ErrorCategoryStatDto;
import com.yash.log.dto.DailyErrorCountDto;


@Slf4j
@Tag(
        name = "Error Log Management APIs",
        description = "APIs for managing and analyzing error logs"
)

@RestController
@RequestMapping("/api/errors")
@CrossOrigin(origins = "http://localhost:3000")
public class ErrorLogController {

    private final LogFileServiceImpl logFileServiceImpl;

    public ErrorLogController(LogFileServiceImpl logFileServiceImpl) {
        this.logFileServiceImpl = logFileServiceImpl;
    }

    //Table ke liye
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
}
