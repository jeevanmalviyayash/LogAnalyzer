package com.yash.log.entity;

import com.yash.log.constants.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "userDB")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sequential auto-increment
    @Column(name = "user_id")
    private int userID;

    @Column(unique = true, nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String userPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;
}