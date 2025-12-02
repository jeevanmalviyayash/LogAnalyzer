package com.yash.log.service.services;

import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;
public interface IUserService {
    User registerUser(UserDto userDto)throws Exception;
    String loginUser(String userEmail, String userPassword);
    String forgotPassword(String userEmail,String userPassword)throws UserNotFoundException;
    boolean deleteUser(String userEmail) throws UserNotFoundException;
}
