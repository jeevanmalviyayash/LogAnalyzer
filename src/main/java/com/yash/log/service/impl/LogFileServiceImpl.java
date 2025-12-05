package com.yash.log.service.impl;

import com.yash.log.constants.LogConstant;
import com.yash.log.dto.ErrorTypes;
import com.yash.log.dto.LogDto;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.yash.log.dto.DailyErrorCountDto;
import com.yash.log.dto.ErrorCategoryStatDto;

@Slf4j
@Service
public class LogFileServiceImpl implements LogFileService {

   @Autowired
    private ErrorLogRepository errorLogRepository;

    private static final Pattern LOG_PATTERN = Pattern.compile(LogConstant.LOG_PATTERN);
    private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public void parseAndSaveLogs(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    if(!matcher.group(2).equals("ERROR")){
                        continue;
                    }
                    LogDto logDto = mapMatcherToLogDto(matcher);
                    Log saveToDb = LogMapper.mapToLog(logDto, new Log());
                    errorLogRepository.save(saveToDb);
                }
            }
        }
    }
    // helper method
    private LogDto mapMatcherToLogDto(Matcher matcher) {
        final String timestamp = matcher.group(1);
        final String level = matcher.group(2);
        final String className = matcher.group(3);
        final String message = matcher.group(4);

        // Reduce volume in production logs; keep detailed output available at DEBUG
        log.debug("Class Name : {}", className);
        log.debug("Message : {}", message);

        LogDto logDto = new LogDto();
        logDto.setErrorLevel(level);
        logDto.setErrorMessage(message);
        logDto.setSource(className);
        logDto.setErrorType(detectErrorType(message));
        logDto.setTimeStamp(LocalDateTime.parse(timestamp, ISO_OFFSET_FORMATTER));
        return logDto;
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



    public List<Log> getLogsLastNDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        return errorLogRepository.findByTimeStampBetweenOrderByTimeStampDesc(from, now);
    }

    public List<DailyErrorCountDto> getDailyErrorCounts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1);
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59, 59);

        List<Object[]> raw = errorLogRepository.countByDayBetween(from, to);
        return raw.stream()
                .map(row -> new DailyErrorCountDto(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        (Long) row[1]))
                .collect(Collectors.toList());
    }
    public List<ErrorCategoryStatDto> getErrorCategoryStats(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        List<Object[]> raw = errorLogRepository.countByerrorTypeBetween(from, now);

        return raw.stream()
                .map(row -> {
                    String errorCode = ((String) row[0]).toUpperCase().trim()
                            .replace(" ", "_")
                            .replace("-", "_")
                            .replace(".", "_");

                    //System.out.println("Raw DB value: '" + row[0] + "' -> Normalized: '" + errorCode + "'");

                    String displayName = getErrorTypeDisplayName(errorCode);
                    return new ErrorCategoryStatDto(displayName, (Long) row[1]);
                })
                .collect(Collectors.toList());
    }

    private String getErrorTypeDisplayName(String errorCode) {
        return switch (errorCode) {
            case "DATABASE_TRANSACTION_ERROR" -> ErrorTypes.DATABASE_TRANSACTION_ERROR;
            case "CONSTRAINT_VIOLATION_ERROR", "CONSTRAINTVIOLATIONEXCEPTION" -> ErrorTypes.CONSTRAINT_VIOLATION_ERROR;
            case "EXTERNAL_SERVICE_ERROR" -> ErrorTypes.EXTERNAL_SERVICE_ERROR;
            case "VALIDATION_ERROR" -> ErrorTypes.VALIDATION_ERROR;
            case "AUTHENTICATION_ERROR" -> ErrorTypes.AUTHENTICATION_ERROR;
            case "AUTHORIZATION_ERROR" -> ErrorTypes.AUTHORIZATION_ERROR;
            case "NETWORK_TIMEOUT_ERROR" -> ErrorTypes.NETWORK_TIMEOUT_ERROR;
            case "UNKNOWN_ERROR" -> ErrorTypes.UNKNOWN_ERROR;
            case "NULL_POINTER_ERROR", "NULLPOINTEREXCEPTION" -> ErrorTypes.NULL_POINTER_ERROR;
            default -> {
                yield ErrorTypes.UNKNOWN_ERROR;
            }
        };
    }







}
