package com.yash.log;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "Log Analysis REST API Documentation",
				description = "This is Log Analysis REST API Documentation",
				version = "v1",
				contact = @Contact(
								name = "Nihalahmad Aslam Sherkar",
								email = "nihalahmad.shekar@yash.com"
						)
		)
)

@SpringBootApplication
public class LogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogApplication.class, args);
	}

}
