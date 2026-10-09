package com.placement.sms.repository;

import com.placement.sms.entity.PlacementCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementCriteriaRepository extends JpaRepository<PlacementCriteria, Long> {

    boolean existsByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCaseAndIdNot(String companyName, Long id);
}
