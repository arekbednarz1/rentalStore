package com.arekbednarz.controller;

import com.arekbednarz.dto.userMgmt.UserUpdateDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.enums.UserUpdateAction;
import com.arekbednarz.mapper.UserMapper;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.service.IManageService;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/users")
public class UserManageController {
	private final IManageService userManageService;
	private final UserMapper userMapper;

	@Autowired
	public UserManageController(
		@Qualifier("userCreationService") IManageService userManageService,
		UserMapper userMapper) {
		this.userManageService = userManageService;
		this.userMapper = userMapper;
	}

	@PutMapping(path = "/self", produces = "application/json")
	@PreAuthorize("hasAuthority('self:manage')")
	public ResponseEntity updateSelfUser(
		@RequestParam(value = "password", required = false) final String password,
		@RequestParam(value = "name", required = false) final String name) {
		if (StringUtils.isEmpty(password) && StringUtils.isEmpty(name)) {
			throw new BadRequestException("Missing required parameters");
		}
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Set<UserUpdateAction> actions = new HashSet<>();
		if (!StringUtils.isEmpty(password)) {
			actions.add(UserUpdateAction.PASSWORD);
		}
		if (!StringUtils.isEmpty(name)) {
			actions.add(UserUpdateAction.NAME);
		}
		return ResponseEntity.ok().body(
			userMapper.toUserDto(userManageService
				.update(new UserUpdateDto(currentUser.getEmail(), actions, name, password, null))));
	}

	@GetMapping(path = "/self", produces = "application/json")
	@PreAuthorize("hasAuthority('self:manage')")
	public ResponseEntity getCurrentUser() {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return ResponseEntity.ok().body(userMapper.toUserDto(currentUser));
	}

	@PutMapping(produces = "application/json")
	@PreAuthorize("hasAuthority('user:create')")
	public ResponseEntity updateUser(
		@RequestParam(value = "email", required = true) final String userEmail,
		@RequestParam(value = "password", required = false) final String password,
		@RequestParam(value = "name", required = false) final String name,
		@RequestParam(value = "role", required = false) final Role role) {
		if (StringUtils.isEmpty(password) && StringUtils.isEmpty(name) && role == null) {
			throw new BadRequestException("Missing required parameters");
		}

		Set<UserUpdateAction> actions = new HashSet<>();
		if (!StringUtils.isEmpty(password)) {
			actions.add(UserUpdateAction.PASSWORD);
		}
		if (!StringUtils.isEmpty(name)) {
			actions.add(UserUpdateAction.NAME);
		}
		if (role != null) {
			actions.add(UserUpdateAction.ROLE);
		}
		return ResponseEntity.ok().body(
			userMapper.toUserDto(userManageService
				.update(new UserUpdateDto(userEmail, actions, name, password, role))));
	}

	@DeleteMapping(produces = "application/json")
	@PreAuthorize("hasAuthority('user:create')")
	public ResponseEntity deleteUser(
		@RequestParam(value = "email", required = true) final String userEmail) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (currentUser.getEmail().equals(userEmail)) {
			throw new BadRequestException("You cannot delete current logged user");
		}
		userManageService.delete(userEmail);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(produces = "application/json")
	@PreAuthorize("hasAuthority('user:create')")
	public ResponseEntity getSpecificUser(
		@RequestParam(value = "email", required = true) final String userEmail) {
		return ResponseEntity.ok().body(
			userMapper.toUserDto(userManageService
				.getOne(userEmail)));
	}

	@GetMapping(path = "/list", produces = "application/json")
	@PreAuthorize("hasAuthority('user:create')")
	public ResponseEntity getAllUsers() {
		return ResponseEntity.ok().body(
			userManageService.getAll()
				.stream()
				.map(user -> userMapper.toUserDto((User) user))
				.toList());
	}
}
