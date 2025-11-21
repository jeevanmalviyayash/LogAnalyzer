package com.yash.log.service.services;

import com.yash.log.entity.LogEntry;
import com.yash.log.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private final LogRepository repo;

    public LogService(LogRepository repo) {
        this.repo = repo;
    }

    public List<LogEntry> filterLogs(String search, String level,
                                     LocalDateTime startDate, LocalDateTime endDate) {

        return repo.findAll().stream()
                .filter(log ->
                        // Search text filter
                        (search == null || search.isBlank()
                                || log.getTitle().toLowerCase().contains(search.toLowerCase())
                                || log.getDescription().toLowerCase().contains(search.toLowerCase()))
                )
                .filter(log ->
                        // Level filter
                        (level == null || level.isBlank()
                                || log.getLevel().equalsIgnoreCase(level))
                )
                .filter(log ->
                        // Start date filter
                        (startDate == null || !log.getTimestamp().isBefore(startDate))
                )
                .filter(log ->
                        // End date filter
                        (endDate == null || !log.getTimestamp().isAfter(endDate))
                )
                .toList();
    }
}
