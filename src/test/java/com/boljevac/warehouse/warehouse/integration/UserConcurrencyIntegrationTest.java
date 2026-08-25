package com.boljevac.warehouse.warehouse.integration;

import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.exception.UserDoubleCreationException;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ActiveProfiles("postgres-test")
public class UserConcurrencyIntegrationTest {

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	public void tearDown() {
		userRepository.findByEmail(EMAIL)
				.ifPresent(user -> userRepository.delete(user));
	}

	@Test
	public void concurrentUserCreationByUserTest_usingSameEmail_throwsUserDoubleCreationException() throws Exception {
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		ExecutorService executor =
				Executors.newFixedThreadPool(2);

		Callable<Boolean> task = () -> {

			ready.countDown();

			start.await();

			try {
				createUserByUser();
				return true;
			} catch (UserDoubleCreationException exception) {
				return false;
			}
		};

		Future<Boolean> a = executor.submit(task);
		Future<Boolean> b = executor.submit(task);

		ready.await();

		start.countDown();

		boolean aSucceeded = a.get();
		boolean bSucceeded = b.get();

		executor.shutdown();

		long successCount =
				Stream.of(aSucceeded, bSucceeded)
						.filter(Boolean::booleanValue)
						.count();

		assertEquals(1, successCount);
		assertEquals(1, userRepository.countByEmail(EMAIL));


	}

	@Test
	public void concurrentUserCreationByManagementTest_usingSameEmail_throwsUserDoubleCreationException() throws Exception {
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		ExecutorService executor =
				Executors.newFixedThreadPool(2);

		Callable<Boolean> task = () -> {

			ready.countDown();

			start.await();

			try {
				createUserByManagement();
				return true;
			} catch (UserDoubleCreationException exception) {
				return false;
			}
		};

		Future<Boolean> a = executor.submit(task);
		Future<Boolean> b = executor.submit(task);

		ready.await();

		start.countDown();

		boolean aSucceeded = a.get();
		boolean bSucceeded = b.get();

		executor.shutdown();

		long successCount =
				Stream.of(aSucceeded, bSucceeded)
						.filter(Boolean::booleanValue)
						.count();

		assertEquals(1, successCount);
		assertEquals(1, userRepository.countByEmail(EMAIL));


	}

	private void createUserByUser() {
		UserRequest userRequest = new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
		userService.registerUserByCustomer(userRequest);
	}

	private void createUserByManagement() {
		UserRequest userRequest = new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
		userService.registerManagement(userRequest, Role.USER);
	}


}
