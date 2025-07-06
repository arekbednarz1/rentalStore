package com.arekbednarz.service.impl.userMgmt;

import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.dto.userMgmt.UserUpdateDto;
import com.arekbednarz.enums.UserUpdateAction;
import com.arekbednarz.exception.EntityCreationException;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.repository.UserRepository;
import com.arekbednarz.service.IManageService;
import io.vavr.control.Option;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service("userCreationService")
public class UserManageService implements IManageService {
	private static final Logger LOG = Logger.getLogger(UserManageService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserManageService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAll() {
		return (List<T>) userRepository.findAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T create(S dto) {
		var userCreationDto = dtoParser(dto, UserDto.class);
		try {
			return (T) userRepository.save(createUserObject(userCreationDto));
		} catch (DataIntegrityViolationException e) {
			LOG.error("Error creating user: User already exists");
			throw new EntityCreationException("User creation failed, user already exists ");
		} catch (Exception e) {
			LOG.errorf("Error creating user: %s", e.getMessage());
			throw new EntityCreationException("User creation failed, caused by: " + e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T getOne(S email) {
		var userEmail = dtoParser(email, String.class);
		return (T) getUserByEmail(userEmail);
	}

	@Override
	public <S> void delete(S email) {
		var userEmail = dtoParser(email, String.class);
		userRepository.delete(getUserByEmail(userEmail));
		LOG.infof("Deleted user: %s", userEmail);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public <T, S> T update(S dto) {
		var userUpdateDto = createUpdateObject(dtoParser(dto, UserUpdateDto.class));
		LOG.infof("Updating user: %s", userUpdateDto.getEmail());
		return (T) userRepository.save(userUpdateDto);
	}

	private User createUpdateObject(final UserUpdateDto userUpdateDto) {
		final String email = Option.of(userUpdateDto.userEmail())
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityCreationException("User email cannot be null"));

		final Set<UserUpdateAction> userUpdateActions = Option.of(userUpdateDto.action())
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityCreationException("User update action set cannot be null or empty"));

		User dbUser = getUserByEmail(email);

		return User.builder()
			.id(dbUser.getId())
			.email(dbUser.getEmail())
			.role(userUpdateActions.contains(UserUpdateAction.ROLE) ? (com.arekbednarz.enums.Role) defineSource(userUpdateDto.role()) : dbUser.getRole())
			.username(userUpdateActions.contains(UserUpdateAction.NAME) ? (String) defineSource(userUpdateDto.username()) : dbUser.getUsername())
			.password(encodePassword(userUpdateActions.contains(UserUpdateAction.PASSWORD) ? userUpdateDto.password() : dbUser.getPassword()))
			.build();
	}

	private String encodePassword(final String password) {
		return passwordEncoder.encode(password);
	}

	private User getUserByEmail(final String email) {
		return Option.of(userRepository.findUserByEmail(email))
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityNotFoundException(User.class, "email", email));
	}

	private User createUserObject(final UserDto userDto) {
		return User.builder()
			.username(userDto.getName())
			.email(userDto.getEmail())
			.role(userDto.getRole())
			.password(encodePassword(userDto.getPassword()))
			.build();
	}
}
