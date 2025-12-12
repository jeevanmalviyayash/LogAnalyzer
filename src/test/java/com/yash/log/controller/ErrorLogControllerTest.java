package com.yash.log.controller;
import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import com.yash.log.dto.DailyErrorCountDto;
import com.yash.log.dto.ErrorCategoryStatDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.GlobalExceptionHandler;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.repository.IUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

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

    @Mock
    private IUserRepository userRepository;


    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== Existing Tests (keep these) ==========
    @Test
    void uploadLogFile_ValidLogFile_ReturnsSuccess() throws IOException {
        // ... existing test code ...
        MultipartFile validFile = new MockMultipartFile(
                "file",
                "test.log",
                "text/plain",
                "sample log content".getBytes()
        );

        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logs uploaded and saved successfully!", response.getBody());
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
    }

    @Test
    void uploadLogFile_InvalidFileType_ReturnsBadRequest() throws IOException {
        // ... existing test code ...
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

    @Test
    void uploadLogFile_FileTooLarge_ReturnsBadRequest() throws IOException {
        // ... existing test code ...
        byte[] largeContent = new byte[51 * 1024 * 1024];
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.log",
                "text/plain",
                largeContent
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(largeFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("File too large"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

    // ========== Tests for saveManualError ==========


    @Test
    void saveManualError_EmptyErrorMessage_ReturnsBadRequest() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("   "); // Whitespace only

        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error message cannot be empty", response.getBody());
        verify(userRepository, never()).findByUserEmail(anyString());
        verify(logFileServiceImpl, never()).saveManualError(any());
    }

    @Test
    void saveManualError_NullErrorMessage_ReturnsBadRequest() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage(null);

        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error message cannot be empty", response.getBody());
        verify(userRepository, never()).findByUserEmail(anyString());
        verify(logFileServiceImpl, never()).saveManualError(any());
    }

    @Test
    void saveManualError_UnauthenticatedUser_ReturnsUnauthorized() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");

        SecurityContextHolder.clearContext(); // No authentication

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody());
        verify(userRepository, never()).findByUserEmail(anyString());
        verify(logFileServiceImpl, never()).saveManualError(any());
    }



    // ========== Tests to improve validateFile coverage ==========
    @Test
    void uploadLogFile_EmptyFile_ReturnsBadRequest() throws IOException {
        // Test for empty file content
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.log",
                "text/plain",
                new byte[0]
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(emptyFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("No file provided or file is empty"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

    @Test
    void uploadLogFile_NullFilename_ReturnsBadRequest() throws IOException {
        // Test for null filename
        MultipartFile nullNameFile = new MockMultipartFile(
                "file",  // parameter name
                null,    // original filename
                "text/plain",
                "content".getBytes()
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(nullNameFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Filename is missing"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

    @Test
    void uploadLogFile_EmptyFilename_ReturnsBadRequest() throws IOException {
        // Test for empty filename
        MultipartFile emptyNameFile = new MockMultipartFile(
                "file",
                "",  // empty filename
                "text/plain",
                "content".getBytes()
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(emptyNameFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Filename is missing"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

    @Test
    void uploadLogFile_WhitespaceFilename_ReturnsBadRequest() throws IOException {
        // Test for whitespace-only filename
        MultipartFile whitespaceNameFile = new MockMultipartFile(
                "file",
                "   ",  // whitespace filename
                "text/plain",
                "content".getBytes()
        );

        ResponseEntity<String> response = errorLogController.uploadLogFile(whitespaceNameFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Filename is missing"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

    @Test
    void uploadLogFile_ValidUpperCaseExtension_ReturnsSuccess() throws IOException {
        // Test that uppercase extensions are also accepted
        MultipartFile validFile = new MockMultipartFile(
                "file",
                "test.LOG",  // uppercase extension
                "text/plain",
                "sample log content".getBytes()
        );

        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logs uploaded and saved successfully!", response.getBody());
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
    }

    @Test
    void uploadLogFile_ValidMixedCaseExtension_ReturnsSuccess() throws IOException {
        // Test that mixed case extensions are also accepted
        MultipartFile validFile = new MockMultipartFile(
                "file",
                "test.LoG",  // mixed case extension
                "text/plain",
                "sample log content".getBytes()
        );

        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logs uploaded and saved successfully!", response.getBody());
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
    }

    @Test
    void uploadLogFile_FileSizeAtLimit_ReturnsSuccess() throws IOException {
        // Test file size exactly at the limit (50 MB)
        byte[] exactSizeContent = new byte[50 * 1024 * 1024]; // Exactly 50 MB
        MultipartFile exactSizeFile = new MockMultipartFile(
                "file",
                "exact.log",
                "text/plain",
                exactSizeContent
        );

        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));

        ResponseEntity<String> response = errorLogController.uploadLogFile(exactSizeFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logs uploaded and saved successfully!", response.getBody());
        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(exactSizeFile);
    }

    @Test
    void uploadLogFile_NullFile_ReturnsBadRequest() throws IOException {
        // This tests the case where the MultipartFile itself is null
        // Note: In actual Spring MVC, @RequestParam will reject null before reaching controller
        // So this might not be reachable in real scenarios, but we test it anyway

        ResponseEntity<String> response = errorLogController.uploadLogFile(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("No file provided or file is empty"));
        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
    }

//    @Test
//    void uploadLogFile_ValidLogFile_ReturnsSuccess() throws IOException {
//        //Spring testing utility class that simulates a real file upload from a web form.
//        // It implements the MultipartFile interface expected by Spring controllers annotated
//        // with @RequestParam("file") MultipartFile file.
//        // Arrange
//        MultipartFile validFile = new MockMultipartFile(
//                "file", // Matches @RequestParam("file")
//                "test.log",
//                "text/plain",  //Content-Type header
//                "sample log content".getBytes()
//        );
//
//        // With doNothing(): Explicitly confirms "success" scenario
//        doNothing().when(logFileServiceImpl).parseAndSaveLogs(any(MultipartFile.class));
//
//        // Act
//        ResponseEntity<String> response = errorLogController.uploadLogFile(validFile);
//
//        // Assert
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Logs uploaded and saved successfully!", response.getBody());
//        verify(logFileServiceImpl, times(1)).parseAndSaveLogs(validFile);
//    }

//    @Test
//    void uploadLogFile_InvalidFileType_ReturnsBadRequest() throws IOException {
//        MultipartFile invalidFile = new MockMultipartFile(
//                "file",
//                "test.invalid",
//                "text/plain",
//                "invalid content".getBytes()
//        );
//
//        ResponseEntity<String> response = errorLogController.uploadLogFile(invalidFile);
//
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertTrue(response.getBody().contains("Invalid file type"));
//        verify(logFileServiceImpl, never()).parseAndSaveLogs(any());
//    }
//
//    @Test
//    void uploadLogFile_FileTooLarge_ReturnsBadRequest() throws IOException {
//        byte[] largeContent = new byte[51 * 1024 * 1024];
//        MultipartFile largeFile = new MockMultipartFile(
//                "file",
//                "large.log",
//                "text/plain",
//                largeContent
//        );
//
//        ResponseEntity<String> response = errorLogController.uploadLogFile(largeFile);
//
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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

    @Test
    void saveManualError_ValidRequest_ReturnsSuccess() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");

        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUserEmail("test@example.com");

        // Mock the authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.emptyList()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
        doNothing().when(logFileServiceImpl).saveManualError(any(LogDTO.class));

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Manual error added successfully!", response.getBody());
        verify(userRepository, times(1)).findByUserEmail("test@example.com");
        verify(logFileServiceImpl, times(1)).saveManualError(any(LogDTO.class));

        // Verify userId and timestamp were set
        assertEquals(1L, logDto.getUserId());
        assertNotNull(logDto.getTimeStamp());
    }

    @Test
    void saveManualError_UserNotFound_ReturnsInternalServerError() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");

        // Set up authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("test@example.com"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to add error"));
        verify(userRepository, times(1)).findByUserEmail("test@example.com");
        verify(logFileServiceImpl, never()).saveManualError(any());
    }

    @Test
    void saveManualError_ServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");

        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUserEmail("test@example.com");

        // Set up authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
        doThrow(new RuntimeException("Database error"))
                .when(logFileServiceImpl).saveManualError(any(LogDTO.class));

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to add error"));
        verify(userRepository, times(1)).findByUserEmail("test@example.com");
        verify(logFileServiceImpl, times(1)).saveManualError(any(LogDTO.class));
    }
    @Test
    void saveManualError_AuthenticationIsNull_ReturnsUnauthorized() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");

        // Set authentication to null explicitly
        SecurityContextHolder.getContext().setAuthentication(null);

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody());
        verify(userRepository, never()).findByUserEmail(anyString());
        verify(logFileServiceImpl, never()).saveManualError(any());
    }

    @Test
    void saveManualError_UserIdAlreadySet_ShouldOverrideWithAuthenticatedUser() {
        // Arrange
        LogDTO logDto = new LogDTO();
        logDto.setErrorMessage("Test error message");
        logDto.setUserId(999L); // Some existing userId
        logDto.setTimeStamp(LocalDateTime.now().minusDays(1)); // Some existing timestamp

        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUserEmail("test@example.com");

        // Set up authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
        doNothing().when(logFileServiceImpl).saveManualError(any(LogDTO.class));

        // Act
        ResponseEntity<String> response = errorLogController.saveManualError(logDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verify that the userId was overridden
        assertEquals(1L, logDto.getUserId());
        // Verify that timestamp was updated (should be recent)
        assertTrue(logDto.getTimeStamp().isAfter(LocalDateTime.now().minusMinutes(1)));
    }


}