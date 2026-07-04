package com.boljevac.warehouse.user.service;


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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;

	//Customer Methods

	public UserResponse registerUserByCustomer(UserRequest userRequest) {
		//For differentiation between customer and management request
		boolean adminCreation = false;

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity user = new UserEntity();
		updateUserEntity(user, userRequest, hashedPassword);

		//Customer cannot choose role
		user.setRole(Role.USER);

		saveUser(user);

		return responseMapper(user, adminCreation);

	}

	public void updateUserByCustomer(AuthenticatedUser authenticatedUser, UserRequest userRequest) {
		UserEntity toUpdate = getUserByAuthenticatedUser(authenticatedUser);

		String passwordInput = userRequest.getPassword();

		boolean passwordMatches = passwordEncoder.matches(passwordInput, toUpdate.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", toUpdate.getId());
			throw new UserIncorrectCredentialsException();
		}
		updateUserEntity(toUpdate, userRequest);

		userRepository.save(toUpdate);
		log.info("User with email {} updated", userRequest.getEmail());
	}

	//Management Methods

	public Page<UserResponse> getPageOfUsers(Pageable pageable) {
		//For differentiation between customer and management request
		boolean adminRequest = true;

		return userRepository.findAll(pageable).map(user -> {
			log.info("Getting Page of Users");
			return responseMapper(user, adminRequest);
		});

	}

	//Only for admin
	public UserResponse registerManagement(UserRequest userRequest, Role role) {
		//For differentiation between customer and management request
		boolean adminCreation = true;

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity newManagementUser = new UserEntity();
		updateUserEntity(newManagementUser, userRequest, hashedPassword);

		//Admin can freely choose role
		newManagementUser.setRole(role);

		saveUser(newManagementUser);

		return responseMapper(newManagementUser, adminCreation);
	}

	public void updateUserByManagement(Long userId, UserRequest userRequest, Role role) {
		UserEntity toUpdate = getUserById(userId);

		updateUserEntity(toUpdate, userRequest);
		toUpdate.setRole(role);

		userRepository.save(toUpdate);
		log.info("User with email {} updated", toUpdate.getEmail());
	}

	//Helper Methods

	private void updateUserEntity(UserEntity user, UserRequest userRequest, String hashedPassword) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
		user.setPassword(hashedPassword);
	}

	private void updateUserEntity(UserEntity user, UserRequest userRequest) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
	}

	private boolean emailExists(UserRequest userRequest) {
		return userRepository.findByEmail(userRequest.getEmail()).isPresent();
	}

	public UserEntity getUserEntityByMail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(
						() -> {
							log.info("User with email {} was not found", email);
							return new UserNotFoundException(email);
						}
				);
	}

	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(
						() -> {
							log.info("User with id {} was not found", userId);
							return new UserNotFoundException(userId);
						}
				);
	}

	private void saveUser(UserEntity user) {
		userRepository.save(user);
		log.info("User with role {} and id {} has been registered successfully", user.getRole(), user.getId());
	}


	private UserResponse responseMapper(UserEntity userEntity, boolean adminCreation) {
		if (adminCreation) {
			return modelMapper.map(userEntity, UserResponse.class);
		} else {
			return UserResponse.builder()
					.userId(userEntity.getId())
					.email(userEntity.getEmail())
					.name(userEntity.getName())
					.surname(userEntity.getSurname())
					.username(userEntity.getUsername())
					.build();
		}
	}

	public UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		return userRepository.findById(authenticatedUser.getUserId())
				.orElseThrow(() -> {
					log.warn("Could not find user with id {}", authenticatedUser.getUserId());
					return new UserNotFoundException(authenticatedUser.getUserId());
				});
	}

}
