package ro.ase.ism.dissertation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.AuthenticationRequest;
import ro.ase.ism.dissertation.auth.AuthenticationResponse;
import ro.ase.ism.dissertation.auth.RegisterRequest;
import ro.ase.ism.dissertation.auth.RegisterResponse;
import ro.ase.ism.dissertation.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid  @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        return authService.refresh(refreshToken);
    }

}
