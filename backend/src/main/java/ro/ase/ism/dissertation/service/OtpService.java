package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.model.otp.OneTimePassword;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.OneTimePasswordRepository;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRATION_MINUTES = 15;

    public void generateAndSendOtpsToUsersWithoutPassword() {
        List<User> usersWithoutPassword = userRepository.findAll().stream()
                .filter(user -> user.getPassword() == null)
                .toList();

        for (User user : usersWithoutPassword) {
            boolean alreadyHasOtp = oneTimePasswordRepository.findByUserAndUsedFalse(user)
                    .filter(otp -> otp.getExpiresAt().isAfter(LocalDateTime.now()))
                    .isPresent();

            if (alreadyHasOtp) {
                log.info("User {} already has a valid OTP. Skipping.", user.getEmail());
                continue;
            }

            String generatedOtp = generateOtp();

            OneTimePassword otp = OneTimePassword.builder()
                    .user(user)
                    .otp(passwordEncoder.encode(generatedOtp))
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                    .used(false)
                    .build();

            oneTimePasswordRepository.save(otp);

            emailService.sendOtpEmails(user.getEmail(), generatedOtp);
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        byte[] otp = new byte[16];
        random.nextBytes(otp);
        StringBuilder sb = new StringBuilder();
        for(byte b : otp) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
