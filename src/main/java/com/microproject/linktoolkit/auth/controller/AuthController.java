package com.microproject.linktoolkit.auth.controller;

import com.microproject.linktoolkit.auth.controller.dto.LoginRequest;
import com.microproject.linktoolkit.auth.controller.dto.RegisterRequest;
import com.microproject.linktoolkit.auth.entity.User;
import com.microproject.linktoolkit.auth.service.AuthService;
import com.microproject.linktoolkit.auth.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "https://linktoolkit-ui.vercel.app/"})
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.getEmail(), request.getPassword(), request.getName());
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok().body(token);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .map(user -> ResponseEntity.ok().body(jwtUtil.generateToken(user.getEmail())))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }
}