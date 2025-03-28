package ro.ase.ism.dissertation.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.exception.EmailSendException;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.sender-email}")
    private String senderEmail;

    @Value("${account.activation-link}")
    private String activationLink;

    private final UserRepository userRepository;

    public void sendActivationEmailsToAllUnverifiedUsers() {
        List<User> usersToNotify = userRepository.findAll().stream()
                .filter(user -> !user.isActivated() && user.getActivationToken() != null)
                .toList();

        for (User user : usersToNotify) {
            user.setActivationTokenExpiresAt(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            String customActivationLink = activationLink + user.getActivationToken();
            sendActivationEmail(user.getEmail(), customActivationLink);
        }
    }

    private void sendActivationEmail(String recipientEmail, String activationLink) {
        Email from = new Email(senderEmail);
        String subject = "Activate your Gradus account";
        Email to = new Email(recipientEmail);
        Content content = new Content(
                "text/plain",
                "Hello!\n\nIn order to activate your account, access this link: " + activationLink + "\n\nThis link will expire in 24 hours.");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            log.error("Error occured while sending the activation email", ex);
            throw new EmailSendException("Error occured while sending the activation email: " + ex);
        }
    }
}
