package com.yash.log.serviceImpl;

import com.yash.log.entity.Log;
import com.yash.log.repository.LogRepository;
import com.yash.log.service.impl.LogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @Mock
    private LogRepository repo;

    @InjectMocks
    private LogServiceImpl service;

    // helper method
    private Log buildLog(String type, LocalDateTime createdAt) {
        Log log = new Log();
        log.setErrorType(type);
        log.setCreatedAt(createdAt);
        return log;
    }

    @Test
    void testReturnsAllLogsWhenNoFilterApplied() {
        Log log1 = buildLog("DB", LocalDateTime.now());
        Log log2 = buildLog("NETWORK", LocalDateTime.now());

        when(repo.findAll()).thenReturn(List.of(log1, log2));

        List<Log> result = service.filterLogs(null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void testFilterBySearch() {
        Log log1 = buildLog("DATABASE", LocalDateTime.now());
        Log log2 = buildLog("NETWORK", LocalDateTime.now());

        when(repo.findAll()).thenReturn(List.of(log1, log2));

        List<Log> result = service.filterLogs("data", null, null);

        assertEquals(1, result.size());
        assertEquals("DATABASE", result.get(0).getErrorType());
    }

    @Test
    void testFilterByStartDate() {
        LocalDateTime now = LocalDateTime.now();

        Log oldLog = buildLog("OLD", now.minusDays(5));
        Log newLog = buildLog("NEW", now.minusHours(1));

        when(repo.findAll()).thenReturn(List.of(oldLog, newLog));

        List<Log> result = service.filterLogs(null, now.minusDays(1), null);

        assertEquals(1, result.size());
        assertEquals("NEW", result.get(0).getErrorType());
    }
}
