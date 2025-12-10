package com.yash.log.controller;

import com.yash.log.constants.Role;
import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.User;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.impl.IUserService;
import com.yash.log.service.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {


    private final TicketService ticketService;
    private final ErrorLogRepository errorLogRepository;
    @Autowired
    private IUserService iUserService;

    public TicketController(TicketService ticketService, ErrorLogRepository errorLogRepository) {
        this.ticketService = ticketService;
        this.errorLogRepository=errorLogRepository;

    }

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO ticketDTO) {

        return ResponseEntity.ok(ticketService.createTicket(ticketDTO));
    }

    @GetMapping("user-id/{assignee}")
    public ResponseEntity<List<TicketDTO>> getTicketByUserId(@PathVariable String assignee) {

        return ResponseEntity.ok(ticketService.findAllTicketBYAssignedTo(assignee));
    }


    @GetMapping("/getAllTickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }


    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDto));
    }


    @GetMapping("/user/{role}")
    public ResponseEntity<List<?>> getUser(@PathVariable Role role) {
        List<User> userList = iUserService.getUserBYRole(role);

        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }
}
