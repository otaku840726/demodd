package day.ohya.demodd.model;

import java.util.Map;

public record MailMessageDto(
        String fromEmail,
        String fromName,
        String toEmail,
        String subject,
        String htmlContent
//        Long templateId,
//        Map<String, Object> variables
) {}

