package com.placement.sms.config;

import com.placement.sms.entity.AcademicRecord;
import com.placement.sms.entity.PlacementCriteria;
import com.placement.sms.entity.PlacementInfo;
import com.placement.sms.entity.PlacementStatus;
import com.placement.sms.entity.Student;
import com.placement.sms.repository.PlacementCriteriaRepository;
import com.placement.sms.repository.StudentRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Inserts sample data on startup, only into empty tables.
 * Disable with APP_SEED_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StudentRepository studentRepository;
    private final PlacementCriteriaRepository criteriaRepository;

    public DataSeeder(StudentRepository students, PlacementCriteriaRepository criteria) {
        studentRepository = students;
        criteriaRepository = criteria;
    }

    @Override
    public void run(String... args) {
        if (criteriaRepository.count() == 0) {
            List<PlacementCriteria> l1 = List.of(
                    criteria("Google", "8.50", 0),
                    criteria("Microsoft", "8.00", 0),
                    criteria("TCS", "7.00", 1),
                    criteria("Infosys", "6.50", 2),
                    criteria("Zoho", "7.50", 0));
            criteriaRepository.saveAll(l1);
            log.info("Seeded {} placement criteria", l1.size());
        }

        if (studentRepository.count() == 0) {
            List<Student> l1 = List.of(
                    student("21MCA001", "Aarav Sharma", "aarav.sharma@example.com", "8.75", 0, null, null),
                    student("21MCA002", "Priya Nair", "priya.nair@example.com", "9.10", 0, "TCS", "7.00"),
                    student("21MCA003", "Rohan Mehta", "rohan.mehta@example.com", "7.20", 1, null, null),
                    student("21MCA004", "Sneha Iyer", "sneha.iyer@example.com", "8.05", 0, null, null),
                    student("21MCA005", "Karthik Reddy", "karthik.reddy@example.com", "6.80", 2, null, null),
                    student("21MCA006", "Divya Menon", "divya.menon@example.com", "7.95", 0, null, null),
                    student("21MCA007", "Arjun Patel", "arjun.patel@example.com", "8.40", 1, null, null),
                    student("21MCA008", "Meera Krishnan", "meera.krishnan@example.com", "9.35", 0, "Zoho", "12.00"));
            studentRepository.saveAll(l1);
            log.info("Seeded {} students", l1.size());
        }
    }

    private static PlacementCriteria criteria(String company, String minCgpa, int maxBacklogs) {
        PlacementCriteria c = new PlacementCriteria();
        c.setCompanyName(company);
        c.setMinCgpa(new BigDecimal(minCgpa));
        c.setMaxActiveBacklogs(maxBacklogs);
        return c;
    }

    private static Student student(String roll, String name, String email, String cgpa, int backlogs,
                                   String company, String packageLpa) {
        Student s = new Student();
        s.setRollNumber(roll);
        s.setFullName(name);
        s.setEmail(email);
        s.setDepartment("MCA");
        s.setAcademicRecord(new AcademicRecord(new BigDecimal(cgpa), backlogs));
        if (company == null) {
            s.setPlacementInfo(new PlacementInfo(PlacementStatus.NOT_PLACED, null, null));
        } else {
            s.setPlacementInfo(new PlacementInfo(PlacementStatus.PLACED, company, new BigDecimal(packageLpa)));
        }
        return s;
    }
}
