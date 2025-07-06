package com.arekbednarz.mapper;

import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface UserMapper {

	@Mapping(target = "name", source = "username")
	@Mapping(target = "password", ignore = true)
	UserDto toUserDto(User user);
}
