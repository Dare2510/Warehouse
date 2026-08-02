//package com.boljevac.warehouse.warehouse.controller;
//
//import com.boljevac.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
//import com.boljevac.warehouse.location.controller.LocationsController;
//import com.boljevac.warehouse.location.dto.LocationsRequest;
//import com.boljevac.warehouse.location.dto.LocationsResponse;
//import com.boljevac.warehouse.location.exceptions.LocationLoadLimitExceededException;
//import com.boljevac.warehouse.location.exceptions.LocationsAlreadyCreatedException;
//import com.boljevac.warehouse.location.repository.LocationsRepository;
//import com.boljevac.warehouse.location.service.LocationService;
//import com.boljevac.warehouse.security.jwt.JwtUtil;
//import com.boljevac.warehouse.security.jwt.JwTAuthenticationFilter;
//import com.boljevac.warehouse.security.principal.AuthenticatedUser;
//import com.boljevac.warehouse.user.dto.UserRequest;
//import com.boljevac.warehouse.user.entity.Role;
//import com.boljevac.warehouse.user.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.RequestPostProcessor;
//
//import java.util.List;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//public class LocationControllerTest {
//
//	@Autowired
//	private MockMvc mockMvc;
//
//	@Autowired
//	private ObjectMapper objectMapper;
//
//	@Autowired
//	private LocationsRepository locationsRepository;
//
//	@Autowired
//	private UserRepository userRepository;
//
//	private static final String USER_MAIL = "clerk@mail.com";
//	private static final String USER_USERNAME = "testClerk";
//	private static final String USER_FIRST_NAME = "testClerkFirstName";
//	private static final String USER_SURNAME = "testClerkSurname";
//	private static final String PASSWORD = "password";
//
//	@AfterEach
//	public void tearDown() throws Exception{
//		locationsRepository.deleteAll();
//		userRepository.deleteAll();
//	}
//
//
//	@Test
//	public void createLocations_whenCreatingFirstTime_returns201() throws Exception {
//		Long userId = registerUserAndGetId(userRequest());
//
//		mockMvc.perform(put("/api/warehouse/locations")
//						.with(clerkAuth(userId)))
//				.andExpect(status().isCreated());
//	}
//
//	@Test
//	public void createLocations_whenLocationsExists_returns409() throws Exception {
//		Long userId = registerUserAndGetId(userRequest());
//
//		createLocations(userId);
//
//		mockMvc.perform(put("/api/warehouse/locations")
//						.with(clerkAuth(userId)))
//				.andExpect(status().isConflict())
//				.andExpect(jsonPath("$.message")
//						.value("Locations already exists, cannot create new location"));
//
//
//	}
//
//	@Test
//	public void storeInventory_whenInventoryExists_returns200() throws Exception {
//		Long userId = registerUserAndGetId(userRequest());
//
//	}
////
////	@Test
////	public void storeInventory_whenRequestIsValid_returns200() throws Exception{
////		LocationsRequest request = new LocationsRequest(1L,5);
////		when(locationService.storeInventory(request)).thenReturn(
////				new LocationsResponse(
////						1L,
////						"TestProduct",
////						50.00,
////						500.00,
////						"Location"
////				)
////		);
////
////		mockMvc.perform(post("/api/warehouse/locations")
////				.contentType(MediaType.APPLICATION_JSON)
////				.content("""
////						{"inventoryId": 1 , "quantity": 5}
////						"""
////				)
////		).andExpect(status().isOk());
////
////		verify(locationService).storeInventory(any(LocationsRequest.class));
////	}
////
////	@Test
////	public void storeInventory_whenValidateAvailableQuantityFailed_returns400() throws Exception {
////		doThrow(new NotSufficientStockToStoreException(50)).when(locationService).storeInventory(any(LocationsRequest.class));
////
////		mockMvc.perform(post("/api/warehouse/locations")
////				.contentType(MediaType.APPLICATION_JSON)
////				.content("""
////						{"inventoryId": 1 , "quantity": 50}
////						"""
////				)
////		).andExpect(status().isBadRequest());
////
////		verify(locationService).storeInventory(any(LocationsRequest.class));
////	}
////
////	@Test
////	public void storeInventory_whenValidateAvailableWeightOnLocationFailed_returns400() throws Exception {
////		doThrow(new LocationLoadLimitExceededException()).when(locationService).storeInventory(any(LocationsRequest.class));
////
////		mockMvc.perform(post("/api/warehouse/locations")
////				.contentType(MediaType.APPLICATION_JSON)
////				.content("""
////						{"inventoryId": 1 , "quantity": 50}
////						"""
////				)
////		).andExpect(status().isBadRequest());
////
////		verify(locationService).storeInventory(any(LocationsRequest.class));
////	}
//
//	//Clerk Authenticator
//
//	private RequestPostProcessor clerkAuth(Long clerkId) {
//		AuthenticatedUser principal = new AuthenticatedUser(
//				clerkId,
//				"clerk@example.com",
//				Role.CLERK
//		);
//
//		UsernamePasswordAuthenticationToken auth =
//				new UsernamePasswordAuthenticationToken(
//						principal,
//						null,
//						List.of(new SimpleGrantedAuthority("ROLE_CLERK"))
//				);
//
//		return authentication(auth);
//	}
//
//	private Long registerUserAndGetId(UserRequest userRequest) throws Exception {
//		String userJson = mockMvc.perform(post("/api/user/register")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(userRequest)))
//				.andExpect(status().isOk())
//				.andReturn().getResponse().getContentAsString();
//
//		return ((Number) JsonPath.read(userJson, "$.userId")).longValue();
//	}
//
//	private UserRequest userRequest() {
//		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
//	}
//
//	private void createLocations(Long userId) throws Exception {
//		mockMvc.perform(put("/api/warehouse/locations")
//						.with(clerkAuth(userId)))
//				.andExpect(status().isCreated());
//	}
//}
