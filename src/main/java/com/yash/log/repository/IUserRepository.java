package com.yash.log.repository;

import com.yash.log.constants.Role;
import com.yash.log.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserEmail(String userEmail);

    List<User> findAllByUserRole(Role role);

}