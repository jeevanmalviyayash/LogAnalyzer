package com.yash.log.serviceImpl;
import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;

import com.yash.log.repository.IUserRepository;
import com.yash.log.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.yash.log.constants.Role.DEVELOPER;
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
    void testRegisterUser_Success() {
        UserDto dto = new UserDto("test@yash.com", "TestUser", "12345", "9023654123", DEVELOPER);
        when(iUserRepository.findByUserEmail(dto.getUserEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getUserPassword())).thenReturn("encodedPwd");

        User savedUser = new User();
        savedUser.setUserEmail(dto.getUserEmail());
        when(iUserRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("test@yash.com", result.getUserEmail());
        verify(iUserRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        UserDto dto = new UserDto("test@yash.com", "TestUser", "12345", "9023654123", DEVELOPER);
        when(iUserRepository.findByUserEmail(dto.getUserEmail())).thenReturn(Optional.of(new User()));

        User result = userService.registerUser(dto);

        assertNull(result);
        verify(iUserRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        User user = new User();
        user.setUserEmail("test@yash.com");
        String hashedPassword = new BCryptPasswordEncoder().encode("12345");
        user.setUserPassword(hashedPassword);

        when(iUserRepository.findByUserEmail("test@yash.com")).thenReturn(Optional.of(user));

        User result = userService.loginUser("test@yash.com", "12345");

        assertNotNull(result);
        assertEquals("test@yash.com", result.getUserEmail());
    }


    @Test
    void testForgotPassword_Success() throws UserNotFoundException {
        User user = new User();
        user.setUserEmail("test@yash.com");

        when(iUserRepository.findByUserEmail("test@yash.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPwd")).thenReturn("encodedNewPwd");

        String result = userService.forgotPassword("test@yash.com", "newPwd");

        assertEquals("Password updated successfully", result);
        verify(iUserRepository, times(1)).save(user);
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(iUserRepository.findByUserEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.forgotPassword("notfound@test.com", "newPwd"));
    }

    @Test
    void testDeleteUser_Success() throws UserNotFoundException {
        User user = new User();
        user.setUserEmail("test@yash.com");

        when(iUserRepository.findByUserEmail("test@yash.com")).thenReturn(Optional.of(user));

        boolean result = userService.deleteUser("test@yash.com");

        assertTrue(result);
        verify(iUserRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(iUserRepository.findByUserEmail("test@yash.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser("test@yash.com"));
    }
}

