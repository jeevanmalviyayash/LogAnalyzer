package com.yash.log.serviceImpl;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yash.log.entity.Log;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogFileServiceImplTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private LogFileServiceImpl logService; // Your service class containing parseAndSaveLogs

    @Test
    void testParseAndSaveLogs() throws Exception {
        // Prepare a MockMultipartFile with your sample log content
        String logContent = "2025-11-17T16:23:35.059+05:30  INFO 17460 --- [LOG] [  restartedMain] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)\n";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "log.txt",
                "text/plain",
                logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Call the method under test
        logService.parseAndSaveLogs(multipartFile);

        // Verify save is called on the repository
        verify(errorLogRepository, times(1)).save(any(Log.class));
    }


    // Helper method to build Log object
    private Log buildLog(String type, LocalDateTime createdAt) {
        Log log = new Log();
        log.setErrorType(type);
        log.setCreatedAt(createdAt);
        return log;
    }

    @Test
    void testGetAllLogs_ReturnsAllLogs() {
        Log log1 = buildLog("DB", LocalDateTime.now());
        Log log2 = buildLog("NETWORK", LocalDateTime.now());

        when(errorLogRepository.findAll()).thenReturn(List.of(log1, log2));

        var result = logService.getAllLogs();

        assertEquals(2, result.size());
        verify(errorLogRepository).findAll();
    }

    @Test
    void testGetLogsLastNDays_ReturnsCorrectLogs() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7);

        Log log1 = buildLog("ERROR", now.minusDays(1));
        Log log2 = buildLog("INFO", now.minusDays(2));

        when(errorLogRepository.findByTimeStampBetweenOrderByTimeStampDesc(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(log1, log2));

        var result = logService.getLogsLastNDays(7);

        assertEquals(2, result.size());
        verify(errorLogRepository).findByTimeStampBetweenOrderByTimeStampDesc(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );

    }

    @Test
    void testGetDailyErrorCounts_ReturnsCorrectDtos() {
        LocalDate today = LocalDate.now();

        Object[] row1 = { Date.valueOf(today.minusDays(1)), 5L };
        Object[] row2 = { Date.valueOf(today.minusDays(2)), 3L };

        when(errorLogRepository.countByDayBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(row1, row2));

        var result = logService.getDailyErrorCounts(10);

        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getCount());
        assertEquals(today.minusDays(1), result.get(0).getDate());

        verify(errorLogRepository).countByDayBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }


    @Test
    void testGetErrorCategoryStats_ReturnsNormalizedCategories() {

        Object[] row1 = {"database-transaction-error", 4L};
        Object[] row2 = {"NullPointerException", 3L};

        when(errorLogRepository.countByerrorTypeBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(row1, row2));

        var result = logService.getErrorCategoryStats(30);

        assertEquals(2, result.size());

        assertEquals("Database Transaction Error", result.get(0).getCategory());
        assertEquals(4L, result.get(0).getCount());

        assertEquals("Null Pointer Error", result.get(1).getCategory());
        assertEquals(3L, result.get(1).getCount());

        verify(errorLogRepository).countByerrorTypeBetween(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

}
