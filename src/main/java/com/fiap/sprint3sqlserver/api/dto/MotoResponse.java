package com.fiap.sprint3sqlserver.api.dto;

public record MotoResponse(
        Long id,
        String placa,
        String modelo,
        String status
) {}
