package ro.ase.ism.dissertation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.auth.LogoutResponse;
import ro.ase.ism.dissertation.exception.ErrorResponse;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.service.JwtService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        log.info("Initializing logout...");
        String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorResponse("Token expired or invalid")));
            } catch (IOException e) {
                log.error("Error writing logout response", e);
                throw new RuntimeException(e);
            }
            return;
        }

        String jwt = authorizationHeader.substring(7);
        String userEmail = jwtService.extractUsername(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        if (jwtService.isAccessTokenValid(jwt, userDetails)) {
            var user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                jwtService.incrementAccessTokenVersion(user);
                userRepository.save(user);

                jwtService.revokeAllRefreshTokens(user);
            }

            ResponseCookie refreshCookie = deleteCookie("refreshToken", "");
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            try {
                response.getWriter().write(new ObjectMapper().writeValueAsString(new LogoutResponse("Logged out successfully")));
            } catch (IOException e) {
                log.error("Error writing logout response", e);
                throw new RuntimeException(e);
            }
        }
    }

    private ResponseCookie deleteCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }
}
