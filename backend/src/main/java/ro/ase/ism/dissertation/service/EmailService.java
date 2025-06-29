package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${login-url}")
    private String loginUrl;

    public void sendOtpEmails(String recipientEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Activate your Gradus account");
        message.setText("Hello,\n\nYour one-time password is: " + otp +
                        "\n\nUse this link to log in and select OTP as login option: " + loginUrl +
                        "\n\nNote: This OTP will expire in 15 minutes.");
        javaMailSender.send(message);
        log.info("Email sent to {}", recipientEmail);
    }

//    @Value("${sendgrid.api-key}")
//    private String sendGridApiKey;
//
//    @Value("${sendgrid.sender-email}")
//    private String senderEmail;
//

//
//    public void sendOtpEmails(String recipientEmail, String otp) {
//        Email from = new Email(senderEmail);
//        String subject = "Activate your Gradus account";
//        Email to = new Email(recipientEmail);
//        Content content = new Content("text/plain",
//                "Hello,\n\nYour one-time password is: " + otp +
//                        "\n\nUse this link to log in and select OTP as login option: " + loginUrl +
//                        "\n\nNote: This OTP will expire in 15 minutes.");
//
//        Mail mail = new Mail(from, subject, to, content);
//
//        SendGrid sg = new SendGrid(sendGridApiKey);
//        Request request = new Request();
//        try {
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            sg.api(request);
//
//            log.info("Email sent to {}", recipientEmail);
//        } catch (IOException ex) {
//            log.error("Failed to send OTP email", ex);
//            throw new EmailSendException("Could not send OTP email.");
//        }
//    }
}
