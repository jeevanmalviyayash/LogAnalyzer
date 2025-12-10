package com.yash.log.serviceImpl;

import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Log;
import com.yash.log.entity.Ticket;
import com.yash.log.mapper.TicketMapper;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.repository.TicketRepository;
import com.yash.log.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private TicketDTO ticketDTO;
    private Log errorLog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticketDTO = new TicketDTO();
        ticketDTO.setTicketId(1L);
        ticketDTO.setTitle("Database Error");
        ticketDTO.setErrorMessage("Unable to connect to DB");
        ticketDTO.setAssignedTo("John");
        ticketDTO.setErrorId(10L);

        ticket = new Ticket();
        ticket.setTicketId(1L);
        ticket.setTitle("Database Error");
        ticket.setErrorMessage("Unable to connect to DB");
        ticket.setAssignedTo("John");
        ticket.setErrorId(10L);

        errorLog = new Log();
        errorLog.setErrorId(10L);
        errorLog.setErrorMessage("Some Error");
    }


    @Test
    void testCreateTicket() {
        when(ticketMapper.toEntity(ticketDTO)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(errorLogRepository.findById(10L)).thenReturn(Optional.of(errorLog));
        when(errorLogRepository.save(errorLog)).thenReturn(errorLog);
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket(ticketDTO);

        assertNotNull(result);
        verify(ticketMapper).toEntity(ticketDTO);
        verify(ticketRepository).save(ticket);
        verify(errorLogRepository).findById(10L);
        verify(errorLogRepository).save(errorLog);
        verify(ticketMapper).toDto(ticket);
    }


    @Test
    void testGetTicketById_Found() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals("Database Error", result.getTitle());
    }

    @Test
    void testGetTicketById_NotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        TicketDTO result = ticketService.getTicketById(99L);

        assertNull(result);
    }


    @Test
    void testGetAllTickets() {
        when(ticketRepository.findAll()).thenReturn(Arrays.asList(ticket));
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        List<TicketDTO> result = ticketService.getAllTickets();

        assertEquals(1, result.size());
        assertEquals("Database Error", result.get(0).getTitle());
    }


    @Test
    void testUpdateTicket_Found() {
        Ticket updatedTicket = new Ticket();
        updatedTicket.setTicketId(1L);
        updatedTicket.setTitle("Database Error Updated");

        TicketDTO updatedDTO = new TicketDTO();
        updatedDTO.setTicketId(1L);
        updatedDTO.setTitle("Database Error Updated");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.updateEntityFromDto(ticketDTO, ticket)).thenReturn(updatedTicket);
        when(ticketRepository.save(updatedTicket)).thenReturn(updatedTicket);
        when(ticketMapper.toDto(updatedTicket)).thenReturn(updatedDTO);

        TicketDTO result = ticketService.updateTicket(1L, ticketDTO);

        assertEquals("Database Error Updated", result.getTitle());
        verify(ticketRepository).findById(1L);
        verify(ticketMapper).updateEntityFromDto(ticketDTO, ticket);
        verify(ticketRepository).save(updatedTicket);
        verify(ticketMapper).toDto(updatedTicket);
    }


    @Test
    void testUpdateTicket_NotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.updateTicket(99L, ticketDTO));

        assertEquals("Ticket not found with id 99", ex.getMessage());
    }


    @Test
    void testFindAllTicketByAssignedTo() {
        Ticket ticket2 = new Ticket();
        ticket2.setTicketId(2L);
        ticket2.setTitle("Login Issue");
        ticket2.setAssignedTo("John");

        TicketDTO dto2 = new TicketDTO();
        dto2.setTicketId(2L);
        dto2.setTitle("Login Issue");

        when(ticketRepository.findAllByAssignedToOrReviewer("John", "John"))
                .thenReturn(Arrays.asList(ticket, ticket2));

        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);
        when(ticketMapper.toDto(ticket2)).thenReturn(dto2);

        List<TicketDTO> result = ticketService.findAllTicketBYAssignedTo("John");

        assertEquals(2, result.size());
        assertEquals("Database Error", result.get(0).getTitle());
        assertEquals("Login Issue", result.get(1).getTitle());
    }
}
