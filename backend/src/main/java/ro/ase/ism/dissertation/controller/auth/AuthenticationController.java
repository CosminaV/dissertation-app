package ro.ase.ism.dissertation.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.dto.*;
import ro.ase.ism.dissertation.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/authenticate-otp")
    public ResponseEntity<AuthenticationResponse> authenticateWithOtp(
            @Valid @RequestBody OtpAuthenticationRequest request) {
        return authService.authenticateWithOtp(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody SetPasswordRequest request) {
        return authService.setNewPassword(request);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(authService.getCurrentUserInfo(email));
    }
}
