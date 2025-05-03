package day.ohya.demodd.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.constant.JwtType;
import day.ohya.demodd.constant.RedisKey;
import day.ohya.demodd.constant.VerifyType;
import day.ohya.demodd.exception.BusinessException;
import day.ohya.demodd.security.JwtClaims;
import day.ohya.demodd.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;


    @Value("${app.register-expiration}")
    private Duration registerExpiration;
    @Value("${app.access-expiration}")
    private Duration accessExpiration;
    @Value("${app.temp-expiration}")
    private Duration tempExpiration;

    private final ObjectMapper objectMapper = new ObjectMapper() {{
        registerModule(new JavaTimeModule());
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }};

    public void setRegister(String userCode, Register register) throws BusinessException {
        String key = RedisKey.register(userCode);
        String val = toJson(register);
        redisTemplate.opsForValue().set(key, val, registerExpiration);
    }

    public Register getRegister(String userCode) {
        String key = RedisKey.register(userCode);
        String val = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return fromJson(val, Register.class);
    }

    public void setToken(String userCode, VerifyType type, String token) throws BusinessException {
        String key = RedisKey.verify(userCode, type);
        // salt 每次重新生成, 安全必要
        String salt = RandomUtils.salt(6);
        // 僅存放hash, 安全必要
        String hash = DigestUtils.sha256Hex(salt + token);
        String val = toJson(new VerifyToken(salt, hash));
        // 時效, 安全必要
        redisTemplate.opsForValue().set(key, val, Duration.ofMinutes(15));
    }

    public boolean verifyToken(String userCode, VerifyType type, String token) throws BusinessException {
        final int maxAttempts = 3;
        String key = RedisKey.verify(userCode, type);
        String errorKey = RedisKey.verifyError(userCode, type);

        String val = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(val)) {
            throw new BusinessException(ErrorCode.VERIFY_TOKEN_EXPIRE);
        }
        VerifyToken verifyToken = fromJson(val, VerifyToken.class);
        String hash = DigestUtils.sha256Hex(verifyToken.salt() + token);
        if (hash.equals(verifyToken.hash())) {
            // clear
            redisTemplate.delete(key);
            redisTemplate.delete(errorKey);
            return true;
        }

        Long errors = redisTemplate.opsForValue().increment(errorKey);
        if (errors == null) {
            log.error("redis increment 失敗, errorKey={}", errorKey);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        if (errors == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(15));
            return false;
        }

        if (errors >= maxAttempts) {
            redisTemplate.delete(key);
            redisTemplate.delete(errorKey);
            throw new BusinessException(ErrorCode.VERIFY_OVER_ATTEMPTS);
        }
        return false;
    }

    public void setUserJwt(JwtClaims jwtClaims) {
        String key = RedisKey.userJwt(jwtClaims.getSubject());
        String val = jwtClaims.getJti();
        redisTemplate.opsForHash().put(key, val, "1");
        redisTemplate.expire(key, JwtType.ACCESS.equals(jwtClaims.getType())
                ? accessExpiration : tempExpiration);
    }

    public boolean verifyUserJwt(JwtClaims jwtClaims) {
        String key = RedisKey.userJwt(jwtClaims.getSubject());
        String val = jwtClaims.getJti();
        Boolean hasToken = redisTemplate.opsForHash().hasKey(key, jwtClaims.getJti());
        return Boolean.TRUE.equals(hasToken);
    }

    private String toJson(Object object) throws BusinessException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("redis 序列化失敗, val={}", object, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private <T> T fromJson(String json, Class<T> dto) throws BusinessException {
        try {
            return objectMapper.readValue(json, dto);
        } catch (Exception e) {
            log.error("redis 反序列化失敗, val={}, dto={}", json, dto, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
