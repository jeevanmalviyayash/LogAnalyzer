package com.yash.log.service.impl;

import com.yash.log.entity.Log;
import com.yash.log.repository.LogRepository;
import com.yash.log.service.services.LogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class LogServiceImpl implements LogService {

    private final LogRepository repo;

    public LogServiceImpl(LogRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Log> filterLogs(String search,
                                LocalDateTime startDate,
                                LocalDateTime endDate) {

        return repo.findAll().stream()

                // SEARCH FILTER — only by errorType
                .filter(log ->
                        search == null || search.isBlank() ||
                                Objects.toString(log.getErrorType(), "")
                                        .toLowerCase()
                                        .contains(search.toLowerCase())
                )

                // START DATE FILTER
                .filter(log ->
                        startDate == null ||
                                (log.getCreatedAt() != null &&
                                        !log.getCreatedAt().isBefore(startDate))
                )

                // END DATE FILTER
                .filter(log ->
                        endDate == null ||
                                (log.getCreatedAt() != null &&
                                        !log.getCreatedAt().isAfter(endDate))
                )

                .toList();
    }
}
