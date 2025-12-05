package com.yash.log.service.impl;

import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Ticket;
import com.yash.log.mapper.TicketMapper;
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

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        ticketDTO.setCreatedDate(LocalDateTime.now());
        ticketDTO.setUpdatedDate(LocalDateTime.now());
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        Ticket tick = ticketRepository.save(ticket);
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
        Optional<Ticket> ticketById = Optional.ofNullable(ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + id)));
        if (ticketById.isPresent()) {
            ticketById.get().setUpdatedDate(LocalDateTime.now());
            Ticket ticket = ticketMapper.updateEntityFromDto(ticketDTO, ticketById.get());
            ticketRepository.save(ticket);
            return ticketMapper.toDto(ticket);
        }
        return null;
    }


    @Override
    public List<TicketDTO> findAllTicketBYAssignedTo(String assignee) {

        return ticketRepository.findAllByAssignedToOrReviewer(assignee, assignee)
                .stream()
                .map(ticketMapper::toDto)   // convert each Ticket → TicketDTO
                .collect(Collectors.toList());
    }


}
