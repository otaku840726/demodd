package day.ohya.demodd.notification;

import day.ohya.demodd.model.MailMessageDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpMailSender implements MailSender {

    private final JavaMailSender mailSender;
    private final Environment environment;

    @Async
    @Override
    public void send(MailMessageDto message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(message.fromEmail());
            helper.setTo(message.toEmail());
            helper.setSubject("%s %s".formatted(
                    StringUtils.join(environment.getActiveProfiles()).toUpperCase(),
                    message.subject())
            );

            // ✅ HTML 內容設為 true
            helper.setText(message.htmlContent(), true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("電子郵件發送失敗", e);
        }
    }
}
