package com.testassignment.bank;

import com.testassignment.bank.controller.AccountController;
import com.testassignment.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BankApplicationTests {

	/**
	 * This test uses bootRun's database, it fails when application is running while running the test.
	 * **/
	@Test
	void contextLoads() {
		BankApplication application = new BankApplication();
		assertThat(application).isNotNull();
	}
}
