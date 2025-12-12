package com.yash.log.serviceImpl;

import com.yash.log.constants.Role;
import com.yash.log.entity.User;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    private IUserRepository userRepository;
    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(IUserRepository.class);
        securityService = new SecurityService(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {

        User user = new User();
        user.setUserEmail("test@yash.com");
        user.setUserPassword("test@123");
        user.setUserRole(Role.ADMIN);

        when(userRepository.findByUserEmail("test@yash.com"))
                .thenReturn(Optional.of(user));


        UserDetails userDetails = securityService.loadUserByUsername("test@yash.com");


        assertNotNull(userDetails);
        assertEquals("test@yash.com", userDetails.getUsername());
        assertEquals("test@123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + Role.ADMIN.name())));


        verify(userRepository, times(1)).findByUserEmail("test@yash.com");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findByUserEmail("test@yash.com"))
                .thenReturn(Optional.empty());


        assertThrows(UsernameNotFoundException.class,
                () -> securityService.loadUserByUsername("test@yash.com"));

        verify(userRepository, times(1)).findByUserEmail("test@yash.com");
    }
}
