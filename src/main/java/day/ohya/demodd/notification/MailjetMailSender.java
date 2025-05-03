//package day.ohya.demodd.notification;
//
//import com.mailjet.client.ClientOptions;
//import com.mailjet.client.MailjetClient;
//import com.mailjet.client.MailjetRequest;
//import com.mailjet.client.MailjetResponse;
//import com.mailjet.client.resource.Emailv31;
//import day.ohya.demodd.model.MailMessageDto;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MailjetMailSender implements MailSender {
//
//    private final MailjetClient client;
//    private final String fromEmail;
//    private final String fromName;
//
//    public MailjetMailSender(
//            @Value("${mailjet.api-key}") String apiKey,
//            @Value("${mailjet.api-secret}") String apiSecret,
//            @Value("${mailjet.from-email}") String fromEmail,
//            @Value("${mailjet.from-name}") String fromName
//    ) {
//        ClientOptions options = ClientOptions.builder()
//                .apiKey(apiKey)
//                .apiSecretKey(apiSecret)
//                .build();
//
//        this.client = new MailjetClient(options);
//        this.fromEmail = fromEmail;
//        this.fromName = fromName;
//    }
//
//    @Override
//    public void send(MailMessageDto message) throws Exception {
//        MailjetRequest request = new MailjetRequest(Emailv31.resource)
//                .property(Emailv31.MESSAGES, new JSONArray()
//                        .put(new JSONObject()
//                                .put(Emailv31.Message.FROM, new JSONObject()
//                                        .put("Email", fromEmail)
//                                        .put("Name", fromName))
//                                .put(Emailv31.Message.TO, new JSONArray()
//                                        .put(new JSONObject()
//                                                .put("Email", message.toEmail())
//                                                .put("Name", message.toName())))
//                                .put(Emailv31.Message.SUBJECT, message.subject())
//                                .put(Emailv31.Message.HTMLPART, message.htmlContent())
//                        )
//                );
//
//
//        MailjetResponse response = client.post(request);
//        if (response.getStatus() != 200) {
//            throw new IllegalStateException("Mailjet 發信失敗: " + response.getStatus() + " - " + response.getData());
//        }
//    }
//}
//
