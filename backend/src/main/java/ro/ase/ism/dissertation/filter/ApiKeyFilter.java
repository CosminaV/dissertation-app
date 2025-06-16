package ro.ase.ism.dissertation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyFilter extends OncePerRequestFilter {
    private static final String HEADER = "X-API-KEY";
    private final String apiKey;

    public ApiKeyFilter(@Value("${biometrics.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("POST".equals(request.getMethod()) && request.getRequestURI().equals("/api/streaming/predictions")) {
            String key = request.getHeader(HEADER);
            if (key == null || !key.equals(apiKey)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Invalid API Key");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
