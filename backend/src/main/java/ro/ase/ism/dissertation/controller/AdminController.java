package ro.ase.ism.dissertation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.dto.RegisterRequest;
import ro.ase.ism.dissertation.auth.dto.RegisterResponse;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.service.AuthenticationService;
import ro.ase.ism.dissertation.service.OtpService;
import ro.ase.ism.dissertation.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authService;
    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/send-otps")
    public ResponseEntity<String> sendOtps() {
        otpService.generateAndSendOtpsToUsersWithoutPassword();
        return ResponseEntity.ok("OTP emails sent");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getNonAdminUsers() {
        return ResponseEntity.ok(userService.getNonAdminUsers());
    }
}
