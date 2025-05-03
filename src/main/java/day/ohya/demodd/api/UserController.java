package day.ohya.demodd.api;

import day.ohya.demodd.config.OpenApiConfig;
import day.ohya.demodd.model.DemoDDApiResponse;
import day.ohya.demodd.model.UpdateLocaleRequestDto;
import day.ohya.demodd.model.UserProfileDto;
import day.ohya.demodd.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @Operation(summary = "更新語系", description = "更新使用者語言偏好設定", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PatchMapping("/locale")
    public DemoDDApiResponse<Void> updateLocale(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody UpdateLocaleRequestDto body) {
        String code = jwt.getClaim("code");
        userService.updateLocale(code, body.locale());
        return DemoDDApiResponse.success();
    }

    @Operation(summary = "查詢個人資料", description = "取得目前登入用戶的詳細資訊", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping("/profile")
    public DemoDDApiResponse<UserProfileDto> profile(@AuthenticationPrincipal Jwt jwt) {
        String code = jwt.getSubject();
        return DemoDDApiResponse.success(userService.fetchProfile(code));
    }

}
