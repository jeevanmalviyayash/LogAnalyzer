package com.yash.log.controller;
import com.yash.log.constants.Role;
import com.yash.log.dto.ApiResponse;
import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.service.impl.IUserService;
import com.yash.log.service.services.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest{

    private PasswordEncoder passwordEncoder;
    private JWTService jwtService;
    private AuthenticationManager authenticationManager;
    private IUserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JWTService.class);
        authenticationManager = mock(AuthenticationManager.class);
        userService = mock(IUserService.class);

        userController = new UserController(passwordEncoder, jwtService, authenticationManager, userService);
    }

    @Test
    void registerUser_shouldReturnSuccess_whenUserIsRegistered() {
        UserDto dto = new UserDto();
        dto.setUserEmail("test@yash.com");
        dto.setUserName("Test User");
        dto.setUserPassword("password");
        dto.setUserPhoneNumber("1234567890");

        User registeredUser = new User();
        registeredUser.setUserEmail(dto.getUserEmail());

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userService.registerUser(dto)).thenReturn(registeredUser);

        ResponseEntity<?> response = userController.registerUser(dto);

        assertEquals(200, response.getStatusCodeValue());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals("User registered successfully", apiResponse.getMessage());
        assertEquals(registeredUser, apiResponse.getData());
    }

    @Test
    void registerUser_shouldReturnConflict_whenUserAlreadyExists() {
        UserDto dto = new UserDto();
        dto.setUserEmail("test@yash.com");

        when(userService.registerUser(dto)).thenReturn(null);

        ResponseEntity<?> response = userController.registerUser(dto);

        assertEquals(409, response.getStatusCodeValue());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertEquals("User already exists", apiResponse.getMessage());
    }

    @Test
    void loginUser_shouldReturnSuccess_whenAuthenticationIsValid() {
        UserDto dto = new UserDto();
        dto.setUserEmail("test@yash.com");
        dto.setUserPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);

        User user = new User();
        user.setUserId(1);
        user.setUserEmail("test@yash.com");
        user.setUserRole(Role.ADMIN);

        when(jwtService.generateToken("test@yash.com")).thenReturn("jwt-token");
        when(userService.loginUser("test@yash.com", "password")).thenReturn(user);

        ResponseEntity<ApiResponse> response = userController.loginUser(dto);

        assertEquals(200, response.getStatusCodeValue());
        ApiResponse apiResponse = response.getBody();
        assertEquals("Success", apiResponse.getMessage());
        assertTrue(((HashMap<?, ?>) apiResponse.getUserMap()).containsKey("token"));
    }

    @Test
    void loginUser_shouldReturnUnauthorized_whenAuthenticationFails() {
        UserDto dto = new UserDto();
        dto.setUserEmail("test@yash.com");
        dto.setUserPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);

        ResponseEntity<ApiResponse> response = userController.loginUser(dto);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid Credentials", response.getBody().getMessage());
    }

    @Test
    void forgotPassword_shouldReturnSuccess_whenUserExists() throws UserNotFoundException {
        Map<String, String> request = new HashMap<>();
        request.put("userEmail", "test@yash.com");
        request.put("userPassword", "newPass");

        when(userService.forgotPassword("test@yash.com", "newPass")).thenReturn("Password updated successfully");

        ResponseEntity<String> response = userController.forgotPassword(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password updated successfully", response.getBody());
    }

    @Test
    void forgotPassword_shouldReturnNotFound_whenUserDoesNotExist() throws UserNotFoundException {
        Map<String, String> request = new HashMap<>();
        request.put("userEmail", "test@yash.com");
        request.put("userPassword", "newPass");

        when(userService.forgotPassword("test@yash.com", "newPass")).thenReturn(null);

        ResponseEntity<String> response = userController.forgotPassword(request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void deleteUser_shouldReturnSuccess_whenUserIsDeleted() throws UserNotFoundException {
        when(userService.deleteUser("test@yash.com")).thenReturn(true);

        ResponseEntity<String> response = userController.deleteUser("test@yash.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws UserNotFoundException {
        when(userService.deleteUser("test@yash.com")).thenReturn(false);

        ResponseEntity<String> response = userController.deleteUser("test@yash.com");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }
}
