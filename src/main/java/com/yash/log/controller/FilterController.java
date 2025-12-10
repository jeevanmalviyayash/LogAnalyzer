//package com.yash.log.controller;
//
//import com.yash.log.entity.Log;
//import com.yash.log.service.services.LogService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@RestController
//@CrossOrigin("*")
//@RequestMapping("/api/logs")
//public class FilterController {
//
//    private final LogService service;
//
//    public FilterController(LogService service) {
//        this.service = service;
//    }
//
//    @GetMapping
//    public List<Log> getLogs(
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) String startDate,
//            @RequestParam(required = false) String endDate
//    ) {
//        LocalDateTime start = null;
//        LocalDateTime end = null;
//
//        try {
//            start = parseDateParam(startDate, false);
//            end = parseDateParam(endDate, true);
//        } catch (Exception e) {
//           log.info("Invalid date format: " + e.getMessage());
//        }
//
//        log.info("FilterController.getLogs called with search=" + search + ", start=" + start + ", end=" + end);
//
//        return service.filterLogs(search, start, end);
//    }
//    private static LocalDateTime parseDateParam(String dateStr, boolean endOfDay) {
//        if (!StringUtils.hasText(dateStr)) {
//            return null;
//        }
//        LocalDate date = LocalDate.parse(dateStr);
//        return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
//    }
//}
