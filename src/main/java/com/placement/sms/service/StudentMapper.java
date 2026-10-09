package com.placement.sms.service;

import com.placement.sms.dto.StudentRequest;
import com.placement.sms.dto.StudentResponse;
import com.placement.sms.entity.AcademicRecord;
import com.placement.sms.entity.PlacementInfo;
import com.placement.sms.entity.PlacementStatus;
import com.placement.sms.entity.Student;
import java.util.Locale;

final class StudentMapper {

    private StudentMapper() {
    }

    static String normalizeRollNumber(String rollNumber) {
        return rollNumber.trim().toUpperCase(Locale.ROOT);
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static Student toEntity(StudentRequest request) {
        Student student = new Student();
        apply(student, request);
        return student;
    }

    static void apply(Student student, StudentRequest request) {
        student.setRollNumber(normalizeRollNumber(request.rollNumber()));
        student.setFullName(request.fullName().trim());
        student.setEmail(normalizeEmail(request.email()));
        student.setDepartment(request.department().trim());
        student.setAcademicRecord(new AcademicRecord(request.cgpa(), request.activeBacklogs()));

        if (request.placementStatus() == PlacementStatus.PLACED) {
            student.setPlacementInfo(new PlacementInfo(PlacementStatus.PLACED,
                    request.companyPlaced().trim(), request.packageLpa()));
        } else {
            student.setPlacementInfo(new PlacementInfo(PlacementStatus.NOT_PLACED, null, null));
        }
    }

    static StudentResponse toResponse(Student student) {
        AcademicRecord academic = student.getAcademicRecord();
        PlacementInfo placement = student.getPlacementInfo();
        return new StudentResponse(
                student.getId(),
                student.getRollNumber(),
                student.getFullName(),
                student.getEmail(),
                student.getDepartment(),
                academic.getCgpa(),
                academic.getActiveBacklogs(),
                placement.getStatus(),
                placement.getCompanyPlaced(),
                placement.getPackageLpa());
    }
}
