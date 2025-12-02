package com.yash.log.exceptions;

import com.yash.log.dto.ErrorLevel;
import com.yash.log.entity.Log;

import com.yash.log.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        ErrorLevel level=  determineErrorLevel(ex);
        Log log= new Log();
        log.setErrorLevel(level.name());
        log.setErrorMessage(ex.getMessage());
        log.setTimeStamp(LocalDateTime.now());
        errorLogRepository.save(log);
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

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        String firstError = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> error.getDefaultMessage())
//                .findFirst()
//                .orElse("Validation failed");
//
//        Map<String, String> response = new HashMap<>();
//        response.put("error", firstError);
//
//        return ResponseEntity.badRequest().body(response);
//    }
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .findFirst()
            .orElse("Validation failed");

    return ResponseEntity.badRequest().body(errorMessage);
}
}
