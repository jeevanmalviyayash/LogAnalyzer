package com.yash.log.service.impl;

import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Log;
import com.yash.log.entity.Ticket;
import com.yash.log.mapper.TicketMapper;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.repository.TicketRepository;
import com.yash.log.service.services.TicketService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {


    private  final ErrorLogRepository errorLogRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository, TicketMapper ticketMapper,
                             ErrorLogRepository errorLogRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.errorLogRepository=errorLogRepository;
    }

    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        ticketDTO.setCreatedDate(LocalDateTime.now());
        ticketDTO.setUpdatedDate(LocalDateTime.now());
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        Ticket tick = ticketRepository.save(ticket);
        Optional<Log> log = errorLogRepository.findById(ticketDTO.getErrorId());
        if(log.isPresent()){
            Log errorLog=log.get();
            errorLog.setTicketId(tick.getTicketId());
            errorLogRepository.save(errorLog);
        }else {
            return new TicketDTO();
        }
        return ticketMapper.toDto(tick);
    }

    @Override
    public TicketDTO getTicketById(Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isPresent()) {
            return ticketMapper.toDto(ticket.get());
        }

        return null;
    }

    @Override
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticketById = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + id));
        if (ticketById!= null) {
            ticketById.setUpdatedDate(LocalDateTime.now());
            Ticket ticket = ticketMapper.updateEntityFromDto(ticketDTO, ticketById);
            ticketRepository.save(ticket);
            return ticketMapper.toDto(ticket);
        }
        return ticketDTO;
    }


    @Override
    public List<TicketDTO> findAllTicketBYAssignedTo(String assignee) {

        return ticketRepository.findAllByAssignedToOrReviewer(assignee, assignee)
                .stream()
                .map(ticketMapper::toDto)   // convert each Ticket → TicketDTO
                .collect(Collectors.toList());
    }


}
