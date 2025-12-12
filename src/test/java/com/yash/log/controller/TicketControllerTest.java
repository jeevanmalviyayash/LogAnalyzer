package com.yash.log.controller;

import com.yash.log.constants.Priority;
import com.yash.log.constants.Role;
import com.yash.log.constants.Status;
import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.User;
import com.yash.log.service.impl.IUserService;
import com.yash.log.service.services.TicketService;
import com.yash.log.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private TicketController ticketController;



    // ✅ Direct controller instance

    // ✅ Test POST /api/tickets
    @Test
    void testCreateTicket() {
        TicketDTO request = new TicketDTO();
        request.setTitle("Login Issue");
        request.setPriority(Priority.HIGH);
        request.setStatus(Status.OPEN);

        TicketDTO response = new TicketDTO();
        response.setTicketId(1L);
        response.setTitle("Login Issue");

        when(ticketService.createTicket(any(TicketDTO.class))).thenReturn(response);

        ResponseEntity<TicketDTO> result = ticketController.createTicket(request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1L, result.getBody().getTicketId());
        assertEquals("Login Issue", result.getBody().getTitle());
    }

    // ✅ Test GET /api/tickets/user-id/{assignee}
    @Test
    void testGetTicketByUserId() {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(10L);
        dto.setAssignedTo("john");

        when(ticketService.findAllTicketBYAssignedTo("john"))
                .thenReturn(List.of(dto));

        ResponseEntity<List<TicketDTO>> result =
                ticketController.getTicketByUserId("john");

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(10L, result.getBody().get(0).getTicketId());
        assertEquals("john", result.getBody().get(0).getAssignedTo());
    }

    // ✅ Test GET /api/tickets/getAllTickets
    @Test
    void testGetAllTickets() {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(5L);
        dto.setTitle("Server Down");

        when(ticketService.getAllTickets()).thenReturn(List.of(dto));

        ResponseEntity<List<TicketDTO>> result = ticketController.getAllTickets();

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(5L, result.getBody().get(0).getTicketId());
        assertEquals("Server Down", result.getBody().get(0).getTitle());
    }

    // ✅ Test PUT /api/tickets/{id}
    @Test
    void testUpdateTicket() {
        TicketDTO request = new TicketDTO();
        request.setTitle("Updated Title");

        TicketDTO response = new TicketDTO();
        response.setTicketId(20L);
        response.setTitle("Updated Title");

        when(ticketService.updateTicket(eq(20L), any(TicketDTO.class)))
                .thenReturn(response);

        ResponseEntity<TicketDTO> result =
                ticketController.updateTicket(20L, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(20L, result.getBody().getTicketId());
        assertEquals("Updated Title", result.getBody().getTitle());
    }


    @Test
    void testGetUserByRole() {
        User user = new User();
        user.setUserId(100);
        user.setUserName("AdminUser");
        user.setUserRole(Role.ADMIN);

        when(iUserService.getUserBYRole(Role.ADMIN))
                .thenReturn(List.of(user));

        ResponseEntity<List<?>> result =
                ticketController.getUser(Role.ADMIN);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(100, ((User) result.getBody().get(0)).getUserId());
        assertEquals("AdminUser", ((User) result.getBody().get(0)).getUserName());
    }
}
