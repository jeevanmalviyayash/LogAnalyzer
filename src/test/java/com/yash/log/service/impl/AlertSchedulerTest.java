package com.yash.log.service.impl;

import com.yash.log.Util.AlertScheduler;
import com.yash.log.Util.EmailService;
import com.yash.log.constants.Role;
import com.yash.log.entity.User;
import com.yash.log.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private AlertScheduler alertScheduler;

    @Mock
    private Logger log;

    private User adminUser1;
    private User adminUser2;
    private User nonAdminUser;

    @BeforeEach
    void setUp() {
        // Initialize test data
        adminUser1 = new User();
        adminUser1.setUserId(1);
        adminUser1.setUserEmail("admin1@example.com");
        adminUser1.setUserRole(Role.ADMIN);

        adminUser2 = new User();
        adminUser2.setUserId(2);
        adminUser2.setUserEmail("admin2@example.com");
        adminUser2.setUserRole(Role.ADMIN);

        nonAdminUser = new User();
        nonAdminUser.setUserId(3);
        nonAdminUser.setUserEmail("developer@example.com");
        nonAdminUser.setUserRole(Role.DEVELOPER);

        // Enable the scheduler by default
        ReflectionTestUtils.setField(alertScheduler, "enabled", true);
    }

    @Test
    void getAllAdminUser_ShouldReturnAdminUsersOnly() {
        // Arrange
        List<User> allUsers = Arrays.asList(adminUser1, adminUser2, nonAdminUser);
        when(userRepository.findByUserRole(Role.ADMIN))
                .thenReturn(Arrays.asList(adminUser1, adminUser2));

        // Act
        List<User> result = alertScheduler.getAllAdminUser();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(adminUser1, adminUser2);
        assertThat(result).doesNotContain(nonAdminUser);
        verify(userRepository).findByUserRole(Role.ADMIN);
    }

    @Test
    void dailyTask_WhenEnabledAndAdminUsersExist_ShouldSendEmailsToAllAdmins() {
        // Arrange
        List<User> adminUsers = Arrays.asList(adminUser1, adminUser2);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        // Verify emails were sent to both admins
        verify(emailService, times(2)).sendSimpleEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        List<String> capturedEmails = emailCaptor.getAllValues();
        List<String> capturedSubjects = subjectCaptor.getAllValues();
        List<String> capturedContents = contentCaptor.getAllValues();

        // Verify first admin
        assertThat(capturedEmails.get(0)).isEqualTo("admin1@example.com");
        assertThat(capturedEmails.get(1)).isEqualTo("admin2@example.com");

        // Verify subjects contain today's date
        String expectedSubject = "Daily Log Analysis Report - " + LocalDate.now();
        assertThat(capturedSubjects.get(0)).isEqualTo(expectedSubject);
        assertThat(capturedSubjects.get(1)).isEqualTo(expectedSubject);

        // Verify content
        assertThat(capturedContents.get(0)).isEqualTo("This is the daily log analysis report. Please check the dashboard for details.");
        assertThat(capturedContents.get(1)).isEqualTo("This is the daily log analysis report. Please check the dashboard for details.");
    }

    @Test
    void dailyTask_WhenEnabledButNoAdminUsers_ShouldLogWarningAndNotSendEmails() {
        // Arrange
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(Collections.emptyList());

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(userRepository).findByUserRole(Role.ADMIN);
        verifyNoInteractions(emailService);
    }

    @Test
    void dailyTask_WhenAdminUserHasNullEmail_ShouldSkipThatAdmin() {
        // Arrange
        User adminWithNullEmail = new User();
        adminWithNullEmail.setUserId(4);
        adminWithNullEmail.setUserEmail(null);
        adminWithNullEmail.setUserRole(Role.ADMIN);

        User adminWithEmptyEmail = new User();
        adminWithEmptyEmail.setUserId(5);
        adminWithEmptyEmail.setUserEmail("");
        adminWithEmptyEmail.setUserRole(Role.ADMIN);

        List<User> adminUsers = Arrays.asList(adminWithNullEmail, adminWithEmptyEmail, adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        // Should only send email to adminUser1
        verify(emailService, times(1)).sendSimpleEmail(
                eq("admin1@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void dailyTask_WhenAdminUserHasWhitespaceEmail_ShouldSkipThatAdmin() {
        // Arrange
        User adminWithWhitespaceEmail = new User();
        adminWithWhitespaceEmail.setUserId(6);
        adminWithWhitespaceEmail.setUserEmail("   ");
        adminWithWhitespaceEmail.setUserRole(Role.ADMIN);

        List<User> adminUsers = Arrays.asList(adminWithWhitespaceEmail, adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        // Should only send email to adminUser1
        verify(emailService, times(1)).sendSimpleEmail(
                eq("admin1@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void dailyTask_WhenDisabled_ShouldNotExecuteAnyLogic() {
        // Arrange
        ReflectionTestUtils.setField(alertScheduler, "enabled", false);

        // Act
        alertScheduler.dailyTask();

        // Assert
        verifyNoInteractions(userRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void dailyTask_WhenEmailServiceThrowsException_ShouldLogErrorButNotPropagate() {
        // Arrange
        List<User> adminUsers = Collections.singletonList(adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        RuntimeException emailException = new RuntimeException("SMTP error");
        doThrow(emailException).when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        // Act
        alertScheduler.dailyTask();

        // Assert
        // Verify email service was called
        verify(emailService).sendSimpleEmail(
                eq("admin1@example.com"),
                anyString(),
                anyString()
        );

        // The exception should be caught and logged, not propagated
        // (We can't easily verify the log call without capturing it)
    }

    @Test
    void dailyTask_WhenRepositoryThrowsException_ShouldLogErrorButNotPropagate() {
        // Arrange
        RuntimeException repoException = new RuntimeException("Database error");
        when(userRepository.findByUserRole(Role.ADMIN)).thenThrow(repoException);

        // Act
        alertScheduler.dailyTask();

        // Assert
        // Should not send any emails when repository fails
        verifyNoInteractions(emailService);

        // The exception should be caught and logged, not propagated
    }

    @Test
    void dailyTask_ShouldIncludeCurrentDateInEmailSubject() {
        // Arrange
        List<User> adminUsers = Collections.singletonList(adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        LocalDate today = LocalDate.now();
        String expectedSubject = "Daily Log Analysis Report - " + today;

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(emailService).sendSimpleEmail(
                eq("admin1@example.com"),
                eq(expectedSubject),
                anyString()
        );
    }

    @Test
    void dailyTask_ShouldSendCorrectEmailContent() {
        // Arrange
        List<User> adminUsers = Collections.singletonList(adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        String expectedContent = "This is the daily log analysis report. Please check the dashboard for details.";

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(emailService).sendSimpleEmail(
                anyString(),
                anyString(),
                eq(expectedContent)
        );
    }

    @Test
    void dailyTask_WhenMultipleAdmins_ShouldSendEmailToEachAdmin() {
        // Arrange
        List<User> adminUsers = Arrays.asList(adminUser1, adminUser2);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(emailService, times(2)).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(emailService).sendSimpleEmail(eq("admin1@example.com"), anyString(), anyString());
        verify(emailService).sendSimpleEmail(eq("admin2@example.com"), anyString(), anyString());
    }

    @Test
    void dailyTask_WithMixedValidAndInvalidEmails_ShouldOnlySendToValidOnes() {
        // Arrange
        User admin1 = new User();
        admin1.setUserId(1);
        admin1.setUserEmail("valid1@example.com");
        admin1.setUserRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setUserId(2);
        admin2.setUserEmail(null);
        admin2.setUserRole(Role.ADMIN);

        User admin3 = new User();
        admin3.setUserId(3);
        admin3.setUserEmail("");
        admin3.setUserRole(Role.ADMIN);

        User admin4 = new User();
        admin4.setUserId(4);
        admin4.setUserEmail("valid2@example.com");
        admin4.setUserRole(Role.ADMIN);

        List<User> adminUsers = Arrays.asList(admin1, admin2, admin3, admin4);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        // Should only send emails to admin1 and admin4
        verify(emailService, times(2)).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(emailService).sendSimpleEmail(eq("valid1@example.com"), anyString(), anyString());
        verify(emailService).sendSimpleEmail(eq("valid2@example.com"), anyString(), anyString());
    }

    @Test
    void dailyTask_WhenEnabledPropertyIsFalse_ShouldReturnImmediately() {
        // Arrange
        ReflectionTestUtils.setField(alertScheduler, "enabled", false);

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(userRepository, never()).findByUserRole(any());
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
    }

    @Test
    void dailyTask_WhenEnabledPropertyIsTrue_ShouldProceedWithExecution() {
        // Arrange
        ReflectionTestUtils.setField(alertScheduler, "enabled", true);
        List<User> adminUsers = Collections.singletonList(adminUser1);
        when(userRepository.findByUserRole(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        alertScheduler.dailyTask();

        // Assert
        verify(userRepository).findByUserRole(Role.ADMIN);
        verify(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
    }
}