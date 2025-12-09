package com.yash.log.service.impl;

import com.yash.log.constants.Role;
import com.yash.log.dto.UserDto;
import com.yash.log.entity.User;
import com.yash.log.exceptions.UserNotFoundException;

import java.util.List;

public interface IUserService {

    User registerUser(UserDto userDto);

    User loginUser(String userEmail, String userPassword);

    String forgotPassword(String userEmail,String userPassword)throws UserNotFoundException;

    boolean deleteUser(String userEmail) throws UserNotFoundException;

    List<User> getUserBYRole(Role role);
}