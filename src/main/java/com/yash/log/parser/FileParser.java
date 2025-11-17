package com.yash.log.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

// Currently not using this class, but it can be useful for future enhancements


@Component
public class FileParser {
    public Object parse(String filePath){
        try{

            Resource resource =new ClassPathResource(filePath.replace("classpath:",""));
            if (filePath.endsWith(".log")){
                ObjectMapper objectMapper= new ObjectMapper();
                return objectMapper.readTree(resource.getInputStream());
            }
            else if (filePath.endsWith(".xml")) {
                // XML parsing logic
            } else if (filePath.endsWith(".yaml")) {
                Yaml yaml = new Yaml();
                return yaml.load(resource.getInputStream());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file: "+   e);
        }

        return null;
    }



}
