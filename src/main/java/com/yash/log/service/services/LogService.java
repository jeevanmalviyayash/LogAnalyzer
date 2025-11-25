package com.yash.log.service.services;
import com.yash.log.entity.Log;
import com.yash.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class LogService {

    private final LogRepository repo;

    public LogService(LogRepository repo) {
        this.repo = repo;
    }

    public List<Log> filterLogs(String search, String level,
                                LocalDateTime startDate, LocalDateTime endDate) {

        return repo.findAll().stream()

                // Search filter (null safe)
                .filter(log ->
                        search == null || search.isBlank()
                                || Objects.toString(log.getTitle(), "")
                                .toLowerCase().contains(search.toLowerCase())
                                || Objects.toString(log.getErrorMessage(), "")
                                .toLowerCase().contains(search.toLowerCase())
                )

                // Level filter
                .filter(log ->
                        level == null || level.isBlank()
                                || Objects.toString(log.getErrorLevel(), "")
                                .equalsIgnoreCase(level)
                )

                // Start date filter
                .filter(log ->
                        startDate == null
                                || (log.getTimeStamp() != null
                                && !log.getTimeStamp().isBefore(startDate))
                )

                // End date filter
                .filter(log ->
                        endDate == null
                                || (log.getTimeStamp() != null
                                && !log.getTimeStamp().isAfter(endDate))
                )

                .toList();
    }
}
