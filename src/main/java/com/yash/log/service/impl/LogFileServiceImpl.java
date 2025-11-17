package com.yash.log.service.impl;

import com.yash.log.entity.ErrorLog;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.services.LogFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogFileServiceImpl implements LogFileService {

    @Autowired
    private ErrorLogRepository errorLogRepository;


    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s+(INFO|WARN|ERROR|DEBUG)\\s+\\d+\\s+---\\s+\\[.*?\\]\\s+(.*?)\\s+:\\s+(.*)$"
    );

    @Override
    public void parseAndSaveLogs(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String timestamp = matcher.group(1);
                    String level = matcher.group(2);
                    String className = matcher.group(3);
                    String message = matcher.group(4);

                    ErrorLog log = new ErrorLog();
                    log.setErrorLevel(level);
                    log.setErrorMessage(message);
                    log.setTimeStamp(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                    errorLogRepository.save(log);
                }
            }
        }
    }
}
