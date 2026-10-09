package com.placement.sms.controller;

import com.placement.sms.dto.EligibilityResponse;
import com.placement.sms.service.EligibilityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService service) {
        eligibilityService = service;
    }

    @GetMapping("/students/{studentId}/criteria/{criteriaId}")
    public ResponseEntity<EligibilityResponse> check(@PathVariable Long studentId,
                                                     @PathVariable Long criteriaId) {
        return ResponseEntity.ok(eligibilityService.check(studentId, criteriaId));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<EligibilityResponse>> checkAll(@PathVariable Long studentId) {
        return ResponseEntity.ok(eligibilityService.checkAll(studentId));
    }
}
