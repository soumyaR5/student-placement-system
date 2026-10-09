package com.placement.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roll_number", nullable = false, unique = true, length = 30)
    private String rollNumber;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Embedded
    private AcademicRecord academicRecord = new AcademicRecord();

    @Embedded
    private PlacementInfo placementInfo = new PlacementInfo();

    public Student() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String value) {
        rollNumber = value;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String value) {
        fullName = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String value) {
        department = value;
    }

    public AcademicRecord getAcademicRecord() {
        return academicRecord;
    }

    public void setAcademicRecord(AcademicRecord value) {
        academicRecord = value;
    }

    public PlacementInfo getPlacementInfo() {
        return placementInfo;
    }

    public void setPlacementInfo(PlacementInfo value) {
        placementInfo = value;
    }
}
