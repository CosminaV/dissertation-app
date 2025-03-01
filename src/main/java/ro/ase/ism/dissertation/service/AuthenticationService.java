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
import ro.ase.ism.dissertation.auth.AuthenticationRequest;
import ro.ase.ism.dissertation.auth.AuthenticationResponse;
import ro.ase.ism.dissertation.auth.RegisterRequest;
import ro.ase.ism.dissertation.auth.RegisterResponse;
import ro.ase.ism.dissertation.auth.validator.PasswordValidator;
import ro.ase.ism.dissertation.exception.PasswordNotValidException;
import ro.ase.ism.dissertation.exception.UserAlreadyExistsException;
import ro.ase.ism.dissertation.model.token.RefreshToken;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.RefreshTokenRepository;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.sql.Ref;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("User with email %s already exists.".formatted(request.getEmail()));
            throw new UserAlreadyExistsException("User with email %s already exists.".formatted(request.getEmail()));
        }

        if (!PasswordValidator.isPasswordValid(request.getPassword())) {
            log.error("Password does not meet complexity requirements.");
            throw new PasswordNotValidException("Password does not meet complexity requirements.");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        log.info("User registered successfully: %s".formatted(request.getEmail()));
        return new RegisterResponse("User registered successfully: %s".formatted(request.getEmail()));
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var accessToken = jwtService.generateAccessToken(new HashMap<>(), user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        // revoke previous refresh tokens for this user
        revokeAllRefreshTokens(user);

        // save the refresh token in the db
        saveRefreshToken(refreshToken, user);

        //create cookie for the refresh token
        ResponseCookie refreshCookie = createCookie("refreshToken", refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build());
    }

    public ResponseEntity<?> refresh(String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not found");
        }

        String userEmail = jwtService.extractUsername(refreshToken);

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));

        if(jwtService.isRefreshTokenValid(refreshToken, user)) {
            String newAccessToken = jwtService.generateAccessToken(new HashMap<>(), user);
            String newRefreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

            incrementAccessTokenVersion(user);

            ResponseCookie refreshCookie = createCookie("refreshToken", newRefreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(AuthenticationResponse.builder()
                            .accessToken(newAccessToken)
                            .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not valid");
    }

    private void revokeAllRefreshTokens(User user) {
        List<RefreshToken> validRefreshTokens =
                refreshTokenRepository.findAllRefreshTokenByUser(user.getId());
        if(!validRefreshTokens.isEmpty()) {
            validRefreshTokens.forEach(rt -> {
                rt.setLoggedOut(true);
            });
        }

        refreshTokenRepository.saveAll(validRefreshTokens);
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
                .sameSite("Strict")
                .path("/api/v1/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }

    private void incrementAccessTokenVersion(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}
