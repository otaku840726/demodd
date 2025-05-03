package day.ohya.demodd.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequestDto(@Email(message = "{validation.email}") @NotBlank(message = "{validation.required}") String email) {}

