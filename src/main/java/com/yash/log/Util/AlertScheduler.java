package com.yash.log.Util;
import com.yash.log.constants.Role;
import com.yash.log.constants.Status;
import com.yash.log.entity.Log;
import com.yash.log.entity.Ticket;
import com.yash.log.entity.User;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.repository.IUserRepository;
import com.yash.log.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
public class AlertScheduler {

    @Value("${alert.enabled:true}")
    private boolean enabled;

    @Value("${alert.application.name:N/A}")
    private String applicationName;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    public List<User> getAllAdminUser(){
        List<User> adminUser =userRepository.findByUserRole(Role.ADMIN);
        return adminUser;
    }

    public Long getErrorCountByType(String errorType) {
        return errorLogRepository.countByErrorType(errorType);
    }

    //@Scheduled(cron = "0 2 10 * * ?", zone = "Asia/Kolkata") // 10:02 am IST
    @Transactional(readOnly = true)
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Kolkata") // 9am IST
    public void dailyTask() {
        if (!enabled) {
            log.info("Email alerts are disabled via configuration.");
            return;
        }
        try{
            LocalDateTime now = LocalDateTime.now();
            log.info("Starting daily log analysis at: {}", now);
            // 1) Significant errors by type (>10)
            // create a method to get all the errorType from the log table
            List<String> errorTypes = errorLogRepository.findAll().stream()
                    .map(Log::getErrorType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            //get the each errorType count and save it to a map only if having count > 10
            Map<String, Long> errorTypeCountMap = errorTypes.stream()
                    .collect(Collectors.toMap(
                            errorType -> errorType,
                            this::getErrorCountByType
                    ));


            // 2)Derive significant error types (count > 10)
            Set<String> significantErrorTypes = errorTypeCountMap.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue() > 10)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            // If no errorType has count > 10, skip
            if (significantErrorTypes.isEmpty()) {
                log.info("No error types above threshold (>10).");
                return;
            }

            // 3)Logs linked to OPEN tickets
            List<Log> logsWithOpenTickets = errorLogRepository.findAllWithTicketStatus(Status.OPEN);
            if (logsWithOpenTickets.isEmpty()) {
                log.info("No logs linked to OPEN tickets.");
                return;
            }

            // 4) Get error types from OPEN tickets that are also significant (>10)
            Set<String> openTicketErrorTypes = logsWithOpenTickets.stream()
                    .map(Log::getErrorType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Intersection: error types that are both significant AND have OPEN tickets
            Set<String> alertErrorTypes = significantErrorTypes.stream()
                    .filter(openTicketErrorTypes::contains)
                    .collect(Collectors.toSet());

            if (alertErrorTypes.isEmpty()) {
                log.info("No error types both exceeding threshold and linked to OPEN tickets. Skipping email.");
                return;
            }

            // Calculate total error count for alert error types
//            long totalErrorCount = alertErrorTypes.stream()
//                    .mapToLong(errorTypeCountMap::get)
//                    .sum();

            // Get admin users
            List<User> adminUsers = getAllAdminUser();
            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to send daily report email");
                return;
            }


            // Prepare and send emails
            for (User admin : adminUsers) {
                String adminEmail = admin.getUserEmail();
                if (adminEmail != null && !adminEmail.trim().isEmpty()) {
                    sendAlertEmail(adminEmail, alertErrorTypes, errorTypeCountMap, logsWithOpenTickets, now);
                    log.info("Alert email sent to admin: {}", adminEmail);
                }
            }


            log.info("Daily alert execution completed. Sent to {} admin users", adminUsers.size());

        }catch(Exception e){
            log.error("Error in sending alert email: ", e);
        }
    }


    private void sendAlertEmail(String toEmail,
                                Set<String> alertErrorTypes,
                                Map<String, Long> errorTypeCountMap,
                                List<Log> logsWithOpenTickets,
                                LocalDateTime timestamp) {

        // Prepare subject
        String subject = "[ALERT] Error Threshold Exceeded in Log Analyzer";

        // Prepare email body
        String emailBody = buildEmailBody(alertErrorTypes, errorTypeCountMap, logsWithOpenTickets, timestamp);

        // Send email
        emailService.sendHtmlEmail(toEmail, subject, emailBody);
    }


