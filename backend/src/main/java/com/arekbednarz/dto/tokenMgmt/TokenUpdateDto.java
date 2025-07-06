package com.arekbednarz.dto.tokenMgmt;

import com.arekbednarz.enums.TokenUpdateAction;
import com.arekbednarz.enums.UserUpdateAction;

import java.util.Set;


public record TokenUpdateDto(Long userId, String tokenValue, Set<TokenUpdateAction> action) {
}
