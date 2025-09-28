package com.fiap.sprint3sqlserver.api;

import com.fiap.sprint3sqlserver.api.dto.MotoRequest;
import com.fiap.sprint3sqlserver.api.dto.MotoResponse;
import com.fiap.sprint3sqlserver.domain.Moto;
import com.fiap.sprint3sqlserver.repository.MotoRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequestMapping(value = "/api/v1/motos", produces = MediaType.APPLICATION_JSON_VALUE)
public class MotoController {

    private final MotoRepository repo;

    public MotoController(MotoRepository repo) {
        this.repo = repo;
    }

    // LIST
    @GetMapping
    public List<MotoResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // GET ONE
    @GetMapping("/{id}")
    public ResponseEntity<MotoResponse> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(m -> ResponseEntity.ok(toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MotoResponse> create(@Valid @RequestBody MotoRequest req) {
        Moto m = new Moto(
                normalize(req.placa()),
                safeTrim(req.modelo()),
                safeTrim(req.status())
        );
        try {
            m = repo.save(m);
        } catch (DataIntegrityViolationException e) {
            // ÍNDICE ÚNICO de placa violado -> 409
            throw new ResponseStatusException(CONFLICT, "Placa já cadastrada");
        }
        return ResponseEntity
                .created(URI.create("/api/v1/motos/" + m.getId()))
                .body(toResponse(m));
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<MotoResponse> update(@PathVariable Long id, @Valid @RequestBody MotoRequest req) {
        return repo.findById(id)
                .map(m -> {
                    m.setPlaca(normalize(req.placa()));
                    m.setModelo(safeTrim(req.modelo()));
                    m.setStatus(safeTrim(req.status()));
                    try {
                        Moto saved = repo.save(m);
                        return ResponseEntity.ok(toResponse(saved));
                    } catch (DataIntegrityViolationException e) {
                        throw new ResponseStatusException(CONFLICT, "Placa já cadastrada");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- helpers ---
    private MotoResponse toResponse(Moto m) {
        return new MotoResponse(m.getId(), m.getPlaca(), m.getModelo(), m.getStatus());
    }
    private static String safeTrim(String s) { return s == null ? null : s.trim(); }
    private static String normalize(String placa) { return placa == null ? null : placa.trim().toUpperCase(); }
}
