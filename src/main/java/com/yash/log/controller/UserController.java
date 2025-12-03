package com.yash.log.controller;

import com.yash.log.dto.ApiResponse;
import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.service.impl.IUserService;
import com.yash.log.service.services.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/Authentication")
public class UserController {

    private final PasswordEncoder passwordEncoder;

    private final JWTService jWTService;

    private final AuthenticationManager authenticationManager;

    private final IUserService iUserService;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder, JWTService jWTService, AuthenticationManager authenticationManager, IUserService iUserService) {
        this.passwordEncoder = passwordEncoder;
        this.jWTService = jWTService;
        this.authenticationManager = authenticationManager;
        this.iUserService = iUserService;
    }
    //http://localhost:8080/api/Authentication/registerUser  [POST][BODY]
    @PostMapping("/registerUser")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody UserDto userDto) {
        User user = new User();
        user.setUserEmail(userDto.getUserEmail());
        user.setUserName(userDto.getUserName());
        user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
        user.setUserPhoneNumber(userDto.getUserPhoneNumber());
        User registeredUser = iUserService.registerUser(userDto);
        if (registeredUser != null) {
            return ResponseEntity.ok(new ApiResponse("User registered successfully", registeredUser));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("User already exists", null));
        }

    }

    //http://localhost:8080/api/Authentication/loginUser  [POST][BODY]
    @PostMapping("/loginUser")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody UserDto userDto) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDto.getUserEmail(), userDto.getUserPassword());
        Authentication auth = authenticationManager.authenticate(token);
        if (auth.isAuthenticated()) {
            String jwt = jWTService.generateToken(userDto.getUserEmail());
            return ResponseEntity.ok(new ApiResponse("User Logged In, Your generateToken: ", jwt));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Invalid Credentials", null));
    }


    //http://localhost:8080/api/Authentication/forgotPassword [PUT][BODY]
    @PutMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) throws UserNotFoundException {
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
    public ResponseEntity<String> deleteUser(@PathVariable String userEmail) throws UserNotFoundException {
        boolean isDeleted = iUserService.deleteUser(userEmail);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}