package day.ohya.demodd.constant;

public class RedisKey {

    public static String register(String userCode) {
        return "register:%s".formatted(userCode);
    }

    public static String verify(String userCode, VerifyType type) {
        return "verify:%s:%s".formatted(type, userCode);
    }

    public static String verifyError( String userCode, VerifyType type) {
        return "verify-error:%s:%s".formatted(type, userCode);
    }

    public static String userJwt(String userCode) {
        return "user-jwt:%s:".formatted(userCode);
    }

}
