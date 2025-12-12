package com.yash.log.serviceImpl;

import com.yash.log.Util.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;       // <-- AssertJ
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
        assertThrows(RuntimeException.class,
                () -> emailService.sendSimpleEmail("user@example.com", "Subject", "Body"));
    }

    // --- Helper to set private field since @Value doesn't run here ---
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }




    /** Utility: return a real MimeMessage so MimeMessageHelper works normally. */
    private MimeMessage realMimeMessage() {
        Session session = Session.getInstance(new Properties());
        return new MimeMessage(session);
    }

    @AfterEach
    void clearStrictProperty() {
        // cleanup in case a test enabled strict address parsing
        System.clearProperty("mail.mime.address.strict");
    }

    @Test
    void sendHtmlEmail_success_sendsMessage() {
        // Arrange
        MimeMessage message = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        String to = "user@example.com";
        String subject = "Hello";
        String html = "<h1>Hi</h1>";

        // Act
        emailService.sendHtmlEmail(to, subject, html);

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        assertSame(message, captor.getValue(), "Should send the same MimeMessage instance created");
    }


    @Test
    void sendHtmlEmail_nullRecipient_throwsRuntimeException() {
        // Arrange
        String subject = "Subject";
        String html = "<p>Hi</p>"; // no need to escape

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendHtmlEmail(null, subject, html));

        assertTrue(
                ex.getMessage() != null && ex.getMessage().contains("Failed to send HTML email"),
                "Expected message to contain 'Failed to send HTML email', but was: " + ex.getMessage()
        );

        // Ensure send() was never called due to early failure
        verify(mailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void sendHtmlEmail_invalidRecipientStrict_throwsRuntimeException() {
        // Arrange: enable strict address parsing to force MessagingException for invalid address
        System.setProperty("mail.mime.address.strict", "true");
        MimeMessage message = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        String invalidTo = "bad\r\n@example.com"; // invalid under strict parsing
        String subject = "Subject";
        String html = "<p>Hi</p>";

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendHtmlEmail(invalidTo, subject, html));
        assertTrue(ex.getMessage().contains("Failed to send HTML email"));

        // Ensure send() was never called due to early failure
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlEmail_mailSenderThrows_wrappedRuntimeException() {
        // Arrange
        MimeMessage message = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Cause mailSender.send(...) to throw
        doThrow(new MailSendException("SMTP down"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendHtmlEmail("user@example.com", "S", "<p>x</p>"));
        assertTrue(ex.getMessage().contains("Failed to send HTML email"));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }


}
