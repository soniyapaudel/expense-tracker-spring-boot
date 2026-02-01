package com.soniya.expense_tracker.controller;

import com.soniya.expense_tracker.model.User;
import com.soniya.expense_tracker.repository.UserRepository;
import com.soniya.expense_tracker.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return "User registered successfully";
    }

    @PostMapping("/login")
    public Map<String, String> login(@requestBody User user){
        User dbUser = userRepository.findbyUsername()
    }

}
