package com.yash.log.service.services;

import com.yash.log.dto.LogDto;
import com.yash.log.entity.Log;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LogFileService {

    void parseAndSaveLogs(MultipartFile file) throws IOException;

    List<Object[]> countByErrorType();

    List<Log> getAllLogs();

    void saveManualError(LogDto logDto);


}
