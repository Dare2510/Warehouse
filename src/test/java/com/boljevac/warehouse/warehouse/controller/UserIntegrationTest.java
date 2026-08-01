package com.boljevac.warehouse.warehouse.controller;

import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
public class UserIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private static final String USER_MAIL = "testuser@mail.com";
	private static final String USER_USERNAME = "testUser";
	private static final String USER_FIRST_NAME = "testUserFirstName";
	private static final String USER_SURNAME = "testUserSurname";

	private static final String UPDATED_USER_MAIL = "updatedtestuser@mail.com";
	private static final String UPDATED_USER_FIRST_NAME = "updateName";
	private static final String UPDATED_USER_USERNAME = "updateUser";
	private static final String UPDATED_USER_SURNAME = "updatedTestUserName";

	private static final String PASSWORD = "password";
	private static final String WRONG_PASSWORD = "Wrong password";

	private static final Role USER_ROLE = Role.USER;

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}


	//Customer Tests

	@Test
	public void registerUser_whenMailIsAvailable_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(USER_MAIL))
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(USER_FIRST_NAME))
				.andExpect(jsonPath("$.surname").value(USER_SURNAME))
				.andExpect(jsonPath("$.role").doesNotExist());


	}

	@Test
	public void registerUser_whenMailIsNotAvailable_returnsBadRequest() throws Exception {
		UserRequest newUser = userRequest();

		postUserRegistration(newUser);

		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("User with " + USER_MAIL + " already exists"));

	}

	@Test
	public void updateUser_whenPasswordIsCorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		setAuthentication(authentication);

		UserRequest updatedUser = updateUserRequest();

		mockMvc.perform(patch("/api/user/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk());
	}

	@Test
	public void updateUser_whenPasswordIsIncorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UsernamePasswordAuthenticationToken authentication = authenticationToken(userId);
		setAuthentication(authentication);

		UserRequest updatedUser = updateUserRequest();
		updatedUser.setPassword(WRONG_PASSWORD);

		mockMvc.perform(patch("/api/user/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid password"));

	}

	//Staff/Admin Tests

	@Test
	public void registerUserByManagement_emailIsAvailable_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		mockMvc.perform(post("/api/management/user/register/" + USER_ROLE.name())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(USER_MAIL))
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(USER_FIRST_NAME))
				.andExpect(jsonPath("$.surname").value(USER_SURNAME))
				.andExpect(jsonPath("$.role").value(USER_ROLE.name()));
	}

	@Test
	public void updateUserByManagement_whenPasswordIsCorrect_returnsOK() throws Exception {
		UserRequest newUser = userRequest();

		Long userId = registerUserAndGetId(newUser);

		UserRequest updatedUser = updateUserRequest();

		mockMvc.perform(patch("/api/management/user/" + userId + "/" + USER_ROLE.name() + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedUser)))
				.andExpect(status().isOk());
	}

	@Test
	public void getPageOfUsers_withPageableDefaults_returnIsOK() throws Exception {
		mockMvc.perform(get("/api/management/user")
						.param("page", "0")
						.param("size", "10")
						.param("sort", "surname")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(10));
	}


	private UserRequest userRequest() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private UserRequest updateUserRequest() {
		return new UserRequest(UPDATED_USER_MAIL, PASSWORD, UPDATED_USER_USERNAME, UPDATED_USER_FIRST_NAME, UPDATED_USER_SURNAME);
	}

	//Endpoint Helpers

	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
		String userJson = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
	}

	private void postUserRegistration(UserRequest registrationRequest) throws Exception {
		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registrationRequest)))
				.andExpect(status().isOk());
	}

	//User Authenticator

	public UsernamePasswordAuthenticationToken authenticationToken(Long userId) {
		AuthenticatedUser principal = new AuthenticatedUser(userId, USER_MAIL, USER_ROLE);


		return new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + USER_ROLE.name())));

	}

	public void setAuthentication(UsernamePasswordAuthenticationToken authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}

