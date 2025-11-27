package com.yash.log.service.services;

import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.impl.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private final IUserRepository iUserRepository;


    @Autowired
    public UserService(IUserRepository iUserRepository) {
        this.iUserRepository = iUserRepository;
    }

    @Override
    public User registerUser(UserDto userDto) {
        if (iUserRepository.findByUserEmail(userDto.getUserEmail()).isEmpty()) {
            User user = new User();
            user.setUserName(userDto.getUserName());
            user.setUserEmail(userDto.getUserEmail());

            user.setUserPhoneNumber(userDto.getUserPhoneNumber());
            user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
            user.setUserRole(userDto.getUserRole());
            return iUserRepository.save(user);
        } else {
            return null;
        }
    }

    @Override
    public User loginUser(String userEmail, String userPassword) {
        Optional<User> userOptional = iUserRepository.findByUserEmail(userEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (checkPassword(userPassword, user.getUserPassword())) {
             iUserRepository.save(user);
                return user;
            }
        }
        return null;
    }

    @Override
    public String forgotPassword(String userEmail, String userPassword) throws UserNotFoundException{
        Optional<User> userOptional = iUserRepository.findByUserEmail(userEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserPassword(passwordEncoder.encode(userPassword));
            iUserRepository.save(user);
            return "Password updated successfully";
        }
        throw new UserNotFoundException("User not found with email: " + userEmail);
    }

    @Override
    public boolean deleteUser(String userEmail) throws UserNotFoundException {
    Optional<User> userOptional = iUserRepository.findByUserEmail(userEmail);
    if (userOptional.isPresent()) {
        iUserRepository.delete(userOptional.get());
        return true;
    }
        throw new UserNotFoundException("User not found with email: " + userEmail);
    }

    private boolean checkPassword(String inputPassword, String hashedPassword) {
        return BCrypt.checkpw(inputPassword, hashedPassword);
    }
}
