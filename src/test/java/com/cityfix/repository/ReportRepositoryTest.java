package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void findReportsByArea() {
        reportRepository.save(createReport(
                "Streetlight issue", "Riverside",
                ReportStatus.REPORTED, Category.STREETLIGHT));

        reportRepository.save(createReport(
                "Pothole issue", "Downtown",
                ReportStatus.REPORTED, Category.POTHOLE));

        List<Report> results = reportRepository.findFilteredReports(
                "riverside", null, null, Sort.unsorted());

        assertThat(results)
                .extracting(Report::getTitle)
                .containsExactly("Streetlight issue");
    }

    @Test
    void findReportsByStatus() {
        reportRepository.save(createReport(
                "Reported issue", "Riverside",
                ReportStatus.REPORTED, Category.POTHOLE));

        reportRepository.save(createReport(
                "Fixed issue", "Downtown",
                ReportStatus.FIXED, Category.POTHOLE));

        List<Report> results = reportRepository.findFilteredReports(
                null, ReportStatus.REPORTED, null, Sort.unsorted());

        assertThat(results)
                .extracting(Report::getTitle)
                .containsExactly("Reported issue");
    }

    @Test
    void findReportsByCategory() {
        reportRepository.save(createReport(
                "Streetlight issue", "Riverside",
                ReportStatus.REPORTED, Category.STREETLIGHT));

        reportRepository.save(createReport(
                "Pothole issue", "Downtown",
                ReportStatus.REPORTED, Category.POTHOLE));

        List<Report> results = reportRepository.findFilteredReports(
                null, null, Category.STREETLIGHT, Sort.unsorted());

        assertThat(results)
                .extracting(Report::getTitle)
                .containsExactly("Streetlight issue");
    }

    @Test
    void findReportsUsingCombinedFilters() {
        reportRepository.save(createReport(
                "Matching report", "Riverside",
                ReportStatus.REPORTED, Category.STREETLIGHT));

        reportRepository.save(createReport(
                "Wrong status", "Riverside",
                ReportStatus.FIXED, Category.STREETLIGHT));

        reportRepository.save(createReport(
                "Wrong area", "Downtown",
                ReportStatus.REPORTED, Category.STREETLIGHT));

        List<Report> results = reportRepository.findFilteredReports(
                "Riverside",
                ReportStatus.REPORTED,
                null,
                Sort.unsorted());

        assertThat(results)
                .extracting(Report::getTitle)
                .containsExactly("Matching report");
    }

    private Report createReport(
            String title,
            String area,
            ReportStatus status,
            Category category) {

        Report report = new Report();
        report.setTitle(title);
        report.setDescription("Test description");
        report.setCategory(category);
        report.setArea(area);
        report.setAddress("Test Road");
        report.setStatus(status);
        report.setReporterName("Test User");
        report.setReporterEmail("test@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        return report;
    }
}
