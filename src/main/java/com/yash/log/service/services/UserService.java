package com.yash.log.service.services;

import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
import com.yash.log.repository.IUserRepository;
import com.yash.log.service.impl.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

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
            user.setUserPassword(hashPassword(userDto.getUserPassword()));
            user.setUserRole(userDto.getUserRole());
            return iUserRepository.save(user);
        } else {
            return null; // User already exists
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
            user.setUserPassword(hashPassword(userPassword));
            iUserRepository.save(user);
            UserDto userDto = new UserDto();
            userDto.setUserEmail(userEmail);
            userDto.setUserPassword(userPassword);
            return "success";
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
    return false;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean checkPassword(String inputPassword, String hashedPassword) {
        return BCrypt.checkpw(inputPassword, hashedPassword);
    }
}