    private String buildEmailBody(Set<String> alertErrorTypes,
                                  Map<String, Long> errorTypeCountMap,
                                  List<Log> logsWithOpenTickets,
                                  LocalDateTime timestamp) {

        StringBuilder html = new StringBuilder();

        // Calculate total error count
        long totalErrorCount = alertErrorTypes.stream()
                .mapToLong(errorTypeCountMap::get)
                .sum();


        // Use HTML 4.01 Transitional for maximum compatibility
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        html.append("<html>");
        html.append("<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 15px; background-color: #ffffff;\">");

        // Email header
        html.append("<div style=\"margin-bottom: 15px;\">");
        html.append("<p style=\"margin: 3px 0;\"><strong>Dear Team,</strong></p>");
        html.append("<p style=\"margin: 3px 0;\">The log monitoring system has detected an unusual spike in errors.</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Application:</strong> ").append(escapeHtml(applicationName)).append("</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Environment:</strong> Development</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Timestamp:</strong> ").append(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Error Count:</strong> ").append(totalErrorCount).append("</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Threshold:</strong> 10</p>");
        html.append("<p style=\"margin: 3px 0;\"><strong>Status:</strong> <span style=\"color: #dc3545; font-weight: bold;\">ALERT - Error count has crossed the threshold.</span></p>");
        html.append("</div>");

        // Error Table - Small and compact
        html.append("<h3 style=\"margin-top: 15px; margin-bottom: 8px; font-size: 14px;\">Error Details:</h3>");
        html.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" style=\"width: auto; border-collapse: collapse; border: 1px solid #dc3545; margin: 10px 0; background-color: #ffffff; font-size: 13px;\">");

        // Table header
        html.append("<thead>");
        html.append("<tr style=\"background-color: #f8f9fa;\">");
        html.append("<th style=\"border: 1px solid #dee2e6; text-align: left; padding: 6px 8px; font-weight: bold; font-size: 12px;\">Error Type</th>");
        html.append("<th style=\"border: 1px solid #dee2e6; text-align: center; padding: 6px 8px; font-weight: bold; font-size: 12px;\">Count</th>");
        html.append("<th style=\"border: 1px solid #dee2e6; text-align: left; padding: 6px 8px; font-weight: bold; font-size: 12px;\">Status</th>");
        html.append("</tr>");
        html.append("</thead>");

        // Table body
        html.append("<tbody>");

        List<String> sortedErrorTypes = alertErrorTypes.stream()
                .sorted((a, b) -> Long.compare(errorTypeCountMap.getOrDefault(b, 0L),
                        errorTypeCountMap.getOrDefault(a, 0L)))
                .toList();

        for (String errorType : sortedErrorTypes) {
            Long count = errorTypeCountMap.getOrDefault(errorType, 0L);
            html.append("<tr style=\"background-color: #fff5f5;\">");
            html.append("<td style=\"border: 1px solid #dee2e6; padding: 6px 8px; max-width: 200px; word-wrap: break-word;\">").append(escapeHtml(errorType)).append("</td>");
            html.append("<td style=\"border: 1px solid #dee2e6; padding: 6px 8px; text-align: center;\"><strong>").append(count).append("</strong></td>");
            html.append("<td style=\"border: 1px solid #dee2e6; padding: 6px 8px;\"><span style=\"color: #dc3545; font-weight: bold;\">ALERT</span></td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Recommended Actions
        html.append("<h3 style=\"margin-top: 15px; margin-bottom: 8px; font-size: 14px;\">Recommended Action:</h3>");
        html.append("<ul style=\"margin-left: 15px; padding-left: 0; font-size: 13px;\">");
        html.append("<li style=\"margin-bottom: 5px;\">Investigate the root cause immediately.</li>");
        html.append("<li style=\"margin-bottom: 5px;\">Check recent deployments or configuration changes.</li>");
        html.append("<li style=\"margin-bottom: 5px;\">Review application logs for detailed stack traces.</li>");
        html.append("</ul>");

        // Footer
        html.append("<div style=\"margin-top: 20px; padding-top: 15px; border-top: 1px solid #eeeeee; color: #666666; font-size: 11px;\">");
        html.append("<p style=\"margin: 3px 0;\">This is an automated alert generated by the Log Analyzer system.</p>");
        html.append("<p style=\"margin: 3px 0;\">Regards,<br>Log Analyzer Monitoring Service</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }


    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
