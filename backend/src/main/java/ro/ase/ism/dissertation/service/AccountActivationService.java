package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private final UserRepository userRepository;

    public User activateUser(String token) {
        Optional<User> optionalUser = userRepository.findByActivationToken(token);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Activation token not valid.");
        }

        User user = optionalUser.get();

        if (user.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Activation token expired.");
        }

        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);

        String passwordToken = generateSecureToken();
        user.setPasswordSetupToken(passwordToken);
        user.setPasswordSetupTokenExpiresAt(LocalDateTime.now().plusHours(1));
        user.setPendingPasswordSetup(true);

        userRepository.save(user);
        return user;
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
