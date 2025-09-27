package com.fiap.sprint3sqlserver.api;

import com.fiap.sprint3sqlserver.api.dto.MotoRequest;
import com.fiap.sprint3sqlserver.api.dto.MotoResponse;
import com.fiap.sprint3sqlserver.domain.Moto;
import com.fiap.sprint3sqlserver.repository.MotoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/motos")
public class MotoController {

    private final MotoRepository repo;

    public MotoController(MotoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<MotoResponse> list() {
        return repo.findAll().stream()
                .map(m -> new MotoResponse(m.getId(), m.getPlaca(), m.getModelo(), m.getStatus()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotoResponse> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(m -> ResponseEntity.ok(new MotoResponse(m.getId(), m.getPlaca(), m.getModelo(), m.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MotoResponse> create(@Valid @RequestBody MotoRequest req) {
        if (repo.findByPlaca(req.placa()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Moto saved = repo.save(new Moto(req.placa(), req.modelo(), req.status()));
        return ResponseEntity
                .created(URI.create("/api/v1/motos/" + saved.getId()))
                .body(new MotoResponse(saved.getId(), saved.getPlaca(), saved.getModelo(), saved.getStatus()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotoResponse> update(@PathVariable Long id, @Valid @RequestBody MotoRequest req) {
        return repo.findById(id).map(existing -> {
            existing.setPlaca(req.placa());
            existing.setModelo(req.modelo());
            existing.setStatus(req.status());
            Moto saved = repo.save(existing);
            return ResponseEntity.ok(new MotoResponse(saved.getId(), saved.getPlaca(), saved.getModelo(), saved.getStatus()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
