package ro.ase.ism.dissertation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.dto.RegisterRequest;
import ro.ase.ism.dissertation.auth.dto.RegisterResponse;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.service.AuthenticationService;
import ro.ase.ism.dissertation.service.EmailService;
import ro.ase.ism.dissertation.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authService;
    private final EmailService emailService;
    private final UserService userService;

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

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getNonAdminUsers() {
        return ResponseEntity.ok(userService.getNonAdminUsers());
    }

    @GetMapping("/users/activation-status")
    public ResponseEntity<Map<String, List<UserDTO>>> getUsersByActivationStatus() {
        return ResponseEntity.ok(userService.getUsersGroupedByActivationStatus());
    }

}
