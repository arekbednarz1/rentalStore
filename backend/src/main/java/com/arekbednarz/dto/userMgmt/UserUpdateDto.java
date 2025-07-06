package com.arekbednarz.dto.userMgmt;

import com.arekbednarz.enums.Role;
import com.arekbednarz.enums.UserUpdateAction;

import java.util.Set;


public record UserUpdateDto(String userEmail, Set<UserUpdateAction> action, String username, String password, Role role) {
}
