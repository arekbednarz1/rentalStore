package com.arekbednarz.service;

import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


public interface IAuthService {
	void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

	TokenAuthDto authenticate(final LoginDto request);

	TokenAuthDto registerUser(UserDto userDto);

}
