package day.ohya.demodd.model;

import java.sql.Timestamp;

public record UserProfileDto(String code, String email, String nickname, String locale, Timestamp lastLoginAt) {}

