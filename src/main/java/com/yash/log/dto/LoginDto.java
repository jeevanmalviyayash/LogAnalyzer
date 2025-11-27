package com.yash.log.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
    private String userEmail;
//    @NotBlank(message = "Password is required")
    // @Size(min = 8, message = "Password must be at least 8 characters long")
//    @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*]).+$",
//            message = "Invalid Username Or Password"
//    )
    private String userPassword;
}
