package day.ohya.demodd.model;

public record LoginResponseDto(String userCode, String token, Boolean isEmailVerified) {
}
