package com.yash.log.repository;

import com.yash.log.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<Log,Long> {

    @Query("SELECT e.errorLevel, COUNT(e) FROM Log e GROUP BY e.errorLevel")
    List<Object[]>countErrorsByLevel();

    @Query("SELECT e.errorType, COUNT(e) FROM Log e GROUP BY e.errorType")
    List<Object[]>countByErrorType();
}
