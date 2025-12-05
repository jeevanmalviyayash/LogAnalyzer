package com.yash.log.Util;

import com.yash.log.constants.Role;
import com.yash.log.entity.User;
import com.yash.log.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class AlertScheduler {

    @Value("${alert.enabled:true}")
    private boolean enabled;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IUserRepository userRepository;

    public List<User> getAllAdminUser(){
        List<User> adminUser =userRepository.findByUserRole(Role.ADMIN);
        return adminUser;
    }

    //@Scheduled(cron = "0 2 10 * * ?", zone = "Asia/Kolkata") // 10:02 am IST
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Kolkata") // 9am IST
    public void dailyTask() {
        if (!enabled) return; // Runtime check for dynamic toggling to On and Off the Alert
        try{
            // Get all admin users
            List<User> adminUsers = getAllAdminUser();
            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to send daily report email");
                return;
            }
            //email to each admin
            for (User admin : adminUsers) {
                String adminEmail = admin.getUserEmail(); // Assuming User entity has getEmail()
                if (adminEmail != null && !adminEmail.trim().isEmpty()) {
                    // Prepare common email content
                    String subject = "Daily Log Analysis Report - " + LocalDate.now();
                    String content = "This is the daily log analysis report. Please check the dashboard for details.";

                    emailService.sendSimpleEmail(adminEmail, subject, content);
                    log.info("Daily report sent to admin: {}", adminEmail);
                }
            }

            log.info("Daily task executed successfully at: {}, Sent to {} admin users",
                    LocalDateTime.now(), adminUsers.size());

        }catch(Exception e){
            log.error("Error in sending alert email: ", e);
        }
    }

}
