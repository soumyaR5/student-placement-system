package com.placement.sms.service;

import com.placement.sms.dto.CriteriaRequest;
import com.placement.sms.dto.CriteriaResponse;
import com.placement.sms.entity.PlacementCriteria;
import com.placement.sms.exception.DuplicateResourceException;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.repository.PlacementCriteriaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlacementCriteriaService {

    private final PlacementCriteriaRepository criteriaRepository;

    public PlacementCriteriaService(PlacementCriteriaRepository repository) {
        criteriaRepository = repository;
    }

    @Transactional
    public CriteriaResponse create(CriteriaRequest request) {
        String companyName = request.companyName().trim();
        if (criteriaRepository.existsByCompanyNameIgnoreCase(companyName)) {
            throw new DuplicateResourceException("Criteria for company " + companyName + " already exist");
        }
        PlacementCriteria criteria = new PlacementCriteria();
        applyValues(criteria, request);
        return toResponse(criteriaRepository.save(criteria));
    }

    @Transactional(readOnly = true)
    public List<CriteriaResponse> getAll() {
        List<CriteriaResponse> l1 = criteriaRepository.findAll(Sort.by("id")).stream()
                .map(PlacementCriteriaService::toResponse)
                .toList();
        return l1;
    }

    @Transactional(readOnly = true)
    public CriteriaResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public CriteriaResponse update(Long id, CriteriaRequest request) {
        PlacementCriteria existing = findOrThrow(id);
        String companyName = request.companyName().trim();
        if (criteriaRepository.existsByCompanyNameIgnoreCaseAndIdNot(companyName, id)) {
            throw new DuplicateResourceException("Criteria for company " + companyName + " already exist");
        }
        applyValues(existing, request);
        return toResponse(criteriaRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!criteriaRepository.existsById(id)) {
            throw notFound(id);
        }
        criteriaRepository.deleteById(id);
    }

    private PlacementCriteria findOrThrow(Long id) {
        return criteriaRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Placement criteria not found with id " + id);
    }

    private static void applyValues(PlacementCriteria criteria, CriteriaRequest request) {
        criteria.setCompanyName(request.companyName().trim());
        criteria.setMinCgpa(request.minCgpa());
        criteria.setMaxActiveBacklogs(request.maxActiveBacklogs());
    }

    private static CriteriaResponse toResponse(PlacementCriteria criteria) {
        return new CriteriaResponse(criteria.getId(), criteria.getCompanyName(),
                criteria.getMinCgpa(), criteria.getMaxActiveBacklogs());
    }
}
