package day.ohya.demodd.service;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.constant.JwtType;
import day.ohya.demodd.constant.VerifyType;
import day.ohya.demodd.exception.BusinessException;
import day.ohya.demodd.jooq.tables.User;
import day.ohya.demodd.jooq.tables.records.UserRecord;
import day.ohya.demodd.locale.LocaleService;
import day.ohya.demodd.model.LoginResultDto;
import day.ohya.demodd.notification.MailService;
import day.ohya.demodd.redis.RedisService;
import day.ohya.demodd.redis.Register;
import day.ohya.demodd.security.JwtClaims;
import day.ohya.demodd.security.JwtService;
import day.ohya.demodd.utils.RandomUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static day.ohya.demodd.jooq.Tables.USER;
import static day.ohya.demodd.jooq.Tables.USER_IDENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final DSLContext dsl;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final RedisService redisService;
    private final LocaleService localeService;


    @Transactional(rollbackFor = Exception.class)
    public String register(String email, String password) {
        try {
            boolean emailExist = dsl.fetchExists(USER_IDENTITY,
                    USER_IDENTITY.IDENTITY_TYPE.eq("PASSWORD")
                            .and(USER_IDENTITY.IDENTIFIER.eq(email)));
            if (emailExist) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }

            String userCode = RandomUtils.uuid();
            // 註冊資料, 暫存redis, 敏感數據加密
            redisService.setRegister(userCode, new Register(email, encoder.encode(password)));
            // 生成暫時JWT token
            JwtClaims jwt = jwtService.generateToken(userCode, true, null);
            checkRequired2FA(jwt);

            return jwt.getToken();
        } catch (Exception e) {
            log.info("註冊失敗", e);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    public String login(String email, String password) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        log.info("{}", auth);
        String userCode = auth.getName();

        JwtClaims jwt = jwtService.generateToken(userCode, false, null);
        checkRequired2FA(jwt);

        return jwt.getToken();
    }

    public void checkRequired2FA(JwtClaims jwtClaims) {
        if (JwtType.ACCESS.equals(jwtClaims.getType())) {
            return;
        }

        Optional<VerifyType> nextVerify = jwtClaims.getVerify().entrySet()
                .stream()
                .filter(x -> Boolean.FALSE.equals(x.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();
        if (nextVerify.isEmpty()) {
            return;
        }

        if (VerifyType.EMAIL.equals(nextVerify.get())) {
            // 生成otp, 發送郵件
            String otp = RandomUtils.numericOtp(6);
            String userCode = jwtClaims.getSubject();
            redisService.setToken(userCode, VerifyType.EMAIL, otp);
            String email = jwtClaims.getIsRegister()
                    ? redisService.getRegister(userCode).email()
                    : dsl.select(USER.EMAIL).from(USER).where(USER.CODE.eq(userCode)).fetchOneInto(String.class);
            mailService.sendVerify(email, otp);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String verify(Jwt jwt, VerifyType type, String token) throws BusinessException {
        String code = jwt.getSubject();
        if (!redisService.verifyToken(jwt.getSubject(), type, token)) {
            log.info("驗證失敗, code={} type={}", code, type);
            throw new BusinessException(ErrorCode.VERIFY_TOKEN_ERROR);
        }
        log.info("驗證成功, code={} type={}", code, type);
        JwtClaims jwtClaims = JwtClaims.fromJwt(jwt);
        var newJwt = jwtService.generateToken(jwtClaims, type);
        checkRequired2FA(newJwt);
        if (newJwt.getType().equals(JwtType.ACCESS) ) {
            if (jwtClaims.getIsRegister()) {
                // 這裡是處理註冊的
                Register register = redisService.getRegister(code);
                UserRecord user = dsl.insertInto(USER)
                        .set(USER.CODE, code)
                        .set(USER.EMAIL, register.email())
                        .set(USER.NICKNAME, register.email())
                        .set(User.USER.LOCALE, localeService.getLocale().getLanguage())
                        .returning()
                        .fetchSingle();
                log.info("user={}", user);
                var userIdentity = dsl.insertInto(USER_IDENTITY)
                        .set(USER_IDENTITY.USER_CODE, user.getCode())
                        .set(USER_IDENTITY.IDENTITY_TYPE, "PASSWORD")
                        .set(USER_IDENTITY.IDENTIFIER, register.email())
                        .set(USER_IDENTITY.CREDENTIALS, register.password())
                        .returning().fetchSingle();
                log.info("userIdentity={}", userIdentity);
                log.info("註冊完成,首次登入");
            } else {
                dsl.update(USER)
                        .set(USER.LAST_LOGIN_AT, LocalDateTime.now())
                        .where(USER.CODE.eq(code))
                        .execute();
            }

        }
        return newJwt.getToken();
    }

}