package com.placement.sms.service;

import com.placement.sms.dto.EligibilityResponse;
import com.placement.sms.dto.EligibilityStatus;
import com.placement.sms.entity.PlacementCriteria;
import com.placement.sms.entity.Student;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.repository.PlacementCriteriaRepository;
import com.placement.sms.repository.StudentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EligibilityService {

    private final StudentRepository studentRepository;
    private final PlacementCriteriaRepository criteriaRepository;

    public EligibilityService(StudentRepository students, PlacementCriteriaRepository criteria) {
        studentRepository = students;
        criteriaRepository = criteria;
    }

    /** Checks one student against one company's criteria. */
    public EligibilityResponse check(Long studentId, Long criteriaId) {
        Student student = findStudent(studentId);
        PlacementCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Placement criteria not found with id " + criteriaId));
        return evaluate(student, criteria);
    }

    /** Checks one student against every configured company. */
    public List<EligibilityResponse> checkAll(Long studentId) {
        Student student = findStudent(studentId);
        List<EligibilityResponse> l1 = criteriaRepository.findAll(Sort.by("companyName")).stream()
                .map(criteria -> evaluate(student, criteria))
                .toList();
        return l1;
    }

    /**
     * Core rule engine: a student is eligible only if CGPA >= minimum CGPA
     * AND active backlogs <= maximum allowed active backlogs.
     */
    public EligibilityResponse evaluate(Student student, PlacementCriteria criteria) {
        BigDecimal cgpa = student.getAcademicRecord().getCgpa();
        int backlogs = student.getAcademicRecord().getActiveBacklogs();
        List<String> l1 = new ArrayList<>();
        boolean eligible = true;

        if (cgpa.compareTo(criteria.getMinCgpa()) >= 0) {
            l1.add("CGPA " + fmt(cgpa) + " meets the minimum requirement of " + fmt(criteria.getMinCgpa()));
        } else {
            eligible = false;
            l1.add("CGPA " + fmt(cgpa) + " is below the minimum requirement of " + fmt(criteria.getMinCgpa()));
        }

        if (backlogs <= criteria.getMaxActiveBacklogs()) {
            l1.add("Active backlogs " + backlogs + " are within the maximum allowed of "
                    + criteria.getMaxActiveBacklogs());
        } else {
            eligible = false;
            l1.add("Active backlogs " + backlogs + " exceed the maximum allowed of "
                    + criteria.getMaxActiveBacklogs());
        }

        return new EligibilityResponse(
                student.getId(),
                student.getFullName(),
                student.getRollNumber(),
                criteria.getId(),
                criteria.getCompanyName(),
                eligible ? EligibilityStatus.ELIGIBLE : EligibilityStatus.INELIGIBLE,
                eligible,
                l1);
    }

    private Student findStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
    }

    private static String fmt(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
