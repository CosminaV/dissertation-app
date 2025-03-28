package ro.ase.ism.dissertation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.AccountActivationService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ActivationController {

    private final AccountActivationService activationService;

    @Value("${set-password-url}")
    private String setPasswordUrl;

    @GetMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("token") String token) {
        try {
            User user = activationService.activateUser(token);
            String redirectUrl = setPasswordUrl +
                    "?email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8) +
                    "&token=" + URLEncoder.encode(user.getPasswordSetupToken(), StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));

            return ResponseEntity.status(HttpStatus.FOUND) // 302 found
                    .headers(headers)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
    }
}
