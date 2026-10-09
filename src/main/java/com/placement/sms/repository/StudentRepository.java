package com.placement.sms.repository;

import com.placement.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);

    boolean existsByEmail(String email);

    boolean existsByRollNumberAndIdNot(String rollNumber, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
