package com.yash.log.repository;

import com.yash.log.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import  java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {



    List<Ticket> findAllByAssignedToOrReviewer(String assignee,String reviewer);
}