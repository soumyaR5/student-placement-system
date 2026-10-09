package com.placement.sms.service;

import com.placement.sms.dto.StudentRequest;
import com.placement.sms.dto.StudentResponse;
import com.placement.sms.entity.PlacementStatus;
import com.placement.sms.entity.Student;
import com.placement.sms.exception.DuplicateResourceException;
import com.placement.sms.exception.InvalidRequestException;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.repository.StudentRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository repository) {
        studentRepository = repository;
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        validatePlacement(request);
        String rollNumber = StudentMapper.normalizeRollNumber(request.rollNumber());
        String email = StudentMapper.normalizeEmail(request.email());

        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new DuplicateResourceException("A student with roll number " + rollNumber + " already exists");
        }
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A student with email " + email + " already exists");
        }

        Student saved = studentRepository.save(StudentMapper.toEntity(request));
        return StudentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        List<StudentResponse> l1 = studentRepository.findAll(Sort.by("id")).stream()
                .map(StudentMapper::toResponse)
                .toList();
        return l1;
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return StudentMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student existing = findOrThrow(id);
        validatePlacement(request);
        String rollNumber = StudentMapper.normalizeRollNumber(request.rollNumber());
        String email = StudentMapper.normalizeEmail(request.email());

        if (studentRepository.existsByRollNumberAndIdNot(rollNumber, id)) {
            throw new DuplicateResourceException("Another student with roll number " + rollNumber + " already exists");
        }
        if (studentRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("Another student with email " + email + " already exists");
        }

        StudentMapper.apply(existing, request);
        Student saved = studentRepository.save(existing);
        return StudentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw notFound(id);
        }
        studentRepository.deleteById(id);
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Student not found with id " + id);
    }

    private static void validatePlacement(StudentRequest request) {
        if (request.placementStatus() == PlacementStatus.PLACED
                && (request.companyPlaced() == null || request.companyPlaced().isBlank())) {
            throw new InvalidRequestException("companyPlaced is required when placementStatus is PLACED");
        }
    }
}
