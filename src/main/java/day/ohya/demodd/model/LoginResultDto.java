package day.ohya.demodd.model;

public record LoginResultDto(String userCode, String token, Boolean isEmailVerified) {
}
