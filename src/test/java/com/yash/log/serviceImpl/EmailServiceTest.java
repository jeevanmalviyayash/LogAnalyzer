package com.yash.log.serviceImpl;

import com.yash.log.Util.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import static org.assertj.core.api.Assertions.assertThat;       // <-- AssertJ
import static org.mockito.Mockito.verify;                       // <-- Mockito
import static org.mockito.Mockito.doThrow;                      // <-- Mockito

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    // We’ll set fromEmail via reflection since @Value won’t work outside Spring context
    @InjectMocks
    private EmailService emailService;

    @Test
    void sendSimpleEmail_ShouldConstructMessageAndInvokeSender() throws Exception {
        // Arrange
        setField(emailService, "fromEmail", "no-reply@example.com");
        String to = "user@example.com";
        String subject = "Test Subject";
        String text = "Hello from unit test.";

        // Act
        emailService.sendSimpleEmail(to, subject, text);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getFrom()).isEqualTo("no-reply@example.com");
        assertThat(msg.getTo()).containsExactly("user@example.com");
        assertThat(msg.getSubject()).isEqualTo("Test Subject");
        assertThat(msg.getText()).isEqualTo("Hello from unit test.");
    }

    @Test
    void sendSimpleEmail_WhenMailSenderThrows_ShouldPropagateException() throws Exception {
        // Arrange
        setField(emailService, "fromEmail", "no-reply@example.com");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> emailService.sendSimpleEmail("user@example.com", "Subject", "Body"));
    }

    // --- Helper to set private field since @Value doesn't run here ---
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
