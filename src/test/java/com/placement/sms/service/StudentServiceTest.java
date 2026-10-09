package com.placement.sms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.placement.sms.dto.StudentRequest;
import com.placement.sms.dto.StudentResponse;
import com.placement.sms.entity.AcademicRecord;
import com.placement.sms.entity.PlacementStatus;
import com.placement.sms.entity.Student;
import com.placement.sms.exception.DuplicateResourceException;
import com.placement.sms.exception.InvalidRequestException;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.repository.StudentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentServiceTest {

    private StudentRepository repository;
    private StudentService service;

    @BeforeEach
    void setUp() {
        repository = mock(StudentRepository.class);
        service = new StudentService(repository);
    }

    @Test
    void createNormalizesAndSavesStudent() {
        when(repository.existsByRollNumber("21MCA010")).thenReturn(false);
        when(repository.existsByEmail("new.student@example.com")).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        StudentResponse r = service.create(request(" 21mca010 ", "New.Student@Example.com", PlacementStatus.NOT_PLACED, null));

        assertEquals(10L, r.id());
        assertEquals("21MCA010", r.rollNumber());
        assertEquals("new.student@example.com", r.email());
        assertEquals(PlacementStatus.NOT_PLACED, r.placementStatus());
        assertNull(r.companyPlaced());
    }

    @Test
    void createRejectsDuplicateRollNumber() {
        when(repository.existsByRollNumber("21MCA010")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.create(request("21MCA010", "a@example.com", null, null)));
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByRollNumber("21MCA010")).thenReturn(false);
        when(repository.existsByEmail("a@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.create(request("21MCA010", "a@example.com", null, null)));
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void createRejectsPlacedStudentWithoutCompany() {
        assertThrows(InvalidRequestException.class,
                () -> service.create(request("21MCA010", "a@example.com", PlacementStatus.PLACED, " ")));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updateChangesFieldsOfExistingStudent() {
        Student existing = new Student();
        existing.setId(5L);
        existing.setRollNumber("21MCA005");
        existing.setAcademicRecord(new AcademicRecord(new BigDecimal("6.80"), 2));
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByRollNumberAndIdNot("21MCA005", 5L)).thenReturn(false);
        when(repository.existsByEmailAndIdNot("a@example.com", 5L)).thenReturn(false);
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse r = service.update(5L,
                new StudentRequest("21MCA005", "Updated Name", "a@example.com", "MCA",
                        new BigDecimal("7.40"), 0, PlacementStatus.PLACED, "Zoho", new BigDecimal("9.50")));

        assertEquals("Updated Name", r.fullName());
        assertEquals(new BigDecimal("7.40"), r.cgpa());
        assertEquals(0, r.activeBacklogs());
        assertEquals(PlacementStatus.PLACED, r.placementStatus());
        assertEquals("Zoho", r.companyPlaced());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteRemovesExistingStudent() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    private static StudentRequest request(String roll, String email, PlacementStatus status, String company) {
        return new StudentRequest(roll, "Test Student", email, "MCA",
                new BigDecimal("8.00"), 0, status, company, null);
    }
}
