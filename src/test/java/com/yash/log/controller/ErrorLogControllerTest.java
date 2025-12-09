package com.yash.log.controller;
import com.yash.log.entity.Log;
import java.time.LocalDate;
import java.util.List;
import com.yash.log.dto.DailyErrorCountDto;
import com.yash.log.dto.ErrorCategoryStatDto;
import com.yash.log.exceptions.GlobalExceptionHandler;
import com.yash.log.repository.ErrorLogRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorLogControllerTest {

    @Mock
    private LogFileServiceImpl logFileServiceImpl;

    //@InjectMocks injects the mock service into the ErrorLogController
    @InjectMocks
    private ErrorLogController errorLogController;

    @Test
    void uploadLogFile_ValidLogFile_ReturnsSuccess() throws IOException {
        //Spring testing utility class that simulates a real file upload from a web form.
        // It implements the MultipartFile interface expected by Spring controllers annotated
        // with @RequestParam("file") MultipartFile file.
        // Arrange
        MultipartFile validFile = new MockMultipartFile(
                "file", // Matches @RequestParam("file")
                "test.log",
                "text/plain",  //Content-Type header
                "sample log content".getBytes()
        );

        // With doNothing(): Explicitly confirms "success" scenario
        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        // Act
        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logs uploaded and saved successfully!", response.getBody());
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
    }

    @Test
    void uploadLogFile_InvalidFileType_ReturnsBadRequest() throws IOException {
        MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.invalid",
                "text/plain",
                "invalid content".getBytes()
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(invalidFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid file type"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

//    @Test
//    void uploadLogFile_FileTooLarge_ReturnsBadRequest() throws IOException {
//        byte[] largeContent = new byte[11 * 1024 * 1024];
//        MultipartFile largeFile = new MockMultipartFile(
//                "file",
//                "large.log",
//                "text/plain",
//                largeContent
//        );
//
//        ResponseEntity<String> response = errorLogController.uploadLogFile(largeFile);
//
//       // assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertTrue(response.getBody().contains("File too large"));
//        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
//    }

    @Test
    void uploadLogFile_ServiceThrowsException_ReturnsErrorResponse() throws IOException {
        // Arrange
        MultipartFile validFile = new MockMultipartFile(
                "file",
                "test.log",
                "text/plain",
                "sample log content".getBytes()
        );

        doThrow(new RuntimeException("Service error"))
                .when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        // Act
        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Service error"));
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
    }



    @Test
    void getAllLogs_ReturnsListOfLogs() {
        // Arrange
        List<Log> mockLogs = List.of(new Log(), new Log());
        when(logFileServiceImpl.getAllLogs()).thenReturn(mockLogs);

        // Act
        ResponseEntity<List<Log>> response = errorLogController.getAllLogs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(logFileServiceImpl, times(1)).getAllLogs();
    }

    @Test
    void getAll_NoLastDays_ReturnsAllLogs() {
        // Arrange
        List<Log> mockLogs = List.of(new Log(), new Log(), new Log());
        when(logFileServiceImpl.getAllLogs()).thenReturn(mockLogs);

        // Act
        List<Log> result = errorLogController.getAll(null);

        // Assert
        assertEquals(3, result.size());
        verify(logFileServiceImpl, times(1)).getAllLogs();
        verify(logFileServiceImpl, never()).getLogsLastNDays(anyInt());
    }

    @Test
    void getAll_WithLastDays_ReturnsFilteredLogs() {
        // Arrange
        List<Log> mockLogs = List.of(new Log());
        when(logFileServiceImpl.getLogsLastNDays(7)).thenReturn(mockLogs);

        // Act
        List<Log> result = errorLogController.getAll(7);

        // Assert
        assertEquals(1, result.size());
        verify(logFileServiceImpl, times(1)).getLogsLastNDays(7);
        verify(logFileServiceImpl, never()).getAllLogs();
    }

    @Test
    void getDailyCounts_ReturnsDailyErrorCounts() {
        // Arrange
        List<DailyErrorCountDto> mockCounts = List.of(
                new DailyErrorCountDto(LocalDate.parse("2024-12-01"), 5),
                new DailyErrorCountDto(LocalDate.parse("2024-12-02"), 3)
        );
        when(logFileServiceImpl.getDailyErrorCounts(10)).thenReturn(mockCounts);

        // Act
        List<DailyErrorCountDto> result = errorLogController.getDailyCounts(10);

        // Assert
        assertEquals(2, result.size());
        verify(logFileServiceImpl, times(1)).getDailyErrorCounts(10);
    }

    @Test
    void getDailyCounts_UsesDefaultLastDays() {
        // Arrange
        when(logFileServiceImpl.getDailyErrorCounts(10))
                .thenReturn(List.of(new DailyErrorCountDto(LocalDate.parse("2024-12-01"), 2)));

        // Act
        List<DailyErrorCountDto> result = errorLogController.getDailyCounts(10);

        // Assert
        assertEquals(1, result.size());
        verify(logFileServiceImpl, times(1)).getDailyErrorCounts(10);
    }

    @Test
    void getCategoryStats_ReturnsCategoryStats() {
        // Arrange
        List<ErrorCategoryStatDto> mockStats = List.of(
                new ErrorCategoryStatDto("CRITICAL", 4),
                new ErrorCategoryStatDto("WARN", 6)
        );
        when(logFileServiceImpl.getErrorCategoryStats(30)).thenReturn(mockStats);

        // Act
        List<ErrorCategoryStatDto> result = errorLogController.getCategoryStats(30);

        // Assert
        assertEquals(2, result.size());
        verify(logFileServiceImpl, times(1)).getErrorCategoryStats(30);
    }

    @Test
    void getCategoryStats_UsesDefaultLastDays() {
        // Arrange
        when(logFileServiceImpl.getErrorCategoryStats(30))
                .thenReturn(List.of(new ErrorCategoryStatDto("INFO", 10)));

        // Act
        List<ErrorCategoryStatDto> result = errorLogController.getCategoryStats(30);

        // Assert
        assertEquals(1, result.size());
        verify(logFileServiceImpl, times(1)).getErrorCategoryStats(30);
    }
// ------------------------- NEGATIVE TEST CASES START -------------------------

    @Test
    void getAllLogs_ShouldThrowException_WhenServiceFails() {
        when(logFileServiceImpl.getAllLogs())
                .thenThrow(new RuntimeException("DB failed"));

        assertThrows(RuntimeException.class,
                () -> errorLogController.getAllLogs());
    }

    @Test
    void getAll_ShouldThrowException_WhenInvalidLastDays() {
        when(logFileServiceImpl.getLogsLastNDays(5))
                .thenThrow(new IllegalArgumentException("Invalid days"));

        assertThrows(IllegalArgumentException.class,
                () -> errorLogController.getAll(5));
    }

    @Test
    void getDailyCounts_ShouldThrowException_WhenServiceFails() {
        when(logFileServiceImpl.getDailyErrorCounts(10))
                .thenThrow(new RuntimeException("Calculation failed"));

        assertThrows(RuntimeException.class,
                () -> errorLogController.getDailyCounts(10));
    }

    @Test
    void getCategoryStats_ShouldThrowException_WhenServiceFails() {
        when(logFileServiceImpl.getErrorCategoryStats(30))
                .thenThrow(new RuntimeException("Category error"));

        assertThrows(RuntimeException.class,
                () -> errorLogController.getCategoryStats(30));
    }

// ------------------------- NEGATIVE TEST CASES END -------------------------





}