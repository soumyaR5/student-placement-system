package com.placement.sms.controller;

import com.placement.sms.dto.CriteriaRequest;
import com.placement.sms.dto.CriteriaResponse;
import com.placement.sms.service.PlacementCriteriaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/criteria")
public class PlacementCriteriaController {

    private final PlacementCriteriaService criteriaService;

    public PlacementCriteriaController(PlacementCriteriaService service) {
        criteriaService = service;
    }

    @PostMapping
    public ResponseEntity<CriteriaResponse> create(@Valid @RequestBody CriteriaRequest request) {
        CriteriaResponse created = criteriaService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CriteriaResponse>> getAll() {
        return ResponseEntity.ok(criteriaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CriteriaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(criteriaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CriteriaResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CriteriaRequest request) {
        return ResponseEntity.ok(criteriaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        criteriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
