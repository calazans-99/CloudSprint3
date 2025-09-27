package com.fiap.sprint3sqlserver.repository;

import com.fiap.sprint3sqlserver.domain.Moto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotoRepository extends JpaRepository<Moto, Long> {
    Optional<Moto> findByPlaca(String placa);
}
