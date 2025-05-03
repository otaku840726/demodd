package day.ohya.demodd.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "註冊請求內容")
public record RegisterRequestDto(

        @Email(message = "{validation.email}")
        @NotBlank(message = "{validation.required}")
        @Schema(description = "使用者電子郵件")
        String email,

        @Size(min = 6, max = 100, message = "{validation.password.size}")
        @Schema(description = "使用者密碼")
        String password
) {}
