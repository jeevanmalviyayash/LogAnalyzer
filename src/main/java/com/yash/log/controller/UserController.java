package com.yash.log.controller;

import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.service.impl.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/Authentication")
public class UserController {

    private final IUserService iUserService;

    @Autowired
    public UserController(IUserService iUserService) {
        this.iUserService = iUserService;
    }
    //http://localhost:8080/api/Authentication/registerUser  [POST][BODY]
    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        User user = new User();
        user.setUserEmail(userDto.getUserEmail());
        user.setUserName(userDto.getUserName());
        user.setUserPassword(userDto.getUserPassword());
        user.setUserPhoneNumber(userDto.getUserPhoneNumber());
        User registeredUser = iUserService.registerUser(userDto);
        if (registeredUser != null) {
            return ResponseEntity.ok(registeredUser);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
    }

    //http://localhost:8080/api/Authentication/loginUser  [POST][BODY]
    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
        User loggedInUser = iUserService.loginUser(userDto.getUserEmail(), userDto.getUserPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    //http://localhost:8080/api/Authentication/forgotPassword [PUT][BODY]
    @PutMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) throws UserNotFoundException {
        String userEmail = request.get("userEmail");
        String userPassword = request.get("userPassword");

        String result = iUserService.forgotPassword(userEmail, userPassword);
        if (result != null) {
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    //http://localhost:9911/api/Authentication/deleteEmployee [TOKEN] [BODY]
    @DeleteMapping("/deleteUser/{userEmail}")
    public ResponseEntity<?> deleteUser(@PathVariable String userEmail) throws UserNotFoundException {
        boolean isDeleted = iUserService.deleteUser(userEmail);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}