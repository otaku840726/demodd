package day.ohya.demodd.utils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class RandomUtils {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String uuid() {
        long timestamp = System.currentTimeMillis();
        String hexTimestamp = Long.toHexString(timestamp);
        String uuid = UUID.randomUUID().toString();
        return hexTimestamp + "-" + uuid;
    }

    public static String salt(int length) {
        byte[] saltBytes = new byte[length];
        secureRandom.nextBytes(saltBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
    }

    public static String numericOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10)); // 0–9
        }
        return otp.toString();
    }
}
