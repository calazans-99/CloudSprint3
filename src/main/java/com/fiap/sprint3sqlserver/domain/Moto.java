package com.fiap.sprint3sqlserver.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "motos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_motos_placa", columnNames = "placa")
})
public class Moto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Placa é obrigatória")
    @Size(max = 10, message = "Placa deve ter no máximo 10 caracteres")
    @Column(nullable = false, length = 10)
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String modelo;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 30, message = "Status deve ter no máximo 30 caracteres")
    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime2")
    private OffsetDateTime updatedAt;

    public Moto() { }

    public Moto(String placa, String modelo, String status) {
        this.placa = placa;
        this.modelo = modelo;
        this.status = status;
    }

    /* --- Callbacks JPA --- */

    @PrePersist
    public void prePersist() {
        normalize();
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = createdAt; // primeira atualização = criação
    }

    @PreUpdate
    public void preUpdate() {
        normalize();
        updatedAt = OffsetDateTime.now();
    }

    private void normalize() {
        if (placa != null) placa = placa.trim().toUpperCase();
        if (modelo != null) modelo = modelo.trim();
        if (status != null) status = status.trim().toUpperCase();
    }

    /* --- Getters/Setters --- */

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setPlaca(String placa) { this.placa = placa; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    /* --- equals/hashCode baseado em ID --- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Moto moto)) return false;
        return id != null && id.equals(moto.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /* --- toString útil para logs --- */

    @Override
    public String toString() {
        return "Moto{" +
                "id=" + id +
                ", placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
