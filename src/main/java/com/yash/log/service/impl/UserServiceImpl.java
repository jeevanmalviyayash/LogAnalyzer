package com.yash.log.service.impl;

import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.JwtService;
import com.yash.log.service.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jWTService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private IUserRepository userRepository;
    @Override
    public User registerUser(UserDto userDto) throws Exception{
        if(userRepository.findByUserEmail(userDto.getUserEmail()).isEmpty()){
            User user = new User();
            user.setUserName(userDto.getUserName());
            user.setUserEmail(userDto.getUserEmail().toLowerCase());
            user.setUserPhoneNumber(userDto.getUserPhoneNumber());
            user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
            user.setUserRole(userDto.getUserRole());
            return userRepository.save(user);
        }else {
            throw new Exception("User already exists");
        }
    }

    @Override
    public String loginUser(String userEmail, String userPassword) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail.toLowerCase(), userPassword));
        if (auth.isAuthenticated()) {
            return jWTService.generateToken(userEmail);
        }
        throw new BadCredentialsException("Invalid credentials");
    }

    @Override
    public String forgotPassword(String userEmail, String userPassword) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByUserEmail(userEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserPassword(passwordEncoder.encode(userPassword));
            userRepository.save(user);
            return "Password updated successfully";
        }
        throw new UserNotFoundException("User not found with email");
    }

    @Override
    public boolean deleteUser(String userEmail) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByUserEmail(userEmail);
        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            return true;
        }
        return false;
    }
}
