package com.yash.log.mapper;

import com.yash.log.dto.ErrorLevel;
import org.springframework.stereotype.Component;

import java.util.Map;


// Currently not using this class, but it can be useful for future enhancements
@Component
public class ErrorFileMapper {

    private static final Map<ErrorLevel,String> fileMap= Map.of(
      ErrorLevel.INFO, "classpath:logs/info.log",
        ErrorLevel.WARN, "classpath:logs/warn.log",
        ErrorLevel.DEBUG, "classpath:logs/debug.log"
    );

    public String getFilePath(ErrorLevel errorLevel){
        return fileMap.get(errorLevel);
    }

}
