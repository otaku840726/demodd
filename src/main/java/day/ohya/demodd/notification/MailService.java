package day.ohya.demodd.notification;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.exception.BusinessException;
import day.ohya.demodd.locale.LocaleService;
import day.ohya.demodd.model.MailMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailService {

    private static final String PREFIX = "emailVerify:";
    private final RedisTemplate<String, String> redis;

    private final MailSender mailSender;
    private final LocaleService localeService;

    @Value("${app.mail-from}")
    private String mailFrom;
    @Value("${app.mail-name}")
    private String mailName;

    public void sendVerify(String email, String token) throws BusinessException {
        try {

            String subject = "${email.verify.subject}";

            String htmlContent = """
                    <html>
                      <body style="margin:0; padding:0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f9f9f9;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f9f9f9; padding: 40px 0;">
                          <tr>
                            <td align="center">
                              <table width="480" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                                <tr>
                                  <td style="font-size: 18px; color: #333333; padding-bottom: 16px;">
                                    ${email.verify.body}
                                  </td>
                                </tr>
                                <tr>
                                  <td align="center" style="padding: 16px 0;">
                                    <div style="display: inline-block; font-size: 32px; font-weight: 700; color: #2c3e50; background-color: #eef2f7; padding: 16px 32px; border-radius: 8px; letter-spacing: 6px;">
                                      %s
                                    </div>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="font-size: 14px; color: #777777; padding-top: 24px;">
                                    ${email.verify.validity}
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                      </body>
                    </html>
                    """.formatted(token);

            mailSender.send(new MailMessageDto(
                    mailFrom,
                    mailName,
                    email,
                    localeService.resolveMessageTemplate(subject),
                    localeService.resolveMessageTemplate(htmlContent)
            ));
        } catch (Exception e) {
            log.error("電子郵件發送失敗, email={}", email, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public boolean verify(String userCode, String token) {
        Boolean hasToken = redis.opsForHash().hasKey(PREFIX + userCode, token);
        return Boolean.TRUE.equals(hasToken);
    }
}
