package day.ohya.demodd.notification;

import day.ohya.demodd.model.MailMessageDto;

public interface MailSender {
    void send(MailMessageDto message) throws Exception;
}
