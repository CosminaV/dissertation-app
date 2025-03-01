package ro.ase.ism.dissertation.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.model.token.RefreshToken;
import ro.ase.ism.dissertation.repository.RefreshTokenRepository;

@RequiredArgsConstructor
@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String authorizationHeader = request.getHeader("Authorization");

        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authorizationHeader.substring(7);

        // get stored token from the db
        RefreshToken refreshToken = refreshTokenRepository.findByToken(jwt).orElse(null);
        // invalidate the token
        if(refreshToken != null) {
            refreshToken.setLoggedOut(true);
            refreshTokenRepository.save(refreshToken);
        }
    }
}
