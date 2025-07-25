package ro.ase.ism.dissertation.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.JwtService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor // creates a ctor using any final field it finds in the class
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // request
    // response
    // filterChain - chain of responsibility dp
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization"); //it has the Bearer token
        String jwt = null;
        String userEmail;
//        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            //extract the token from the auth header
            jwt = authorizationHeader.substring(7);
        }
        else {
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("SSE_TOKEN".equals(c.getName())) {
                        jwt = c.getValue();
                        break;
                    }
                }
            }
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        //extract the user's email from the JWT token
        userEmail = jwtService.extractUsername(jwt);
        // if user is not connected yet
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isAccessTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                if (userDetails instanceof User user && user.getPassword() == null) {
                    String path = request.getRequestURI();
                    if (!path.contains("/auth/set-password")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Password setup is required!");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
