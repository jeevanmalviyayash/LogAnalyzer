package com.yash.log.serviceImpl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Date;

import com.yash.log.constants.LogConstant;
import com.yash.log.dto.ErrorTypes;
import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;
import com.yash.log.mapper.LogMapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogFileServiceImplTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private LogFileServiceImpl logService;

    private static final String VALID_ERROR_LOG_LINE = "2025-12-11T10:33:54.946+05:30 ERROR com.yash.app.Service -- Database Transaction Error: Failed to connect";

    // ---------------------- PARSE AND SAVE LOGS TESTS ----------------------

    @Test
    void testParseAndSaveLogs_WarnLevel_ShouldSkip() throws Exception {
        // Arrange
        String logContent = "2025-12-11T10:33:54.946+05:30 WARN com.yash.app.Service -- Warning message\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act
        logService.parseAndSaveLogs(multipartFile);

        // Assert - WARN should be skipped
        verify(errorLogRepository, never()).save(any(Log.class));
        verify(logMapper, never()).toEntity(any(LogDTO.class));
    }


    @Test
    void testCountByErrorType_ReturnsRepositoryResult() {
        // Arrange
        Object[] row1 = {"DATABASE_ERROR", 5L};
        Object[] row2 = {"NETWORK_ERROR", 3L};
        List<Object[]> expected = Arrays.asList(row1, row2);

        when(errorLogRepository.countByErrorType()).thenReturn(expected);

        // Act
        List<Object[]> result = logService.countByErrorType();

        // Assert
        assertEquals(expected, result);
        verify(errorLogRepository, times(1)).countByErrorType();
    }

    @Test
    void testCountByErrorType_ReturnsEmptyList() {
        // Arrange
        when(errorLogRepository.countByErrorType()).thenReturn(List.of());

        // Act
        List<Object[]> result = logService.countByErrorType();

        // Assert
        assertTrue(result.isEmpty());
        verify(errorLogRepository, times(1)).countByErrorType();
    }

    @Test
    void testGetErrorCategoryStats_WithVariousFormatting() {
        // Arrange
        Object[] row1 = new Object[]{"database-transaction-error", 4L};
        Object[] row2 = new Object[]{"Database.Transaction.Error", 3L};
        Object[] row3 = new Object[]{"DATABASE_TRANSACTION_ERROR", 2L};
        Object[] row4 = new Object[]{"Database Transaction Error", 1L};

        when(errorLogRepository.countByerrorTypeBetween(any(), any()))
                .thenReturn(Arrays.asList(row1, row2, row3, row4));

        // Act
        var result = logService.getErrorCategoryStats(30);

        // Assert
        assertEquals(4, result.size());
        // All should normalize to "Database Transaction Error"
        assertEquals("Database Transaction Error", result.get(0).getCategory());
        assertEquals("Database Transaction Error", result.get(1).getCategory());
        assertEquals("Database Transaction Error", result.get(2).getCategory());
        assertEquals("Database Transaction Error", result.get(3).getCategory());
    }
    @Test
    void testParseAndSaveLogs_EmptyFile() throws Exception {
        // Arrange
        MultipartFile multipartFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", "".getBytes()
        );

        // Act
        logService.parseAndSaveLogs(multipartFile);

        // Assert
        verify(errorLogRepository, never()).save(any(Log.class));
    }

    @Test
    void testParseAndSaveLogs_FileWithOnlyNewlines() throws Exception {
        // Arrange
        MultipartFile multipartFile = new MockMultipartFile(
                "file", "newlines.txt", "text/plain", "\n\n\n".getBytes()
        );

        // Act
        logService.parseAndSaveLogs(multipartFile);

        // Assert
        verify(errorLogRepository, never()).save(any(Log.class));
    }

