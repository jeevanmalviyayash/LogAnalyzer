package com.yash.log.repository;

import com.yash.log.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<Log,Long> {

    List<Log> findByTimeStampBetweenOrderByTimeStampDesc(
            LocalDateTime from, LocalDateTime to
    );

    @Query("SELECT e.errorLevel, COUNT(e) FROM Log e GROUP BY e.errorLevel")
    List<Object[]>countErrorsByLevel();

    @Query("SELECT e.errorType, COUNT(e) FROM Log e GROUP BY e.errorType")
    List<Object[]>countByErrorType();

    @Query("SELECT DATE(e.timeStamp) as day, COUNT(e) " +
            "FROM Log e " +
            "WHERE e.timeStamp BETWEEN :from AND :to " +
            "GROUP BY DATE(e.timeStamp) " +
            "ORDER BY day ASC")
    List<Object[]> countByDayBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT e.errorType as errorType, COUNT(e) " +
            "FROM Log e " +
            "WHERE e.timeStamp BETWEEN :from AND :to " +
            "GROUP BY e.errorType")
    List<Object[]> countByerrorTypeBetween(LocalDateTime from, LocalDateTime to);




}
