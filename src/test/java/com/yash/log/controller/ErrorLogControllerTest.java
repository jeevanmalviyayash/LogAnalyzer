package com.yash.log.controller;

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

    @Test
    void uploadLogFile_FileTooLarge_ReturnsBadRequest() throws IOException {
        byte[] largeContent = new byte[11 * 1024 * 1024];
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
}