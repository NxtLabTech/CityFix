package com.cityfix.controller;

import com.cityfix.dto.CreateReportRequest;
import com.cityfix.exception.ReportNotFoundException;
import com.cityfix.mapper.ReportMapper;
import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(ReportMapper.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void createReportReturns201() throws Exception {
        Report report = new Report();
        report.setId(1L);
        report.setTitle("Pothole");
        report.setCategory(Category.POTHOLE);
        report.setStatus(ReportStatus.REPORTED);
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(report);

        String json = """
                {
                  "title": "Pothole",
                  "description": "Deep hole in the road",
                  "category": "POTHOLE",
                  "area": "Downtown",
                  "address": "Main St near no. 42",
                  "reporterName": "Ana",
                  "reporterEmail": "ana@example.com"
                }
                """;

        mockMvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REPORTED"));
    }

    @Test
    void getMissingReportReturns404() throws Exception {
        when(reportService.findReportById(99L)).thenThrow(new ReportNotFoundException(99L));

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Report with id 99 not found"));
    }

    @Test
    void createReportWithoutTitleReturns400() throws Exception {
        String json = """
                {
                  "description": "Deep hole in the road",
                  "category": "POTHOLE",
                  "area": "Downtown",
                  "reporterName": "Ana",
                  "reporterEmail": "ana@example.com"
                }
                """;

        mockMvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Nested
    class PrivacyTests {

        private Report mockReport;

        @BeforeEach
        void setUp() {
            mockReport = new Report();
            mockReport.setId(1L);
            mockReport.setTitle("Pothole");
            mockReport.setReporterName("Ana");
            mockReport.setReporterEmail("ana@example.com");
        }

        @Test
        void createReportShouldNotReturnEmail() throws Exception {
            when(reportService.createReport(any())).thenReturn(mockReport);

            String json = """
                {
                  "title": "Pothole",
                  "description": "Deep hole in the road",
                  "category": "POTHOLE",
                  "area": "Downtown",
                  "address": "Main St",
                  "reporterName": "Ana",
                  "reporterEmail": "ana@example.com"
                }
                """;

            mockMvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reporterName").value("Ana"))
                    .andExpect(jsonPath("$.reporterEmail").doesNotExist());
        }

        @Test
        void getReportByIdShouldNotReturnEmail() throws Exception {
            when(reportService.findReportById(1L)).thenReturn(mockReport);

            mockMvc.perform(get("/api/reports/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reporterName").value("Ana"))
                    .andExpect(jsonPath("$.reporterEmail").doesNotExist());
        }

        @Test
        void listReportsShouldNotReturnEmail() throws Exception {
            when(reportService.findReports(any(), any(), any(), any())).thenReturn(List.of(mockReport));

            mockMvc.perform(get("/api/reports"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].reporterName").value("Ana"))
                    .andExpect(jsonPath("$[0].reporterEmail").doesNotExist());
        }

        @Test
        void updateStatusShouldNotReturnEmail() throws Exception {
            when(reportService.updateStatus(anyLong(), any())).thenReturn(mockReport);

            String json = """
                {
                  "status": "FIXED"
                }
                """;

            mockMvc.perform(patch("/api/reports/1/status").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reporterEmail").doesNotExist());
        }

        @Test
        void upvoteReportShouldNotReturnEmail() throws Exception {
            when(reportService.upvoteReport(anyLong(), anyString())).thenReturn(mockReport);

            String json = """
                {
                  "voterEmail": "voter@example.com"
                }
                """;

            mockMvc.perform(post("/api/reports/1/upvotes").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reporterEmail").doesNotExist());
        }
    }
}
