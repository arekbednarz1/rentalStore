package com.arekbednarz.controller;

import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.tokenMgmt.RegisterUserDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final IAuthService authService;

	@Autowired
	public AuthController(IAuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<TokenAuthDto> register(@RequestBody @Valid RegisterUserDto registerUserDto) {
		var userDto = UserDto.builder()
			.name(registerUserDto.getUsername())
			.password(registerUserDto.getPassword())
			.email(registerUserDto.getEmail())
			.role(Role.USER)
			.build();

		return ResponseEntity.ok(authService.registerUser(userDto));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<TokenAuthDto> authenticate(@RequestBody @Valid LoginDto request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

	@PostMapping("/refresh-token")
	public void refreshToken(
		HttpServletRequest request,
		HttpServletResponse response) throws IOException {
		authService.refreshToken(request, response);
	}
}
