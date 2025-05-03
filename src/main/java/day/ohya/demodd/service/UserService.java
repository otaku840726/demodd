package day.ohya.demodd.service;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.exception.BusinessException;
import day.ohya.demodd.model.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static day.ohya.demodd.jooq.Tables.USER;

@Service
@RequiredArgsConstructor
public class UserService {
    private final DSLContext dsl;

    public void updateLocale(String code, String locale) {
        int updated = dsl.update(USER)
                .set(USER.LOCALE, locale)
                .where(USER.CODE.eq(code))
                .execute();

        if (updated == 0) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    public UserProfileDto fetchProfile(String code) {
        return dsl.select(USER.CODE, USER.EMAIL, USER.NICKNAME, USER.LOCALE, USER.LAST_LOGIN_AT)
                .from(USER)
                .where(USER.CODE.eq(code))
                .fetchOptional()
                .map(r -> new UserProfileDto(
                        r.get(USER.CODE),
                        r.get(USER.EMAIL),
                        r.get(USER.NICKNAME),
                        r.get(USER.LOCALE),
                        Timestamp.valueOf(r.get(USER.LAST_LOGIN_AT))
                ))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}