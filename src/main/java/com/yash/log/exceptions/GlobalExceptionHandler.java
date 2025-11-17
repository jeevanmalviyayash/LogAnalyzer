package com.yash.log.exceptions;

import com.yash.log.dto.ErrorLevel;
import com.yash.log.entity.ErrorLog;
import com.yash.log.mapper.ErrorFileMapper;
import com.yash.log.parser.FileParser;
import com.yash.log.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
   private ErrorFileMapper fileMapper;

    @Autowired
    private FileParser fileParser;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        ErrorLevel level=  determineErrorLevel(ex);
        String filePath = fileMapper.getFilePath(level);

        Object parsedData= fileParser.parse(filePath);

        ErrorLog  log= new ErrorLog();
        log.setErrorLevel(level.name());
        log.setErrorMessage(ex.getMessage());
        log.setTimeStamp(LocalDateTime.now());
        errorLogRepository.save(log);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Handled "+level + " , parsed file: "+ filePath );

    }

    private ErrorLevel determineErrorLevel(Exception ex){
        if (ex instanceof IllegalArgumentException){
            return ErrorLevel.WARN;
        }
        return ErrorLevel.ERROR;

    }



}
