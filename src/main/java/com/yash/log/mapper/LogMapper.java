package com.yash.log.mapper;

import com.yash.log.dto.LogDTO;
import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;

public class LogMapper {

    public static LogDTO mapToLogDto(Log log, LogDTO logDto){
         logDto.setErrorId(log.getErrorId());
            logDto.setErrorLevel(log.getErrorLevel());
            logDto.setErrorMessage(log.getErrorMessage());
            logDto.setTimeStamp(log.getTimeStamp());
            logDto.setUserId(log.getUserId());
            logDto.setSource(log.getSource());
            logDto.setErrorId(log.getErrorId());
            logDto.setErrorType(log.getErrorType());
            return logDto;

    }

    public static Log mapToLog(LogDTO logDto,Log log){
        log.setUserId(logDto.getUserId());
        log.setErrorLevel(logDto.getErrorLevel());
        log.setErrorMessage(logDto.getErrorMessage());
        log.setTimeStamp(logDto.getTimeStamp());
        log.setSource(logDto.getSource());
        log.setErrorId(logDto.getErrorId());
        log.setErrorType(logDto.getErrorType());
        return log;
    }

}