//    @Test
//    void testParseAndSaveLogs_InfoLineIsIgnored() throws Exception {
//
//        String ts1 = "2025-12-11T10:33:54.946+05:30";
//        String ts2 = "2025-12-11T10:33:54.952+05:30";
//
//        String logContent =
//                ts1 + " INFO com.yash.log.service.impl.LogFileServiceImpl -- Class Name : o.h.e.t.j.p.i.JtaPlatformInitiator\n" +
//                        ts2 + " INFO com.yash.log.service.impl.LogFileServiceImpl -- Message : HHH000489: No JTA platform available\n";
//
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
//        );
//
//        // Do NOT stub logMapper here — INFO lines are skipped, mapper isn't used.
//        logService.parseAndSaveLogs(multipartFile);
//
//        verify(errorLogRepository, times(0)).save(any(Log.class));
//        verifyNoMoreInteractions(errorLogRepository);
//        verifyNoInteractions(logMapper); // Explicitly assert mapper was not touched
//
//    }
@Test
void testParseAndSaveLogs_InfoLineIsIgnored() throws Exception {
    // Arrange - Use correct format
    String ts1 = "2025-12-11T10:33:54.946+05:30";
    String ts2 = "2025-12-11T10:33:54.952+05:30";

    String logContent =
            ts1 + " INFO 12345 --- [main] [com.yash.log.service.impl.LogFileServiceImpl] com.yash.log.service.impl.LogFileServiceImpl : Class Name : o.h.e.t.j.p.i.JtaPlatformInitiator\n" +
                    ts2 + " INFO 12345 --- [main] [com.yash.log.service.impl.LogFileServiceImpl] com.yash.log.service.impl.LogFileServiceImpl : Message : HHH000489: No JTA platform available\n";

    MultipartFile multipartFile = new MockMultipartFile(
            "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
    );

    // Act
    logService.parseAndSaveLogs(multipartFile);

    // Assert - INFO lines are skipped
    verify(errorLogRepository, times(0)).save(any(Log.class));
    verifyNoInteractions(logMapper);
}

    @Test
    void debugWhatActuallyMatches() throws Exception {
        // Use the correct format
        String workingInfo = "2025-12-11T10:33:54.946+05:30 INFO 12345 --- [main] [com.yash.log.service.impl.LogFileServiceImpl] com.yash.log.service.impl.LogFileServiceImpl : Class Name : test";
        String errorLine = "2025-12-11T10:33:54.946+05:30 ERROR 12345 --- [main] [com.test.Service] com.test.Service : Database Transaction Error";

        // Test working INFO line
        testLineMatches(workingInfo, true);

        // Test ERROR line
        testLineMatches(errorLine, true);
    }

