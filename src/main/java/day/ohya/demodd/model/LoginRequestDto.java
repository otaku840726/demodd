package day.ohya.demodd.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "登入請求內容")
public record LoginRequestDto(

        @Email(message = "{validation.email}") @NotBlank(message = "{validation.required}")
        @Schema(description = "電子郵件")
        String email,

        @NotBlank(message = "{validation.required}")
        @Schema(description = "密碼")
        String password
) {}
