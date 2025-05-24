package ro.ase.ism.dissertation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import ro.ase.ism.dissertation.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).preload(true)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/authenticate",
                                "/api/auth/authenticate-otp",
                                "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/admin_only/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/common/study-years").hasAuthority("ADMIN")
                        .requestMatchers("/api/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers("/api/student/**").hasAuthority("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/user/profile-image").hasAnyAuthority("STUDENT", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/user/profile-image-url").hasAnyAuthority("STUDENT", "TEACHER")
                        .anyRequest()
                        .authenticated()
                ).userDetailsService(userDetailsService)
                .logout(l -> l.logoutUrl("/api/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(
                                ((request, response, authentication) -> {
                                    response.setContentType("application/json");
                                    SecurityContextHolder.clearContext();
                                })
                        ))
                .exceptionHandling(e -> e
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // more secure version of the default HttpFirewall - blocks things like encoded clashes or malformed headers
        return webSecurity -> webSecurity.httpFirewall(new StrictHttpFirewall());
    }
}
