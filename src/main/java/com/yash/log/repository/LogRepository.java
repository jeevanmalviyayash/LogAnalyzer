package com.yash.log.repository;

import com.yash.log.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogEntry, Long> {

}