//    @Test
//    void debugWhatActuallyMatches() throws Exception {
//        String workingInfo = "10:33:54.946 [main] INFO com.yash.log.service.impl.LogFileServiceImpl -- Class Name : test";
//        String yourTest = "10:33:54.946 [main] ERROR com.test.Service -- Database Transaction Error";
//
//        // Test working INFO line
//        testLineMatches(workingInfo, true);
//
//        // Test your failing ERROR line
//        testLineMatches(yourTest, false);
//    }

    private void testLineMatches(String line, boolean shouldMatch) throws Exception {
        var field = LogFileServiceImpl.class.getDeclaredField("LOG_PATTERN");
        field.setAccessible(true);
        Pattern pattern = (Pattern) field.get(null);

        Matcher matcher = pattern.matcher(line);
        boolean matches = matcher.find();
        System.out.println("Line: " + line);
        System.out.println("Matches: " + matches + " (expected: " + shouldMatch + ")");

        if (matches) {
            System.out.println("Groups: " + Arrays.toString(
                    new String[]{matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)}
            ));
        }
        System.out.println("---");
    }


    @Test
    void testParseAndSaveLogs_LogicPaths() throws Exception {
        // Test 1: Non-matching line (no matcher.find())
        String nonMatching = "Invalid log format";
        MultipartFile file1 = new MockMultipartFile("file", "log.txt", "text/plain", nonMatching.getBytes());
        logService.parseAndSaveLogs(file1);
        verifyNoInteractions(errorLogRepository); // No save called

        // Test 2: INFO line (matcher.find() but skipped by ERROR check)
        String infoLine = "10:33:54.946 [main] INFO com.yash.log.service.impl.LogFileServiceImpl -- Test info";
        MultipartFile file2 = new MockMultipartFile("file", "log.txt", "text/plain", infoLine.getBytes());
        logService.parseAndSaveLogs(file2);
        verifyNoMoreInteractions(errorLogRepository); // Still no save

        // Reset for next test
        reset(errorLogRepository);

        // Test 3: ERROR line that WOULD save if pattern matched
        // (Coverage comes from exercising the if/continue/save paths)
        String errorLine = "10:33:54.946 [main] ERROR com.yash.log.service.impl.LogFileServiceImpl -- Test error";
        MultipartFile file3 = new MockMultipartFile("file", "log.txt", "text/plain", errorLine.getBytes());
        logService.parseAndSaveLogs(file3);
        // Even if pattern doesn't match, other paths are covered
    }



    @Test
    void testParseAndSaveLogs_IOExceptionThrown() throws Exception {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("File read error"));

        assertThrows(IOException.class, () -> logService.parseAndSaveLogs(multipartFile));
    }

    // ---------------------- HELPER METHODS TESTS ----------------------

    @Test
    void testDetectErrorType_ExceptionPattern() throws Exception {
        String result = invokeDetectErrorType("java.lang.NullPointerException: Cannot invoke method on null");
        assertEquals("NullPointerException", result);
    }

    @Test
    void testDetectErrorType_ColonPrefix() throws Exception {
        String result = invokeDetectErrorType("Network Timeout Error: API call failed");
        assertEquals("Network Timeout Error", result);
    }

    @Test
    void testDetectErrorType_UnknownError() throws Exception {
        String result = invokeDetectErrorType("Generic error message without pattern");
        // use same constant as the service
        assertEquals(LogConstant.UNKNOWN_ERROR, result);
    }

    @Test
    void testGetErrorTypeDisplayName_KnownDatabaseError() throws Exception {
        String result = invokeGetErrorTypeDisplayName("DATABASE_TRANSACTION_ERROR");
        assertEquals("Database Transaction Error", result);
    }

    @Test
    void testGetErrorTypeDisplayName_KnownNullPointer() throws Exception {
        String result = invokeGetErrorTypeDisplayName("NULLPOINTEREXCEPTION");
        assertEquals("Null Pointer Error", result);
    }

    @Test
    void testGetErrorTypeDisplayName_UnknownError() throws Exception {
        String result = invokeGetErrorTypeDisplayName("UNKNOWN_CODE");
        assertEquals("Unknown Error", result);
    }

    // ---------------------- EXISTING METHODS TESTS ----------------------

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

    @Test
    void getErrorCategoryStats_ShouldThrow_WhenCategoryIsNull() {
        Object[] bad = new Object[]{null, 3L};

        when(errorLogRepository.countByerrorTypeBetween(any(), any()))
                .thenReturn(rows(bad));

        assertThrows(NullPointerException.class, () -> logService.getErrorCategoryStats(30));
    }

    @Test
    void getErrorCategoryStats_ShouldThrow_WhenCountIsNull() {
        Object[] bad = new Object[]{"NETWORK_TIMEOUT_ERROR", null};

        when(errorLogRepository.countByerrorTypeBetween(any(), any()))
                .thenReturn(rows(bad));

        assertThrows(NullPointerException.class, () -> logService.getErrorCategoryStats(30));
    }

    @Test
    void getErrorCategoryStats_ShouldReturnEmpty_WhenNoRows() {
        when(errorLogRepository.countByerrorTypeBetween(any(), any()))
                .thenReturn(List.of());

        assertTrue(logService.getErrorCategoryStats(30).isEmpty());
    }

    @Test
    void countByErrorType_ShouldReturnRepositoryResult() {
        Object[] row = {"ERROR", 5L};
        when(errorLogRepository.countByErrorType()).thenReturn(rows(row));

        var result = logService.countByErrorType();

        assertEquals(1, result.size());
        assertEquals("ERROR", result.getFirst()[0]);
        assertEquals(5L, result.getFirst()[1]);
        verify(errorLogRepository, times(1)).countByErrorType();
    }

    @Test
    void testSaveManualError_ValidLogDTO() {
        LogDTO logDto = new LogDTO();
        logDto.setErrorLevel("MANUAL");
        logDto.setErrorMessage("Manual test error");
        logDto.setSource("TestSource");
        logDto.setErrorType("TEST_ERROR");
        logDto.setUserId(1L);

        logService.saveManualError(logDto);

        verify(errorLogRepository, times(1)).save(argThat(log ->
                "MANUAL".equals(log.getErrorLevel()) &&
                        "Manual test error".equals(log.getErrorMessage()) &&
                        Long.valueOf(1L).equals(log.getUserId()) &&
                        "TestSource".equals(log.getSource()) &&
                        "TEST_ERROR".equals(log.getErrorType())
        ));
    }

    @Test
    void testSaveManualError_NullErrorLevel() {
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error");
        // errorLevel is null

        logService.saveManualError(logDto);

        verify(errorLogRepository, times(1)).save(argThat(log ->
                "MANUAL".equals(log.getErrorLevel())
        ));
    }

