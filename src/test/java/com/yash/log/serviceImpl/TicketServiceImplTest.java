package com.yash.log.serviceImpl;



import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Ticket;
import com.yash.log.mapper.TicketMapper;
import com.yash.log.repository.TicketRepository;
import com.yash.log.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private TicketDTO ticketDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticketDTO = new TicketDTO();
        ticketDTO.setTicketId(1L);
        ticketDTO.setTitle("Database Error");
        ticketDTO.setErrorMessage("Unable to connect to DB");
        ticketDTO.setAssignedTo("John");

        ticket = new Ticket();
        ticket.setTicketId(1L);
        ticket.setTitle("Database Error");
        ticket.setErrorMessage("Unable to connect to DB");
        ticket.setAssignedTo("John");
    }

    @Test
    void testCreateTicket() {
        // Mock mapper and repository behavior
        when(ticketMapper.toEntity(any(TicketDTO.class))).thenReturn(ticket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDto(any(Ticket.class))).thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket(ticketDTO);

        // Assertions
        assertNotNull(result);
        assertEquals("Database Error", result.getTitle());
        assertEquals("John", result.getAssignedTo());

        // Verify interactions
        verify(ticketMapper, times(1)).toEntity(ticketDTO);
        verify(ticketRepository, times(1)).save(ticket);
        verify(ticketMapper, times(1)).toDto(ticket);
    }

    @Test
    void testGetTicketById_Found() {
        // Mock repository and mapper
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTicketId());
       assertEquals("Database Error", result.getTitle());

    }
    @Test
    void testGetTicketById_NotFound() {
        // Mock repository to return empty
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        TicketDTO result = ticketService.getTicketById(99L);

        assertNull(result);

        verify(ticketRepository, times(1)).findById(99L);
        verify(ticketMapper, never()).toDto(any());
    }


    @Test
    void testGetAllTickets() {
        // Mock repository and mapper
        when(ticketRepository.findAll()).thenReturn(Arrays.asList(ticket));
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);


        List<TicketDTO> result = ticketService.getAllTickets();

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Database Error", result.get(0).getTitle());

        // Verify interactions
        verify(ticketRepository, times(1)).findAll();
        verify(ticketMapper, times(1)).toDto(ticket);

    }
    @Test
    void testUpdateTicket_Found() {
        // Mock repository and mapper
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.updateEntityFromDto(ticketDTO, ticket)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.updateTicket(1L, ticketDTO);

        assertNotNull(result);
        assertEquals("Database Error Updated", result.getTitle());
        assertEquals("Connection timeout", result.getErrorMessage());

        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketMapper, times(1)).updateEntityFromDto(ticketDTO, ticket);
        verify(ticketRepository, times(1)).save(ticket);
        verify(ticketMapper, times(1)).toDto(ticket);
    }

    @Test
    void testUpdateTicket_NotFound() {
        // Mock repository to throw exception
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ticketService.updateTicket(99L, ticketDTO));

        assertEquals("Ticket not found with id 99", exception.getMessage());

        verify(ticketRepository, times(1)).findById(99L);
        verify(ticketMapper, never()).updateEntityFromDto(any(), any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testFindAllTicketByAssignedTo() {
        // Mock repository to return tickets where assignee or reviewer = "John"
        when(ticketRepository.findAllByAssignedToOrReviewer("John", "John"))
                .thenReturn(Arrays.asList(ticket));

        // Mock mapper conversions
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);


        List<TicketDTO> result = ticketService.findAllTicketBYAssignedTo("John");

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Database Error", result.get(0).getTitle());
        assertEquals("Login Issue", result.get(1).getTitle());

        // Verify interactions
        verify(ticketRepository, times(1)).findAllByAssignedToOrReviewer("John", "John");
        verify(ticketMapper, times(1)).toDto(ticket);

    }


}

