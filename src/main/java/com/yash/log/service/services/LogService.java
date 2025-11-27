package com.yash.log.service.services;

import com.yash.log.entity.Log;
import java.time.LocalDateTime;
import java.util.List;

public interface LogService {

    List<Log> filterLogs(String search,
                         LocalDateTime startDate,
                         LocalDateTime endDate);
}
