package day.ohya.demodd.api;

import day.ohya.demodd.config.OpenApiConfig;
import day.ohya.demodd.model.*;
import day.ohya.demodd.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth", description = "認證相關 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "用戶註冊", description = "新用戶註冊帳號，需提供 email 與 password, 成功會回傳臨時Token")
    @ApiResponse(responseCode = "200", description = "註冊成功")
    @PostMapping("/register")
    public DemoDDApiResponse<String> register(@Valid @RequestBody RegisterRequestDto body) {
        return DemoDDApiResponse.success(authService.register(body.email(), body.password()));
    }

    @Operation(summary = "2FA驗證", description = "完成信箱驗證流程, 成功會回傳正式Token",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "驗證成功"),
            @ApiResponse(responseCode = "401", description = "驗證失敗或已過期")
    })
    @PostMapping("/verify")
    public DemoDDApiResponse<String> verify(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody VerifyRequestDto body) {
        return DemoDDApiResponse.success(authService.verify(jwt, body.type(), body.token()));
    }

    @Operation(summary = "用戶登入", description = "成功會回傳臨時Token, 繼續verify")
    @ApiResponse(responseCode = "200", description = "登入成功")
    @PostMapping("/login")
    public DemoDDApiResponse<String> login(@Valid @RequestBody LoginRequestDto body) {
        return DemoDDApiResponse.success(authService.login(body.email(), body.password()));
    }

}
