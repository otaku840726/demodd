// 測試代碼將加入於對應的 test/java/day/ohya/demodd 下的 auth、redis、notification、controller 模組
// 包含 Testcontainers 整合與實際的 Controller 測試（/register, /verify, /profile）

package day.ohya.demodd;

import com.redis.testcontainers.RedisContainer;
import day.ohya.demodd.model.MailMessageDto;
import day.ohya.demodd.notification.MailSender;
import day.ohya.demodd.notification.MailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("demo")
            .withUsername("root")
            .withPassword("root");

    @Container
    static RedisContainer redis = new RedisContainer("redis/redis-stack-server:7.2.0-v15")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @MockBean
    MailService mailService;

    @Captor
    ArgumentCaptor<String> emailCaptor;

    @Captor
    ArgumentCaptor<String> tokenCaptor;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterAndVerifyFlow() {
        String baseUrl = "http://localhost:" + port;

        // 註冊
        var registerBody = Map.of("email", "test@example.com", "password", "Password123");
        var registerRes = rest.postForEntity(baseUrl + "/register", registerBody, Map.class);
        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerRes.getBody()).containsKey("data");

        // 取得 OTP
        verify(mailService).sendVerify(emailCaptor.capture(), tokenCaptor.capture());
        String otp = tokenCaptor.getValue();
        String capturedEmail = emailCaptor.getValue();
        assertThat(capturedEmail).isEqualTo("test@example.com");
        assertThat(otp).hasSize(6);

        // 模擬 OTP 驗證
        var token = registerRes.getBody().get("data").toString();
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        var verifyReq = new HttpEntity<>(Map.of("token", otp, "type", "EMAIL"), headers);

        var verifyRes = rest.exchange(baseUrl + "/verify", HttpMethod.POST, verifyReq, Map.class);
        assertThat(verifyRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyRes.getBody()).containsKey("data");

        // 驗證完成後請求 profile
        String finalToken = verifyRes.getBody().get("data").toString();
        headers.setBearerAuth(finalToken);
        var profileRes = rest.exchange(baseUrl + "/user/profile", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(profileRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileRes.getBody()).containsKey("data");
        Map<?, ?> profileData = (Map<?, ?>) profileRes.getBody().get("data");
        assertThat(profileData.get("email")).isEqualTo("test@example.com");
    }
}
