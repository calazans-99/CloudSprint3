package com.fiap.sprint3sqlserver.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoRequest(
        @NotBlank(message = "Placa é obrigatória")
        @Size(max = 10) String placa,

        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 100) String modelo,

        @NotBlank(message = "Status é obrigatório")
        @Size(max = 30) String status
) {}
