package day.ohya.demodd.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo API")
                        .version("1.0")
                        .description("""
                                登入與註冊流程說明

                                本系統採用多階段二因素驗證（2FA）登入與註冊機制，流程如下：

                                1. 使用者透過 `POST /register` 或 `POST /login` 發起請求，系統會回傳一組暫時 JWT Token。
                                2. 系統根據 JWT Claims 判斷該 Token 是否為已完成所有驗證的正式 Token：
                                   - 若是正式 Token，表示登入成功，可存取受保護資源。
                                   - 若是暫時 Token，表示尚未完成所有二因素驗證。
                                3. 系統會從 Claims 中判斷尚需哪些驗證步驟（如 Email 驗證碼、手機 OTP 等）。
                                4. 使用者依照指示輸入對應 OTP，並呼叫 `POST /verify` 驗證。
                                5. 驗證成功後，系統會回傳新的 Token，重複步驟 2~4，直到取得正式 Token 為止。

                                此流程設計可支援彈性的多因素驗證組合，並確保登入與註冊流程一致且安全。
                                """))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

//    @Bean
//    @Primary
//    public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties config) {
//        SwaggerUiConfigProperties.Csrf csrf = new SwaggerUiConfigProperties.Csrf();
//        csrf.setEnabled(true);
//        csrf.setUseSessionStorage(true);
//        config.setCsrf(csrf);
//        return config;
//    }
}
