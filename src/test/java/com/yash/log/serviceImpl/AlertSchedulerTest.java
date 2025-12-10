package com.yash.log.serviceImpl;

import com.yash.log.Util.AlertScheduler;
import com.yash.log.Util.EmailService;
import com.yash.log.constants.Role;
import com.yash.log.constants.Status;
import com.yash.log.entity.Log;
import com.yash.log.entity.User;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.repository.IUserRepository;
import com.yash.log.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ErrorLogRepository errorLogRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AlertScheduler alertScheduler;

    @BeforeEach
    void init() {
        // Set @Value-injected fields explicitly
        ReflectionTestUtils.setField(alertScheduler, "enabled", true);
        ReflectionTestUtils.setField(alertScheduler, "applicationName", "TestApp");
    }

    // --------- Helpers ---------
    private Log logWithType(String type) {
        Log l = new Log();
        l.setErrorType(type);
        return l;
    }

    private User admin(String email) {
        User u = new User();
        u.setUserRole(Role.ADMIN);
        u.setUserEmail(email);
        return u;
    }

    // --------- Tests for dailyTask ---------

    @Test
    void dailyTask_Disabled_NoActions() {
        ReflectionTestUtils.setField(alertScheduler, "enabled", false);

        // Act
        alertScheduler.dailyTask();

        // Assert: no emails, minimal repository usage
        verifyNoInteractions(emailService);
        // It returns early, so we avoid asserting repo interactions
    }

    @Test
    void dailyTask_NoSignificantErrors_SkipsEmail() {
        // error types: DB (10), NPE (5) -> none > 10
        when(errorLogRepository.findAll())
                .thenReturn(List.of(logWithType("DB"), logWithType("NPE")));

        when(errorLogRepository.countByErrorType("DB")).thenReturn(10L);
        when(errorLogRepository.countByErrorType("NPE")).thenReturn(5L);

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(errorLogRepository).findAll();
        verify(errorLogRepository).countByErrorType("DB");
        verify(errorLogRepository).countByErrorType("NPE");
        verifyNoInteractions(emailService);
        // findAllWithTicketStatus() should not be called when significant types empty
        verify(errorLogRepository, never()).findAllWithTicketStatus(any(Status.class));
    }

    @Test
    void dailyTask_NoOpenTickets_SkipsEmail() {
        // Significant errors exist: DB (15), NPE (25)
        when(errorLogRepository.findAll())
                .thenReturn(List.of(logWithType("DB"), logWithType("NPE")));

        when(errorLogRepository.countByErrorType("DB")).thenReturn(15L);
        when(errorLogRepository.countByErrorType("NPE")).thenReturn(25L);

        // No open tickets
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of());

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(errorLogRepository).findAll();
        verify(errorLogRepository).countByErrorType("DB");
        verify(errorLogRepository).countByErrorType("NPE");
        verify(errorLogRepository).findAllWithTicketStatus(Status.OPEN);
        verifyNoInteractions(emailService);
    }

    @Test
    void dailyTask_NoIntersectionBetweenSignificantAndOpenTicketTypes_SkipsEmail() {
        // Significant: DB (20)
        when(errorLogRepository.findAll()).thenReturn(List.of(logWithType("DB")));
        when(errorLogRepository.countByErrorType("DB")).thenReturn(20L);

        // Open tickets have different type: NETWORK
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of(logWithType("NETWORK")));

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(errorLogRepository).findAll();
        verify(errorLogRepository).countByErrorType("DB");
        verify(errorLogRepository).findAllWithTicketStatus(Status.OPEN);
        verifyNoInteractions(emailService);
    }

    @Test
    void dailyTask_AdminsEmpty_SkipsEmail() {
        // Significant and intersection present: DB
        when(errorLogRepository.findAll()).thenReturn(List.of(logWithType("DB")));
        when(errorLogRepository.countByErrorType("DB")).thenReturn(30L);
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of(logWithType("DB")));

        // No admins
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(List.of());

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(errorLogRepository).findAll();
        verify(errorLogRepository).countByErrorType("DB");
        verify(errorLogRepository).findAllWithTicketStatus(Status.OPEN);
        verify(userRepository).findByUserRole(Role.ADMIN);
        verifyNoInteractions(emailService);
    }

    @Test
    void dailyTask_HappyPath_SendsEmailToAllAdminsWithValidEmails() {
        // Significant and intersection present
        when(errorLogRepository.findAll()).thenReturn(List.of(
                logWithType("DB"),
                logWithType("NPE"),
                logWithType("NETWORK"))
        );
        when(errorLogRepository.countByErrorType("DB")).thenReturn(50L);
        when(errorLogRepository.countByErrorType("NPE")).thenReturn(8L);
        when(errorLogRepository.countByErrorType("NETWORK")).thenReturn(12L);

        // Open tickets with DB and NETWORK
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of(
                        logWithType("DB"),
                        logWithType("NETWORK"))
                );

        // Admins with two valid emails
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(List.of(
                admin("admin1@yash.com"),
                admin("admin2@yash.com"))
        );

        // Act
        alertScheduler.dailyTask();

        // Assert: two emails sent
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(2))
                .sendHtmlEmail(toCaptor.capture(), subjectCaptor.capture(), bodyCaptor.capture());

        // Subject should match the fixed value in sendAlertEmail
        subjectCaptor.getAllValues().forEach(s ->
                org.junit.jupiter.api.Assertions.assertEquals(
                        "[ALERT] Error Threshold Exceeded in Log Analyzer", s));

        // Recipients should be the admins
        List<String> recipients = toCaptor.getAllValues();
        org.junit.jupiter.api.Assertions.assertTrue(recipients.containsAll(
                Set.of("admin1@yash.com", "admin2@yash.com")));

        // Body should be non-empty (exact format is internal)
        bodyCaptor.getAllValues().forEach(b ->
                org.junit.jupiter.api.Assertions.assertNotNull(b));
    }

    @Test
    void dailyTask_BlankOrNullAdminEmails_AreFilteredOut_NoEmailSentToThem() {
        // Significant + intersection for DB
        when(errorLogRepository.findAll()).thenReturn(List.of(logWithType("DB")));
        when(errorLogRepository.countByErrorType("DB")).thenReturn(40L);
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of(logWithType("DB")));

        // Admin list contains valid and invalid emails
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(List.of(
                admin("valid@yash.com"),
                admin(""),             // blank
                admin("   "),          // whitespace
                admin(null)            // null
        ));

        alertScheduler.dailyTask();

        // Only one valid email should be used
        verify(emailService, times(1))
                .sendHtmlEmail(eq("valid@yash.com"),
                        eq("[ALERT] Error Threshold Exceeded in Log Analyzer"),
                        anyString());

        // Ensure no other calls
        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void dailyTask_EmailServiceThrows_IsCaught_NoExceptionPropagated() {
        // Significant + intersection for DB
        when(errorLogRepository.findAll()).thenReturn(List.of(logWithType("DB")));
        when(errorLogRepository.countByErrorType("DB")).thenReturn(40L);
        when(errorLogRepository.findAllWithTicketStatus(Status.OPEN))
                .thenReturn(List.of(logWithType("DB")));

        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(List.of(admin("admin@yash.com")));

        doThrow(new RuntimeException("SMTP failure"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> alertScheduler.dailyTask());

        verify(emailService, times(1))
                .sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // --------- Pass-through method tests ---------

    @Test
    void getAllAdminUser_DelegatesToRepo() {
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(List.of(admin("a@yash.com")));
        List<User> result = alertScheduler.getAllAdminUser();
        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        verify(userRepository, times(1)).findByUserRole(Role.ADMIN);
    }

    @Test
    void getErrorCountByType_DelegatesToRepo() {
        when(errorLogRepository.countByErrorType("DB")).thenReturn(123L);
        Long count = alertScheduler.getErrorCountByType("DB");
        org.junit.jupiter.api.Assertions.assertEquals(123L, count);
        verify(errorLogRepository, times(1)).countByErrorType("DB");
    }
}
