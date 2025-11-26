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
    public List<Log> filterLogs(String search,
                                LocalDateTime startDate, LocalDateTime endDate) {
        return repo.findAll().stream()

                // Search filter (null safe)
                .filter(log ->
                        search == null || search.isBlank()
                                || Objects.toString(log.getErrorType(), "")
                                .toLowerCase().contains(search.toLowerCase())
                                || Objects.toString(log.getErrorMessage(), "")
                                .toLowerCase().contains(search.toLowerCase())
                )

                //                 Level filter
                //                .filter(log ->
                //                        level == null || level.isBlank()
                //                                || Objects.toString(log.getErrorLevel(), "")
                //                                .equalsIgnoreCase(level)
                //                )


                .filter(log ->
                        startDate == null
                                || (log.getCreatedAt() != null
                                && !log.getCreatedAt().isBefore(startDate))
                )


                .filter(log ->
                        endDate == null
                                || (log.getCreatedAt() != null
                                && !log.getCreatedAt().isAfter(endDate))
                )

                .toList();
    }

}
