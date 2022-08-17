package com.apps.keka.api.users.ui.controllers;

import com.apps.keka.api.users.service.UsersService;
import com.apps.keka.api.users.shared.UserDto;
import com.apps.keka.api.users.ui.model.*;
import com.apps.keka.api.users.ui.model.request.CreateUserRequestModel;
import com.apps.keka.api.users.ui.model.request.UpdateUserRequestModel;
import com.apps.keka.api.users.ui.model.response.CreateUserResponseModel;
import com.apps.keka.api.users.ui.model.response.UserResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    UsersService usersService;
    @Autowired
    private Environment env;

    @GetMapping("/status/check")
    public String status() {
        return "Working on port " + env.getProperty("server.port") + ", with token = " + env.getProperty("token.secret");
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<CreateUserResponseModel> createAdmin(@RequestBody CreateUserRequestModel userDetails) {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        userDto.setRole(UserRole.ADMIN.toString());

        UserDto createdUser = usersService.createUser(userDto);

        CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    @GetMapping(value = "/{userId}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserResponseModel> getUser(@RequestHeader(value = "Authorization") String authHeader,
                                                     @PathVariable("userId") String userId) {

        if (!usersService.isAdmin(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDto userDto = usersService.getUserByUserId(userId);
        if (userDto.getRole().equals(UserRole.ADMIN.toString())) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        UserResponseModel returnValue = new ModelMapper().map(userDto, UserResponseModel.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

    @DeleteMapping(value = "/{userId}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> deleteUser(@RequestHeader(value = "Authorization") String authHeader,
                                                 @PathVariable("userId") String userId) {
        if (!usersService.isAdmin(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return usersService.deleteUserByUserId(userId);

    }

    @PatchMapping(value = "/{userId}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> updateUser(@RequestHeader(value = "Authorization") String authHeader,
                                                 @PathVariable("userId") String userId,
                                                 @RequestBody UpdateUserRequestModel userDetails) {
        if (!usersService.isAdmin(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        return usersService.updateUserByUserId(userId, userDto);
    }

    @GetMapping(value = "/check",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Boolean> isAdmin(@RequestHeader(value = "Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(usersService.isAdmin(authHeader));
    }


}
