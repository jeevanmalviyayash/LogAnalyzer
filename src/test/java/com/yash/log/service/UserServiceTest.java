package com.yash.log.service;
import com.yash.log.constants.Role;
import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.impl.IUserService;
import com.yash.log.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class UserServiceTest {
    @Mock
    private IUserRepository iUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser() {
        UserDto userDto = new UserDto();
        userDto.setUserName("John");
        userDto.setUserEmail("john@yash.com");
        userDto.setUserPassword("password123");
        userDto.setUserPhoneNumber("1234567890");
        userDto.setUserRole(Role.valueOf("ADMIN"));

        when(iUserRepository.findByUserEmail("john@yash.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUserEmail("john@yash.com");
        savedUser.setUserPassword("encodedPassword");
        savedUser.setUserRole(Role.ADMIN);

        when(iUserRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(userDto);

        assertNotNull(result);
        assertEquals("john@yash.com", result.getUserEmail());
        assertEquals(Role.ADMIN, result.getUserRole());
    }

    //Test Cases for login


}
