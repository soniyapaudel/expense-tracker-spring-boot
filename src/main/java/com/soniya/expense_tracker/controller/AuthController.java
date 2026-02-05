package com.soniya.expense_tracker.controller;

import com.soniya.expense_tracker.model.User;
import com.soniya.expense_tracker.repository.UserRepository;
import com.soniya.expense_tracker.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.soniya.expense_tracker.security.GoogleTokenVerifier;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil, GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        User dbUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(dbUser.getUsername());
        return Map.of("token", token);

    }

    @PostMapping("/google")
    public Map<String, String> googleSignIn(@RequestBody Map<String, String> request) {
        try {
            String googleToken = request.get("token");

            // verifying the token with Google
            String email = googleTokenVerifier.getEmailFromToken(googleToken);

            if (email == null) {
                throw new RuntimeException("Invalid Google token");
            }

            // check if user laready exists
            Optional<User> existingUser = userRepository.findByUsername(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                user = new User();
                user.setUsername(email);
                user.setPassword(passwordEncoder.encode("google-" + System.currentTimeMillis()));
                userRepository.save(user);
            }

            String jwtToken = jwtUtil.generateToken(user.getUsername());
            return Map.of("token", jwtToken, "message", "Logged in successfully with Google");

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed:" + e.getMessage());
        }
    }

}
