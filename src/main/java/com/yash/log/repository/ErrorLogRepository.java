package com.yash.log.repository;

import com.yash.log.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog,Long> {

    @Query("SELECT e.errorLevel, COUNT(e) FROM ErrorLog e GROUP BY e.errorLevel")
    List<Object[]>countErrorsByLevel();
}