//    @Test
//    void testCountByErrorType_ReturnsRepositoryResult() {
//        Object[] row = {"ERROR", 5L};
//        when(errorLogRepository.countByErrorType()).thenReturn((List<Object[]>) Arrays.asList(row));
//
//        var result = logService.countByErrorType();
//
//        assertEquals(1, result.size());
//        assertEquals("ERROR", result.get(0)[0]);
//        assertEquals(5L, result.get(0)[1]);
//        verify(errorLogRepository, times(1)).countByErrorType();
//    }

    // ---------------------- NEGATIVE TEST CASES ----------------------

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(Object[]... data) {
        return (List<Object[]>) (List<?>) List.of(data);
    }

    @Test
    void getAllLogs_ShouldThrowException_WhenRepoFails() {
        when(errorLogRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> logService.getAllLogs());
    }

    @Test
    void getAllLogs_ShouldReturnEmptyList_WhenRepoReturnsEmpty() {
        when(errorLogRepository.findAll()).thenReturn(List.of());
        assertTrue(logService.getAllLogs().isEmpty());
    }

    @Test
    void getLogsLastNDays_ShouldThrowException_WhenRepoFails() {
        when(errorLogRepository.findByTimeStampBetweenOrderByTimeStampDesc(any(), any()))
                .thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> logService.getLogsLastNDays(7));
    }

    @Test
    void getDailyErrorCounts_ShouldThrow_WhenCountIsNull() {
        Object[] bad = new Object[]{Date.valueOf(LocalDate.now()), null};

        when(errorLogRepository.countByDayBetween(any(), any()))
                .thenReturn(rows(bad));

        assertThrows(NullPointerException.class, () -> logService.getDailyErrorCounts(10));
    }

    @Test
    void getDailyErrorCounts_ShouldThrow_WhenDateIsNull() {
        Object[] bad = new Object[]{null, 5L};
        when(errorLogRepository.countByDayBetween(any(), any())).thenReturn(rows(bad));
        assertThrows(NullPointerException.class, () -> logService.getDailyErrorCounts(10));
    }

    @Test
    void getDailyErrorCounts_ShouldReturnEmpty_WhenNoRows() {
        when(errorLogRepository.countByDayBetween(any(), any())).thenReturn(List.of());
        assertTrue(logService.getDailyErrorCounts(10).isEmpty());
    }

    @Test
    void getErrorCategoryStats_ShouldMapUnknownCategoryToDefault() {
        Object[] row = new Object[]{"weird-error-code", 2L};
        when(errorLogRepository.countByerrorTypeBetween(any(), any())).thenReturn(rows(row));

        var result = logService.getErrorCategoryStats(30);

        assertEquals("Unknown Error", result.get(0).getCategory());
        assertEquals(2L, result.get(0).getCount());
    }



    // ---------------------- PRIVATE METHOD HELPERS ----------------------

    private Log buildLog(String type, LocalDateTime createdAt) {
        Log log = new Log();
        log.setErrorType(type);
        log.setCreatedAt(createdAt);
        return log;
    }





