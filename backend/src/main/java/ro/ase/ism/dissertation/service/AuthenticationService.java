package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.auth.dto.*;
import ro.ase.ism.dissertation.auth.validator.PasswordValidator;
import ro.ase.ism.dissertation.exception.InvalidAssignmentException;
import ro.ase.ism.dissertation.exception.PasswordNotValidException;
import ro.ase.ism.dissertation.exception.UserAlreadyExistsException;
import ro.ase.ism.dissertation.model.otp.OneTimePassword;
import ro.ase.ism.dissertation.model.token.RefreshToken;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.OneTimePasswordRepository;
import ro.ase.ism.dissertation.repository.RefreshTokenRepository;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OneTimePasswordRepository oneTimePasswordRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Initializing registration...");

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("User with email %s already exists.".formatted(request.getEmail()));
            throw new UserAlreadyExistsException("User with email %s already exists.".formatted(request.getEmail()));
        }

        Role roleToAssign = switch (request.getRole()) {
            case STUDENT -> Role.STUDENT;
            case TEACHER -> Role.TEACHER;
            case ADMIN -> throw new InvalidAssignmentException("You can't self-register as admin.");
            default -> Role.USER; // fallback
        };

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(roleToAssign)
                .build();

        if (roleToAssign == Role.STUDENT) {
            Student student = Student.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .role(Role.STUDENT)
                    .educationLevel(request.getEducationLevel())
                    .build();
            userRepository.save(student);
        } else {
            userRepository.save(user);
        }

        log.info("User registered successfully: %s".formatted(request.getEmail()));
        return new RegisterResponse("User registered successfully: %s".formatted(request.getEmail()));
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        log.info("Initializing authentication...");

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.error("Invalid username or password");
            throw new BadCredentialsException("Invalid credentials");
        }

        // check if user has an invalidated refresh token
        var latestRefreshToken = refreshTokenRepository.findLatestTokenByUserId(user.getId());
        if (latestRefreshToken.isPresent() && !latestRefreshToken.get().isLoggedOut()) {
            jwtService.incrementAccessTokenVersion(user);
            userRepository.save(user);
        }

        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        // revoke previous refresh tokens for this user
        jwtService.revokeAllRefreshTokens(user);

        // save the refresh token in the db
        saveRefreshToken(refreshToken, user);

        var accessToken = jwtService.generateAccessToken(new HashMap<>(), user);

        // create cookie for the refresh token
        ResponseCookie refreshCookie = createCookie("refreshToken", refreshToken);

        boolean faceImageRequired = user.getRole() != Role.ADMIN && user.getFaceImagePath() == null;

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .faceImageRequired(faceImageRequired)
                        .build());
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> authenticateWithOtp(OtpAuthenticationRequest request) {
        log.info("Authenticating with OTP for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<OneTimePassword> otpOptional = oneTimePasswordRepository.findByUserAndUsedFalse(user);
        if (otpOptional.isEmpty()) {
            throw new BadCredentialsException("Invalid or already used OTP");
        }

        var otp = otpOptional.get();

        if (!passwordEncoder.matches(request.getOtp(), otp.getOtp())) {
            throw new BadCredentialsException("Incorrect OTP");
        }

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("OTP expired");
        }

        // mark OTP as used
        otp.setUsed(true);
        oneTimePasswordRepository.save(otp);

        // continue with issuing tokens
        String accessToken = jwtService.generateAccessToken(new HashMap<>(), user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        saveRefreshToken(refreshToken, user);

        ResponseCookie refreshCookie = createCookie("refreshToken", refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .needsPasswordSetup(user.getPassword() == null)
                        .build());
    }

    public ResponseEntity<?> refresh(String refreshToken) {
        log.info("Refreshing access token...");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not found");
        }

        String userEmail = jwtService.extractUsername(refreshToken);

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));

        if (jwtService.isRefreshTokenValid(refreshToken, user)) {
            // invalidate old access tokens
            jwtService.incrementAccessTokenVersion(user);
            userRepository.save(user);

            String newAccessToken = jwtService.generateAccessToken(new HashMap<>(), user);

            return ResponseEntity.ok()
                    .body(AuthenticationResponse.builder()
                            .accessToken(newAccessToken)
                            .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not valid");
    }

    // TODO: for future can be extended for the other 2 contexts
    public ResponseEntity<String> setNewPassword(SetPasswordRequest request) {
        log.info("Setting first password for user {}", request.getEmail());
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var context = request.getChangePasswordContext();
        if (context == ChangePasswordContext.FIRST_TIME) {
            if (user.getPassword() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is already set.");
            }
        }

        String newPassword = request.getPassword();
        if (!PasswordValidator.isPasswordValid(newPassword)) {
            throw new PasswordNotValidException("Password does not meet complexity requirements.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password set successfully.");
    }

    public UserDTO getCurrentUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getPassword() != null
        );
    }

    private void saveRefreshToken(String refreshToken, User user) {
        RefreshToken dbRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .isLoggedOut(false)
                .user(user)
                .build();
        refreshTokenRepository.save(dbRefreshToken);
    }

    private ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }
}
