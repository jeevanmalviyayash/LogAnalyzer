package com.yash.log.service.services;

import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketService {


    TicketDTO createTicket(TicketDTO ticketDTO);


    TicketDTO getTicketById(Long id);


    List<TicketDTO> getAllTickets();


    TicketDTO updateTicket(Long id, TicketDTO ticketDTO);


    List<TicketDTO> findAllTicketBYAssignedTo(String assignee);
}

