package com.placement.sms.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.placement.sms.dto.StudentRequest;
import com.placement.sms.dto.StudentResponse;
import com.placement.sms.entity.PlacementStatus;
import com.placement.sms.exception.ResourceNotFoundException;
import com.placement.sms.service.StudentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void createReturns201WithLocationForValidRequest() throws Exception {
        StudentResponse response = new StudentResponse(10L, "21MCA010", "Test User", "test.user@example.com",
                "MCA", new BigDecimal("8.25"), 0, PlacementStatus.NOT_PLACED, null, null);
        when(studentService.create(any(StudentRequest.class))).thenReturn(response);

        String body = """
                {"rollNumber":"21MCA010","fullName":"Test User","email":"test.user@example.com",
                 "department":"MCA","cgpa":8.25,"activeBacklogs":0}
                """;

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.rollNumber").value("21MCA010"));
    }

    @Test
    void createReturns400WithFieldErrorsForInvalidRequest() throws Exception {
        String body = """
                {"rollNumber":"","fullName":"","email":"not-an-email",
                 "department":"MCA","cgpa":11.5,"activeBacklogs":-1}
                """;

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.rollNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.cgpa").exists())
                .andExpect(jsonPath("$.fieldErrors.activeBacklogs").exists());
    }

    @Test
    void getByIdReturns404WhenStudentMissing() throws Exception {
        when(studentService.getById(99L)).thenThrow(new ResourceNotFoundException("Student not found with id 99"));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found with id 99"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/students/1")).andExpect(status().isNoContent());
    }
}
