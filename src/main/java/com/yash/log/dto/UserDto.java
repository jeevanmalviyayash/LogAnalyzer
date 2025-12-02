package com.yash.log.dto;

import com.yash.log.constants.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String userEmail;
    @NotBlank(message = "Name is required")
    private String userName;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[A-Z]).+$",
            message = "Password must contain at least one digit, one special character, and one uppercase letter"
    )
    private String userPassword;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String userPhoneNumber;
    private Role userRole;

}