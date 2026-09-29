package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    void saveAndFindReport() {
        Report report = new Report();
        report.setTitle("Broken streetlight");
        report.setDescription("The light on Park Road is out");
        report.setCategory(Category.STREETLIGHT);
        report.setArea("Riverside");
        report.setAddress("Park Road");
        report.setStatus(ReportStatus.REPORTED);
        report.setReporterName("Ana");
        report.setReporterEmail("ana@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        Report savedReport = reportRepository.save(report);

        Report foundReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        assertThat(foundReport.getTitle()).isEqualTo("Broken streetlight");
        assertThat(foundReport.getCategory()).isEqualTo(Category.STREETLIGHT);
        assertThat(foundReport.getUpvoteCount()).isZero();
    }

    @Test
    void rejectsReportWithNullTitle() {
        Report report = new Report();
        report.setDescription("The light on Park Road is out");
        report.setCategory(Category.STREETLIGHT);
        report.setArea("Riverside");
        report.setStatus(ReportStatus.REPORTED);
        report.setReporterName("Ana");
        report.setReporterEmail("ana@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> reportRepository.saveAndFlush(report)
        );
    }
}
