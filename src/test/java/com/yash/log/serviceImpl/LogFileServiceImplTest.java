package com.yash.log.serviceImpl;

import com.yash.log.entity.Log;
import com.yash.log.repository.ErrorLogRepository;
import com.yash.log.service.impl.LogFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogFileServiceImplTest {

//    @Test
//    void myFirstTest() {
//        System.out.println("My test is running");
//    }
    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private LogFileServiceImpl logService; // Your service class containing parseAndSaveLogs

    @Test
    public void testParseAndSaveLogs() throws Exception {
        // Prepare a MockMultipartFile with your sample log content
        String logContent = "2025-11-17T16:23:35.059+05:30  INFO 17460 --- [LOG] [  restartedMain] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)\n";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "log.txt",
                "text/plain",
                logContent.getBytes(StandardCharsets.UTF_8)
        );

        // Call the method under test
        logService.parseAndSaveLogs(multipartFile);

        // Verify save is called on the repository
        verify(errorLogRepository, times(1)).save(any(Log.class));
    }

    // another method



}
