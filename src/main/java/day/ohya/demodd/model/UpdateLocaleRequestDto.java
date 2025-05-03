package day.ohya.demodd.model;

import jakarta.validation.constraints.NotBlank;

public record UpdateLocaleRequestDto(@NotBlank(message = "{validation.required}") String locale) {}

