package com.yash.log.service.impl;

import com.yash.log.constants.LogConstant;
import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;

import com.yash.log.mapper.LogMapper;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.services.LogFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LogFileServiceImpl implements LogFileService {

   @Autowired
    private ErrorLogRepository errorLogRepository;


    private final LogMapper logMapper;

    public  LogFileServiceImpl(LogMapper logMapper){
        this.logMapper=logMapper;
    }



    /*
    * Used to match log lines and extract timestamp,level,className and message
    *  */

    private static final Pattern LOG_PATTERN = Pattern.compile(LogConstant.LOG_PATTERN);

    @Override
    public void parseAndSaveLogs(MultipartFile file) throws IOException {
        /*
        *  @param BufferedReader reader: To read the uploaded log file line by line
        * */
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String timestamp = matcher.group(1);
                    String level = matcher.group(2);
                    String className = matcher.group(3);
                    log.info("Class Name : {}",className);

                    String message = matcher.group(4);

                    log.info("Message : {}",message);

                    LogDTO log = new LogDTO();
                    log.setErrorLevel(level);
                    log.setErrorMessage(message);
                    log.setSource(className);
                    log.setErrorType(detectErrorType(message));
                    log.setTimeStamp(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                    Log saveToDb = logMapper.toEntity(log);
                    errorLogRepository.save(saveToDb);
                }
            }
        }
    }



    private String detectErrorType(String message) {
        // If message contains a Java exception, extract it
        Pattern exceptionPattern = Pattern.compile(LogConstant.EXCEPTION_PATTERN);
        Matcher matcher = exceptionPattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1); // e.g., NullPointerException
        }

        // If message has a colon-based prefix (e.g., Database Transaction Error: ...)
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            return message.substring(0, colonIndex).trim();
        }
        return LogConstant.UNKNOWN_ERROR;
    }


    @Override
    public List<Object[]> countByErrorType() {
        List<Object[]> result = errorLogRepository.countByErrorType();
        return result;
    }



    @Override
    public List<Log> getAllLogs() {
        return errorLogRepository.findAll() ;
    }







}
