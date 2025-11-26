package com.yash.log.controller;

import com.yash.log.entity.Log;
import com.yash.log.service.services.LogService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public List<Log> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (startDate != null && !startDate.isBlank()) {
                start = LocalDate.parse(startDate).atStartOfDay();
            }

            if (endDate != null && !endDate.isBlank()) {
                end = LocalDate.parse(endDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            System.out.println("Invalid date format: " + e.getMessage());
        }
  System.out.println("FilterController.getLogs called with search=" + search + ", start=" + start + ", end=" + end);
        return service.filterLogs(search, start, end);
    }

}
