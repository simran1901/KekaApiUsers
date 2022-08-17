package com.apps.keka.api.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.apps.keka.api.users.shared.UserDto;

public interface UsersService extends UserDetailsService {
    UserDto createUser(UserDto userDetails);

    UserDto getUserDetailsByEmail(String email);

    UserDto getUserByUserId(String userId);

    ResponseEntity<HttpStatus> deleteUserByUserId(String userId);

    ResponseEntity<HttpStatus> updateUserByUserId(String userId, UserDto userDetails);

    Boolean isAdmin(String jwt);

    Boolean isUser(String jwt);

    public String getUserIdFromToken(String authHeader);

}
