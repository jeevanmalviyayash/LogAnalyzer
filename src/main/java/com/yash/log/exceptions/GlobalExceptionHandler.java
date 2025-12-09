package com.yash.log.exceptions;

import com.yash.log.dto.ErrorLevel;
import com.yash.log.entity.Log;

import com.yash.log.repository.ErrorLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private final ErrorLogRepository errorLogRepository;

    public GlobalExceptionHandler(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        ErrorLevel level=  determineErrorLevel(ex);
        Log log= new Log();
        log.setErrorLevel(level.name());
        log.setErrorMessage(ex.getMessage());
        log.setTimeStamp(LocalDateTime.now());
       // errorLogRepository.save(log);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Handled "+level  );

    }

    private ErrorLevel determineErrorLevel(Exception ex){
        if (ex instanceof IllegalArgumentException){
            return ErrorLevel.WARN;
        }
        return ErrorLevel.ERROR;
    }



}
