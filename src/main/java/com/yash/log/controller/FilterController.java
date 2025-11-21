package com.yash.log.controller;

import com.yash.log.entity.LogEntry;
import com.yash.log.service.services.LogService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/logs")
public class FilterController {

    private final LogService service;

    public FilterController(LogService service) {
        this.service = service;
    }

    @GetMapping
    public List<LogEntry> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = (startDate != null && !startDate.isBlank())
                ? LocalDateTime.parse(startDate + "T00:00:00") : null;

        LocalDateTime end = (endDate != null && !endDate.isBlank())
                ? LocalDateTime.parse(endDate + "T23:59:59") : null;

        return service.filterLogs(search, level, start, end);
    }
}
