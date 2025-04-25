package ro.ase.ism.dissertation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.auth.dto.RegisterRequest;
import ro.ase.ism.dissertation.auth.dto.RegisterResponse;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.service.AuthenticationService;
import ro.ase.ism.dissertation.service.OtpService;
import ro.ase.ism.dissertation.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

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

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getFilteredStudents(
            @RequestParam(required = false, defaultValue = "false") Boolean unassignedOnly,
            @RequestParam(required = false) EducationLevel educationLevel,
            @RequestParam(required = false) Integer cohortId) {
        return ResponseEntity.ok(userService.getFilteredStudents(educationLevel, cohortId, unassignedOnly));
    }
}
