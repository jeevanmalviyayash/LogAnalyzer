package com.yash.log.service.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface LogFileService {

    void parseAndSaveLogs(MultipartFile file) throws IOException;
}