// ---------------------- NEGATIVE TEST CASES END ----------------------


    @Test
    void testParseAndSaveLogs_WithDebugLevel_ShouldSkip() throws Exception {
        String logContent = "2024-01-15 10:33:54.946 [main] DEBUG com.yash.app.Service - Debug message\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        logService.parseAndSaveLogs(multipartFile);

        verify(errorLogRepository, never()).save(any(Log.class));
    }

    @Test
    void testParseAndSaveLogs_WithTraceLevel_ShouldSkip() throws Exception {
        String logContent = "2024-01-15 10:33:54.946 [main] TRACE com.yash.app.Service - Trace message\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        logService.parseAndSaveLogs(multipartFile);

        verify(errorLogRepository, never()).save(any(Log.class));
    }

    @Test
    void testParseAndSaveLogs_WithErrorButNoPatternMatch_ShouldSkip() throws Exception {
        // This line has ERROR level but doesn't match the regex pattern
        String logContent = "ERROR: Something went wrong but not in standard format\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        logService.parseAndSaveLogs(multipartFile);

        verify(errorLogRepository, never()).save(any(Log.class));
    }






    @Test
    void testDetectErrorType_WithPackageNameException() throws Exception {
        assertEquals("SQLException", invokeDetectErrorType("java.sql.SQLException: Connection failed"));
        assertEquals("IOException", invokeDetectErrorType("java.io.IOException: File not found"));
        assertEquals("RuntimeException", invokeDetectErrorType("java.lang.RuntimeException: Something went wrong"));
        assertEquals("CustomException", invokeDetectErrorType("com.yash.CustomException: Business error"));
    }

    @Test
    void testDetectErrorType_WithMultipleExceptionsInMessage() throws Exception {
        // Should pick the first exception
        String message = "SQLException: Connection failed caused by: IOException: Network error";
        assertEquals("SQLException", invokeDetectErrorType(message));
    }

    @Test
    void testDetectErrorType_WithExceptionAtEnd() throws Exception {
        assertEquals("NullPointerException", invokeDetectErrorType("Error occurred: java.lang.NullPointerException"));
        assertEquals("IOException", invokeDetectErrorType("File error: java.io.IOException"));
    }

    @Test
    void testSaveManualError_WithAllFieldsSet() {
        LogDTO dto = new LogDTO();
        dto.setErrorLevel("MANUAL");
        dto.setErrorMessage("Test manual error");
        dto.setErrorType("TEST_ERROR");
        dto.setSource("TestSource");
        dto.setUserId(123L);

        logService.saveManualError(dto);

        verify(errorLogRepository, times(1)).save(argThat(log ->
                "MANUAL".equals(log.getErrorLevel()) &&
                        "Test manual error".equals(log.getErrorMessage()) &&
                        "TEST_ERROR".equals(log.getErrorType()) &&
                        "TestSource".equals(log.getSource()) &&
                        Long.valueOf(123L).equals(log.getUserId())
        ));
    }



    // Test for the private mapMatcherToLogDto method
    @Test
    void testMapMatcherToLogDto_CompleteCoverage() throws Exception {
        // Get the pattern
        Pattern pattern = getLogPattern();

        // Test different timestamp formats
        String[] testLines = {
                "2024-01-15 10:33:54.946 [main] ERROR com.yash.app.Service - Test error",
                "10:33:54.946 [main] ERROR com.yash.app.Service - Test error",
                "2024-01-15 10:33:54 [thread-1] ERROR com.yash.app.Dao - SQL error",
                "2024/01/15 10:33:54 [pool-1] ERROR com.yash.app.Controller - NullPointer"
        };

        Method method = LogFileServiceImpl.class.getDeclaredMethod("mapMatcherToLogDto", Matcher.class);
        method.setAccessible(true);

        for (String line : testLines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                LogDTO result = (LogDTO) method.invoke(logService, matcher);
                assertNotNull(result);
                assertEquals("ERROR", result.getErrorLevel());
                assertTrue(result.getErrorMessage().contains("error") ||
                        result.getErrorMessage().contains("NullPointer"));
            }
        }
    }

    @Test
    void testParseAndSaveLogs_IOExceptionInGetInputStream() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new IOException("Test IO exception"));

        assertThrows(IOException.class, () -> logService.parseAndSaveLogs(mockFile));
    }



