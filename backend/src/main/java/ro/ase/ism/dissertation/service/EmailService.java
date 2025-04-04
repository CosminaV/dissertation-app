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

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.sender-email}")
    private String senderEmail;

    @Value("${login-url}")
    private String loginUrl;

    public void sendOtpEmails(String recipientEmail, String otp) {
        Email from = new Email(senderEmail);
        String subject = "Activate your Gradus account";
        Email to = new Email(recipientEmail);
        Content content = new Content("text/plain",
                "Hello,\n\nYour one-time password is: " + otp +
                        "\n\nUse this link to log in and select OTP as login option: " + loginUrl +
                        "\n\nNote: This OTP will expire in 15 minutes.");

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);

            log.info("Email sent to {}", recipientEmail);
        } catch (IOException ex) {
            log.error("Failed to send OTP email", ex);
            throw new EmailSendException("Could not send OTP email.");
        }
    }
}
