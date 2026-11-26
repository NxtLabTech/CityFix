package com.cityfix.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    void saveAndFindReport() {
        Report report = createReport(
                "Broken streetlight",
                Category.STREETLIGHT,
                ReportStatus.REPORTED
        );
        report.setDescription("The light on Park Road is out");
        report.setArea("Riverside");
        report.setAddress("Park Road");
        report.setReporterName("Ana");
        report.setReporterEmail("ana@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);

        Report foundReport = reportRepository.findById(
                savedReport.getId()
        ).orElseThrow();

        assertThat(foundReport.getTitle()).isEqualTo("Broken streetlight");
        assertThat(foundReport.getCategory()).isEqualTo(Category.STREETLIGHT);
        assertThat(foundReport.getUpvoteCount()).isZero();
    }

    @Test
    void countByStatusReturnsCorrectCount() {
        reportRepository.saveAll(List.of(
                createReport("Report 1", Category.STREETLIGHT,
                        ReportStatus.REPORTED),
                createReport("Report 2", Category.POTHOLE,
                        ReportStatus.REPORTED),
                createReport("Report 3", Category.GARBAGE,
                        ReportStatus.IN_PROGRESS),
                createReport("Report 4", Category.GRAFFITI,
                        ReportStatus.FIXED)
        ));

        assertThat(reportRepository.countByStatus(
                ReportStatus.REPORTED
        )).isEqualTo(2);

        assertThat(reportRepository.countByStatus(
                ReportStatus.IN_PROGRESS
        )).isEqualTo(1);

        assertThat(reportRepository.countByStatus(
                ReportStatus.FIXED
        )).isEqualTo(1);
    }

    @Test
    void countByCategoryReturnsCorrectCount() {
        reportRepository.saveAll(List.of(
                createReport("Report 1", Category.STREETLIGHT,
                        ReportStatus.REPORTED),
                createReport("Report 2", Category.STREETLIGHT,
                        ReportStatus.FIXED),
                createReport("Report 3", Category.POTHOLE,
                        ReportStatus.IN_PROGRESS),
                createReport("Report 4", Category.GARBAGE,
                        ReportStatus.REPORTED)
        ));

        assertThat(reportRepository.countByCategory(
                Category.STREETLIGHT
        )).isEqualTo(2);

        assertThat(reportRepository.countByCategory(
                Category.POTHOLE
        )).isEqualTo(1);

        assertThat(reportRepository.countByCategory(
                Category.GARBAGE
        )).isEqualTo(1);
    }

    private Report createReport(
            String title,
            Category category,
            ReportStatus status
    ) {
        Report report = new Report();
        report.setTitle(title);
        report.setDescription("Test report description");
        report.setCategory(category);
        report.setArea("Riverside");
        report.setAddress("Park Road");
        report.setStatus(status);
        report.setReporterName("Jahnavi");
        report.setReporterEmail("jahnavi@example.com");

        return report;
    }
}