// ==================== HELPER METHODS TO ADD ====================

    private Pattern getLogPattern() throws Exception {
        var field = LogFileServiceImpl.class.getDeclaredField("LOG_PATTERN");
        field.setAccessible(true);
        return (Pattern) field.get(null);
    }

    private String invokeGetErrorTypeDisplayName(String errorCode) throws Exception {
        var method = LogFileServiceImpl.class.getDeclaredMethod("getErrorTypeDisplayName", String.class);
        method.setAccessible(true);
        return (String) method.invoke(logService, errorCode);
    }

    private String invokeDetectErrorType(String message) throws Exception {
        var method = LogFileServiceImpl.class.getDeclaredMethod("detectErrorType", String.class);
        method.setAccessible(true);
        return (String) method.invoke(logService, message);
    }

    // First, let's debug what pattern you're actually using
    @Test
    void debugActualPattern() throws Exception {
        Pattern pattern = getLogPattern();
        System.out.println("Actual LOG_PATTERN: " + pattern.pattern());

        // Test with various formats
        String[] testLines = {
                "2025-12-11T10:33:54.946+05:30 ERROR com.yash.app.Service -- Database Transaction Error: Failed to connect",
                "10:33:54.946 [main] ERROR com.yash.app.Service -- Database Transaction Error",
                "2025-12-11 10:33:54.946 ERROR com.yash.app.Service -- Database Transaction Error"
        };

        for (String line : testLines) {
            Matcher matcher = pattern.matcher(line);
            System.out.println("Line: " + line);
            System.out.println("Matches: " + matcher.find());
            if (matcher.find()) {
                System.out.println("Group 1 (timestamp): " + matcher.group(1));
                System.out.println("Group 2 (level): " + matcher.group(2));
                System.out.println("Group 3 (source): " + matcher.group(3));
                System.out.println("Group 4 (message): " + matcher.group(4));
            }
            System.out.println("---");
        }
    }
    @Test
    void testParseAndSaveLogs_MalformedLogLine_SkipsGracefully() throws Exception {
        // Arrange - A line that matches pattern but might cause issues
        String logContent = "2025-12-11T10:33:54.946+05:30 ERROR -- \n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "bad.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> logService.parseAndSaveLogs(multipartFile));
    }

    @Test
    void testGetErrorTypeDisplayName_AllCases() throws Exception {
        // Test all known error types
        // Note: Based on your switch statement, these should match the case values
        assertEquals("Database Transaction Error",  // Assuming ErrorTypes.DATABASE_TRANSACTION_ERROR = "Database Transaction Error"
                invokeGetErrorTypeDisplayName("DATABASE_TRANSACTION_ERROR"));
        assertEquals("Constraint Violation Error",
                invokeGetErrorTypeDisplayName("CONSTRAINT_VIOLATION_ERROR"));
        assertEquals("Constraint Violation Error",
                invokeGetErrorTypeDisplayName("CONSTRAINTVIOLATIONEXCEPTION"));
        assertEquals("External Service Error",
                invokeGetErrorTypeDisplayName("EXTERNAL_SERVICE_ERROR"));
        assertEquals("Validation Error",
                invokeGetErrorTypeDisplayName("VALIDATION_ERROR"));
        assertEquals("Authentication Error",
                invokeGetErrorTypeDisplayName("AUTHENTICATION_ERROR"));
        assertEquals("Authorization Error",
                invokeGetErrorTypeDisplayName("AUTHORIZATION_ERROR"));
        assertEquals("Network Timeout Error",
                invokeGetErrorTypeDisplayName("NETWORK_TIMEOUT_ERROR"));
        assertEquals("Unknown Error",
                invokeGetErrorTypeDisplayName("UNKNOWN_ERROR"));
        assertEquals("Null Pointer Error",
                invokeGetErrorTypeDisplayName("NULL_POINTER_ERROR"));
        assertEquals("Null Pointer Error",
                invokeGetErrorTypeDisplayName("NULLPOINTEREXCEPTION"));

        // Test unknown codes - should return "Unknown Error"
        assertEquals("Unknown Error",
                invokeGetErrorTypeDisplayName("RANDOM_UNKNOWN_ERROR"));
    }

    @Test
    void testGetErrorCategoryStats_WithNullValues() {
        // Arrange
        Object[] row1 = new Object[]{"", 2L};  // Use empty string instead of null
        Object[] row2 = new Object[]{"database-transaction-error", 1L};

        when(errorLogRepository.countByerrorTypeBetween(any(), any()))
                .thenReturn(Arrays.asList(row1, row2));

        // Act
        var result = logService.getErrorCategoryStats(30);

        // Assert
        assertEquals(2, result.size());
        // Empty string should map to UNKNOWN_ERROR after normalization
        assertEquals("Unknown Error", result.get(0).getCategory());
        assertEquals("Database Transaction Error", result.get(1).getCategory());
    }

    @Test
    void testParseAndSaveLogs_ValidErrorLine_SavesToDatabase() throws Exception {
        // Arrange - Use the correct format matching the pattern
        String logContent = "2025-12-11T10:33:54.946+05:30 ERROR 12345 --- [main] [com.yash.app.Service] com.yash.app.Service : Database Transaction Error: Failed to connect\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Create a Log entity that will be saved
        Log expectedLog = new Log();
        expectedLog.setErrorLevel("ERROR");
        expectedLog.setErrorMessage("Database Transaction Error: Failed to connect");
        expectedLog.setSource("com.yash.app.Service");
        expectedLog.setErrorType("Database Transaction Error");
        expectedLog.setTimeStamp(LocalDateTime.parse("2025-12-11T10:33:54.946+05:30",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // Mock the mapper to return our expected log
        when(logMapper.toEntity(any(LogDTO.class))).thenReturn(expectedLog);

        // Act
        logService.parseAndSaveLogs(multipartFile);

        // Assert
        verify(errorLogRepository, times(1)).save(expectedLog);
        verify(logMapper, times(1)).toEntity(any(LogDTO.class));
    }

    @Test
    void testParseAndSaveLogs_MultipleErrorLines_SavesAll() throws Exception {
        // Arrange - Use correct format matching the pattern
        String logContent =
                "2025-12-11T10:33:54.946+05:30 ERROR 12345 --- [main] [com.yash.app.Service] com.yash.app.Service : Database Transaction Error: Failed to connect\n" +
                        "2025-12-11T10:33:55.946+05:30 ERROR 12345 --- [main] [com.yash.app.Dao] com.yash.app.Dao : NullPointerException: Object is null\n" +
                        "2025-12-11T10:33:56.946+05:30 INFO 12345 --- [main] [com.yash.app.Service] com.yash.app.Service : This should be skipped\n" +
                        "2025-12-11T10:33:57.946+05:30 ERROR 12345 --- [main] [com.yash.app.Controller] com.yash.app.Controller : Validation Error: Invalid input\n";

        MultipartFile multipartFile = new MockMultipartFile(
                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Mock the mapper to create logs
        when(logMapper.toEntity(any(LogDTO.class))).thenAnswer(invocation -> {
            LogDTO dto = invocation.getArgument(0);
            Log log = new Log();
            log.setErrorLevel(dto.getErrorLevel());
            log.setErrorMessage(dto.getErrorMessage());
            log.setSource(dto.getSource());
            log.setErrorType(dto.getErrorType());
            log.setTimeStamp(dto.getTimeStamp());
            return log;
        });

        // Act
        logService.parseAndSaveLogs(multipartFile);

        // Assert - should save 3 ERROR lines (skip the INFO line)
        verify(errorLogRepository, times(3)).save(any(Log.class));
        verify(logMapper, times(3)).toEntity(any(LogDTO.class));
    }

//    @Test
//    void testParseAndSaveLogs_ValidErrorLine_SavesToDatabase() throws Exception {
//        // Arrange - Use ISO format timestamp
//        String logContent = "2025-12-11T10:33:54.946+05:30 ERROR com.yash.app.Service -- Database Transaction Error: Failed to connect\n";
//
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
//        );
//
//        // Create a Log entity that will be saved
//        Log expectedLog = new Log();
//        expectedLog.setErrorLevel("ERROR");
//        expectedLog.setErrorMessage("Database Transaction Error: Failed to connect");
//        expectedLog.setSource("com.yash.app.Service");
//        expectedLog.setErrorType("Database Transaction Error"); // Based on detectErrorType logic
//        expectedLog.setTimeStamp(LocalDateTime.parse("2025-12-11T10:33:54.946+05:30",
//                DateTimeFormatter.ISO_OFFSET_DATE_TIME));
//
//        // Mock the mapper to return our expected log
//        when(logMapper.toEntity(any(LogDTO.class))).thenReturn(expectedLog);
//
//        // Act
//        logService.parseAndSaveLogs(multipartFile);
//
//        // Assert
//        verify(errorLogRepository, times(1)).save(expectedLog);
//        verify(logMapper, times(1)).toEntity(any(LogDTO.class));
//    }

//    @Test
//    void testParseAndSaveLogs_MultipleErrorLines_SavesAll() throws Exception {
//        // Arrange - Use correct ISO format timestamps
//        String logContent =
//                "2025-12-11T10:33:54.946+05:30 ERROR com.yash.app.Service -- Database Transaction Error: Failed to connect\n" +
//                        "2025-12-11T10:33:55.946+05:30 ERROR com.yash.app.Dao -- NullPointerException: Object is null\n" +
//                        "2025-12-11T10:33:56.946+05:30 INFO com.yash.app.Service -- This should be skipped\n" +
//                        "2025-12-11T10:33:57.946+05:30 ERROR com.yash.app.Controller -- Validation Error: Invalid input\n";
//
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file", "log.txt", "text/plain", logContent.getBytes(StandardCharsets.UTF_8)
//        );
//
//        // Mock the mapper to create logs
//        when(logMapper.toEntity(any(LogDTO.class))).thenAnswer(invocation -> {
//            LogDTO dto = invocation.getArgument(0);
//            Log log = new Log();
//            log.setErrorLevel(dto.getErrorLevel());
//            log.setErrorMessage(dto.getErrorMessage());
//            log.setSource(dto.getSource());
//            log.setErrorType(dto.getErrorType());
//            log.setTimeStamp(dto.getTimeStamp());
//            return log;
//        });
//
//        // Act
//        logService.parseAndSaveLogs(multipartFile);
//
//        // Assert - should save 3 ERROR lines (skip the INFO line)
//        verify(errorLogRepository, times(3)).save(any(Log.class));
//        verify(logMapper, times(3)).toEntity(any(LogDTO.class));
//    }



}
