package com.placement.sms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.placement.sms.dto.EligibilityResponse;
import com.placement.sms.dto.EligibilityStatus;
import com.placement.sms.entity.AcademicRecord;
import com.placement.sms.entity.PlacementCriteria;
import com.placement.sms.entity.Student;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.repository.PlacementCriteriaRepository;
import com.placement.sms.repository.StudentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class EligibilityServiceTest {

    private StudentRepository studentRepository;
    private PlacementCriteriaRepository criteriaRepository;
    private EligibilityService service;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        criteriaRepository = mock(PlacementCriteriaRepository.class);
        service = new EligibilityService(studentRepository, criteriaRepository);
    }

    @Test
    void eligibleWhenCgpaAndBacklogsMeetCriteria() {
        EligibilityResponse r = service.evaluate(student(1L, "8.75", 0), criteria(1L, "Google", "8.50", 0));

        assertEquals(EligibilityStatus.ELIGIBLE, r.status());
        assertTrue(r.eligible());
        assertEquals(2, r.reasons().size());
        assertEquals("CGPA 8.75 meets the minimum requirement of 8.50", r.reasons().get(0));
    }

    @Test
    void eligibleAtExactBoundaryValues() {
        EligibilityResponse r = service.evaluate(student(1L, "8.50", 1), criteria(1L, "TCS", "8.50", 1));

        assertEquals(EligibilityStatus.ELIGIBLE, r.status());
    }

    @Test
    void ineligibleWhenCgpaBelowMinimum() {
        EligibilityResponse r = service.evaluate(student(1L, "7.99", 0), criteria(1L, "Microsoft", "8.00", 0));

        assertEquals(EligibilityStatus.INELIGIBLE, r.status());
        assertFalse(r.eligible());
        assertEquals("CGPA 7.99 is below the minimum requirement of 8.00", r.reasons().get(0));
        assertTrue(r.reasons().get(1).contains("within the maximum allowed"));
    }

    @Test
    void ineligibleWhenBacklogsExceedMaximum() {
        EligibilityResponse r = service.evaluate(student(1L, "9.00", 2), criteria(1L, "TCS", "7.00", 1));

        assertEquals(EligibilityStatus.INELIGIBLE, r.status());
        assertEquals("Active backlogs 2 exceed the maximum allowed of 1", r.reasons().get(1));
    }

    @Test
    void ineligibleWithBothReasonsWhenBothRulesFail() {
        EligibilityResponse r = service.evaluate(student(1L, "7.20", 1), criteria(1L, "Google", "8.50", 0));

        assertEquals(EligibilityStatus.INELIGIBLE, r.status());
        assertEquals(List.of(
                "CGPA 7.20 is below the minimum requirement of 8.50",
                "Active backlogs 1 exceed the maximum allowed of 0"), r.reasons());
    }

    @Test
    void checkThrowsWhenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.check(99L, 1L));
    }

    @Test
    void checkThrowsWhenCriteriaMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "8.00", 0)));
        when(criteriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.check(1L, 99L));
    }

    @Test
    void checkAllReturnsOneResultPerCompany() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "8.00", 0)));
        when(criteriaRepository.findAll(any(Sort.class))).thenReturn(List.of(
                criteria(1L, "Google", "8.50", 0),
                criteria(2L, "TCS", "7.00", 1)));

        List<EligibilityResponse> l1 = service.checkAll(1L);

        assertEquals(2, l1.size());
        assertEquals(EligibilityStatus.INELIGIBLE, l1.get(0).status());
        assertEquals(EligibilityStatus.ELIGIBLE, l1.get(1).status());
    }

    private static Student student(Long id, String cgpa, int backlogs) {
        Student s = new Student();
        s.setId(id);
        s.setRollNumber("21MCA00" + id);
        s.setFullName("Test Student");
        s.setAcademicRecord(new AcademicRecord(new BigDecimal(cgpa), backlogs));
        return s;
    }

    private static PlacementCriteria criteria(Long id, String company, String minCgpa, int maxBacklogs) {
        PlacementCriteria c = new PlacementCriteria();
        c.setId(id);
        c.setCompanyName(company);
        c.setMinCgpa(new BigDecimal(minCgpa));
        c.setMaxActiveBacklogs(maxBacklogs);
        return c;
    }
}
