package com.apps.keka.api.users.ui.controllers;

import com.apps.keka.api.users.service.AttendanceService;
import com.apps.keka.api.users.service.UsersService;
import com.apps.keka.api.users.shared.UserDto;
import com.apps.keka.api.users.ui.model.*;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    UsersService usersService;
    @Autowired
    AttendanceService attendanceService;
    @Autowired
    private Environment env;

    @GetMapping("/status/check")
    public String status() {
        return "Working on port " + env.getProperty("server.port") + ", with token = " +
                env.getProperty("token.secret");
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<CreateUserResponseModel> createUser(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody CreateUserRequestModel userDetails) {
        // does authHeader belong to admin?
        if (!usersService.isAdmin(authHeader)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        userDto.setRole(UserRole.USER.toString());

        UserDto createdUser = usersService.createUser(userDto);

        CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    @GetMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserResponseModel> getUser(@RequestHeader(value = "Authorization") String authHeader) {


        String userId = usersService.getUserIdFromToken(authHeader);
        if (userId == null || userId.isEmpty()) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        UserDto userDto = usersService.getUserByUserId(userId);
        UserResponseModel returnValue = new ModelMapper().map(userDto, UserResponseModel.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

    // check in
    @PostMapping(value = "/attendance",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> checkIn(@RequestHeader(value = "Authorization") String authHeader) {
        if (!usersService.isUser(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String userId = usersService.getUserIdFromToken(authHeader);

        attendanceService.checkIn(userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // check out
    @PatchMapping(value = "/attendance",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity checkOut(@RequestHeader(value = "Authorization") String authHeader) {
        if (!usersService.isUser(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String userId = usersService.getUserIdFromToken(authHeader);

        attendanceService.checkOut(userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // get attendance
    @GetMapping(value = "/attendance",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AttendanceListResponseModel> getAttendance(@RequestHeader(value = "Authorization") String authHeader,
                                                                     @RequestBody GetAttendanceRequestModel details) {
        if (!usersService.isUser(authHeader)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        String userId = usersService.getUserIdFromToken(authHeader);
        AttendanceListResponseModel returnValue = modelMapper.map(details, AttendanceListResponseModel.class);
        returnValue.setAttendanceList(attendanceService.getAttendance(userId, details));

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

    @GetMapping(value = "/check",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Boolean> isUser(@RequestHeader(value = "Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(usersService.isUser(authHeader));
    }
}
