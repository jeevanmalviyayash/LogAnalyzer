package com.yash.log;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LogApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void testMainMethod() {
		LogApplication.main(new String[]{});
	}
}
