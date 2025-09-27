package com.fiap.sprint3sqlserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoRequest(
        @NotBlank @Size(max = 10) String placa,
        @NotBlank @Size(max = 80) String modelo,
        @NotBlank @Size(max = 30) String status
) {}
