package com.yash.log.controller;

import com.yash.log.dto.LoginDto;
import com.yash.log.dto.UserDto;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.service.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/Authentication")
public class UserController {
    @Autowired
    private IUserService iUserService;

    //http://localhost:8080/api/Authentication/registerUser  [POST][BODY]
    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        try {
            return ResponseEntity.ok(iUserService.registerUser(userDto));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    //http://localhost:8080/api/Authentication/loginUser  [POST][BODY]
    @PostMapping("/loginUser")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            String jwt = iUserService.loginUser(loginDto.getUserEmail(), loginDto.getUserPassword());
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Username Or Password");
        }
    }


    //http://localhost:8080/api/Authentication/forgotPassword [PUT][BODY]
    @PutMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody LoginDto request)  {
        try {
            String result = iUserService.forgotPassword(request.getUserEmail(), request.getUserPassword());
            return ResponseEntity.ok(result);
        }catch (UserNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
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