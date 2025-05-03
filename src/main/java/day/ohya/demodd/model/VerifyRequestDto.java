package day.ohya.demodd.model;

import day.ohya.demodd.constant.VerifyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

@Schema(description = "驗證請求內容")
public record VerifyRequestDto(

        @NotNull(message = "{validation.required}")
        @Schema(description = "驗證類型")
        VerifyType type,

        @NotBlank(message = "{validation.required}")
        @Schema(description = "驗證 Token/OTP")
        String token
) {}
