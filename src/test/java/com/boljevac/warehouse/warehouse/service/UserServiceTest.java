package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.dto.UserResponse;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.exception.UserDoubleCreationException;
import com.boljevac.warehouse.user.exception.UserIncorrectCredentialsException;
import com.boljevac.warehouse.user.exception.UserNotFoundException;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Spy
	private ModelMapper modelMapper;

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String HASHED_PASSWORD = "hashedPassword";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	private static final String UPDATED_EMAIL = "newtestuser@mail.com";
	private static final String UPDATED_USERNAME = "newTester";
	private static final String UPDATED_NAME = "newTestName";
	private static final String UPDATED_SURNAME = "newTestSurname";

	private static final Role USER_ROLE = Role.USER;
	private static final Role ADMIN_ROLE = Role.ADMIN;
	private static final Long USER_ID = 1L;


	@BeforeEach
	public void setUp() {
		userService = new UserService(userRepository, orderRepository, passwordEncoder, modelMapper);
	}

	//Customer Methods Tests

	@Test
	public void registerUserByCustomer_whenEmailIsAvailable_returnUserResponse() {
		UserRequest newUser = userRequestUser();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);

		when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity user = invocation.getArgument(0);
			user.setId(USER_ID);
			return user;
		});

		UserResponse response = userService.registerUserByCustomer(newUser);

		assertEquals(EMAIL, response.getEmail());
		assertEquals(NAME, response.getName());
		assertEquals(SURNAME, response.getSurname());

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder).encode(PASSWORD);
		verify(userRepository).save(any(UserEntity.class));
	}

	@Test
	public void registerUserByCustomer_whenEmailIsAlreadyRegistered_throwsUserDoubleCreatedException() {
		UserRequest newUser = userRequestUser();
		UserEntity existingUser = new UserEntity();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

		assertThatThrownBy(() -> userService.registerUserByCustomer(newUser))
				.isInstanceOf(UserDoubleCreationException.class)
				.hasMessage("User with " + EMAIL + " already exists");

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder, never()).encode(PASSWORD);
		verify(userRepository, never()).save(any(UserEntity.class));

	}

	@Test
	public void updateUserByCustomer_updateIsValid_updatesUser() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		existingUser.setPassword(PASSWORD);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(passwordEncoder.matches(PASSWORD, existingUser.getPassword())).thenReturn(true);

		userService.updateUserByCustomer(authenticatedUser, updatedValues);

		verify(userRepository).save(existingUser);
		verify(userRepository).findById(USER_ID);
		verify(passwordEncoder).matches(PASSWORD, existingUser.getPassword());

		assertEquals(UPDATED_NAME, existingUser.getName());
		assertEquals(UPDATED_USERNAME, existingUser.getUsername());
		assertEquals(UPDATED_EMAIL, existingUser.getEmail());
		assertEquals(UPDATED_SURNAME, existingUser.getSurname());

	}

	@Test
	public void updateUserByCustomer_whenPasswordDoesntMatch_throwsUserIncorrectCredentialsException() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		existingUser.setPassword(PASSWORD);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(passwordEncoder.matches(updatedValues.getPassword(), existingUser.getPassword())).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserByCustomer(authenticatedUser, updatedValues))
				.isInstanceOf(UserIncorrectCredentialsException.class)
				.hasMessage("Invalid password");

		verify(userRepository, never()).save(existingUser);
		verify(userRepository).findById(USER_ID);
		verify(passwordEncoder).matches(updatedValues.getPassword(), existingUser.getPassword());

	}
	//Management Method Tests

	@Test
	public void registerManagement_whenEmailIsValid_registersUser() {
		UserRequest newUser = userRequestUser();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);

		when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity user = invocation.getArgument(0);
			user.setId(USER_ID);
			return user;
		});

		UserResponse response = userService.registerManagement(newUser, ADMIN_ROLE);

		assertEquals(EMAIL, response.getEmail());
		assertEquals(NAME, response.getName());
		assertEquals(SURNAME, response.getSurname());

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder).encode(PASSWORD);
		verify(userRepository).save(any(UserEntity.class));

	}

	@Test
	public void updateUserByManagement_whenUserIsFound_updatesUser() {
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		userService.updateUserByManagement(USER_ID, updatedValues, ADMIN_ROLE);

		assertEquals(ADMIN_ROLE, existingUser.getRole());
		assertEquals(UPDATED_NAME, existingUser.getName());
		assertEquals(UPDATED_USERNAME, existingUser.getUsername());
		assertEquals(UPDATED_EMAIL, existingUser.getEmail());
		assertEquals(UPDATED_SURNAME, existingUser.getSurname());

		verify(userRepository).findById(USER_ID);
		verify(userRepository).save(existingUser);

	}

	@Test
	public void updateUserByManagement_whenUserIsNotFound_throwsUserNotFoundException() {
		UserRequest updatedValues = userRequestUpdatedUser();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUserByManagement(USER_ID, updatedValues, ADMIN_ROLE))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessage("User with id " + USER_ID + " was not found");

		verify(userRepository).findById(USER_ID);
		verify(userRepository, never()).save(any(UserEntity.class));

	}

	//Helper Methods

	private UserRequest userRequestUser() {
		return new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
	}

	private UserRequest userRequestUpdatedUser() {
		return new UserRequest(UPDATED_EMAIL, PASSWORD, UPDATED_USERNAME, UPDATED_NAME, UPDATED_SURNAME);
	}

	private AuthenticatedUser authenticatedUser() {
		return new AuthenticatedUser(USER_ID, EMAIL, USER_ROLE);
	}


}
