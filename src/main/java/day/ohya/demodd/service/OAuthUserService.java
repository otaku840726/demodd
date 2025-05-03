package day.ohya.demodd.service;

import day.ohya.demodd.constant.ErrorCode;
import day.ohya.demodd.exception.BusinessException;
import day.ohya.demodd.jooq.tables.records.UserIdentityRecord;
import day.ohya.demodd.security.OAuthUserInfo;
import day.ohya.demodd.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static day.ohya.demodd.jooq.tables.User.USER;
import static day.ohya.demodd.jooq.tables.UserIdentity.USER_IDENTITY;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
    private final DSLContext dsl;

    public String getOrCreateUser(String identityType, OAuthUserInfo info) {
        return dsl.selectFrom(USER_IDENTITY)
                .where(USER_IDENTITY.IDENTITY_TYPE.eq(identityType))
                .and(USER_IDENTITY.IDENTIFIER.eq(info.identifier()))
                .fetchOptional()
                .map(UserIdentityRecord::getUserCode)
                .orElseGet(() -> {
                    String code = RandomUtils.uuid();
                    var insertUser = Optional.ofNullable(
                            dsl.insertInto(USER)
                                    .set(USER.CODE, code)
                                    .set(USER.EMAIL, info.email())
                                    .set(USER.NICKNAME, info.name())
                                    .returning(USER.ID)
                                    .fetchOne()
                    ).orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS));

                    dsl.insertInto(USER_IDENTITY)
                            .set(USER_IDENTITY.USER_CODE, insertUser.getCode())
                            .set(USER_IDENTITY.IDENTITY_TYPE, identityType)
                            .set(USER_IDENTITY.IDENTIFIER, info.identifier())
                            .set(USER_IDENTITY.CREDENTIALS, "-oauth-")
                            .execute();

                    return insertUser.getCode();
                });
    }

//    public String getUserCode(Long userId) {
//        return dsl.selectFrom(USER)
//                .where(USER.ID.eq(userId))
//                .fetchOptional()
//                .map(UserRecord::getCode)
//                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
//    }
}