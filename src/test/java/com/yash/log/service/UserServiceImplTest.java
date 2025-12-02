package com.yash.log.service;


import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.JwtService;
import com.yash.log.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userDto = new UserDto();
        userDto.setUserName("John");
        userDto.setUserEmail("john@yash.com");
        userDto.setUserPassword("Password@123");
        userDto.setUserPhoneNumber("1234567890");

        user = new User();
        user.setUserName("John");
        user.setUserEmail("john@yash.com");
        user.setUserPassword("encodedPassword");
        user.setUserPhoneNumber("1234567890");
    }

    // ---------- registerUser ----------
    @Test
    void testRegisterUser_Success() throws Exception {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(userDto);

        assertNotNull(savedUser);
        assertEquals("john@yash.com", savedUser.getUserEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.of(user));

        Exception ex = assertThrows(Exception.class, () -> userService.registerUser(userDto));
        assertEquals("User already exists", ex.getMessage());
    }

    // ---------- loginUser ----------
    @Test
    void testLoginUser_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("john@yash.com")).thenReturn("jwtToken");

        String token = userService.loginUser("john@yash.com", "Password@123");

        assertEquals("jwtToken", token);
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> userService.loginUser("john@yash.com", "wrongPassword"));
    }

    // ---------- forgotPassword ----------
    @Test
    void testForgotPassword_Success() throws UserNotFoundException {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        String result = userService.forgotPassword("john@yash.com", "newPassword");

        assertEquals("Password updated successfully", result);
        verify(userRepository).save(user);
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.forgotPassword("john@yash.com", "newPassword"));
    }

    // ---------- deleteUser ----------
    @Test
    void testDeleteUser_Success() throws UserNotFoundException {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.of(user));

        boolean result = userService.deleteUser("john@yash.com");

        assertTrue(result);
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound() throws UserNotFoundException {
        when(userRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.empty());

        boolean result = userService.deleteUser("john@yash.com");

        assertFalse(result);
        verify(userRepository, never()).delete(any(User.class));
    }
}
