package ro.ase.ism.dissertation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.dto.RegisterRequest;
import ro.ase.ism.dissertation.auth.dto.RegisterResponse;
import ro.ase.ism.dissertation.service.AuthenticationService;
import ro.ase.ism.dissertation.service.EmailService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/send-activation")
    public ResponseEntity<String> sendActivationEmail() {
        emailService.sendActivationEmailsToAllUnverifiedUsers();
        return ResponseEntity.ok("Activation emails sent");
    }
}
