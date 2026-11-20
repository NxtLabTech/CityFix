package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.service.ReportService;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ReportService.class)
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private EntityManager entityManager;

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
    void countGroupedByStatusAndCategoryQueriesWorkCorrectly() {
        Report r1 = createReport("Pothole 1", Category.POTHOLE, ReportStatus.REPORTED, "Downtown");
        Report r2 = createReport("Pothole 2", Category.POTHOLE, ReportStatus.REPORTED, "Downtown");
        Report r3 = createReport("Streetlight 1", Category.STREETLIGHT, ReportStatus.FIXED, "Riverside");
        reportRepository.saveAll(List.of(r1, r2, r3));

        List<Object[]> statusCounts = reportRepository.countGroupedByStatus();
        List<Object[]> categoryCounts = reportRepository.countGroupedByCategory();

        assertThat(statusCounts).isNotEmpty();
        assertThat(categoryCounts).isNotEmpty();
    }

    @Test
    void getStatsExecutesExactlyTwoQueriesAndDemonstratesReductionFromTen() {
        Report r1 = createReport("Pothole 1", Category.POTHOLE, ReportStatus.REPORTED, "Downtown");
        Report r2 = createReport("Pothole 2", Category.POTHOLE, ReportStatus.REPORTED, "Downtown");
        Report r3 = createReport("Streetlight 1", Category.STREETLIGHT, ReportStatus.FIXED, "Riverside");
        reportRepository.saveAll(List.of(r1, r2, r3));

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);

        // Baseline: demonstrate previous implementation's SQL statement count
        // (1 count query per status + 1 count query per category = 4 + 6 = 10 queries)
        statistics.clear();
        for (ReportStatus status : ReportStatus.values()) {
            reportRepository.countByStatus(status);
        }
        for (Category category : Category.values()) {
            reportRepository.countByCategory(category);
        }
        long baselineStatementCount = statistics.getPrepareStatementCount();
        assertThat(baselineStatementCount).isEqualTo(10L);

        // Optimized: demonstrate getStats() with two GROUP BY queries
        statistics.clear();
        Map<String, Map<String, Long>> stats = reportService.getStats();
        long optimizedStatementCount = statistics.getPrepareStatementCount();

        // Acceptance Criteria: Two GROUP BY queries replace the repeated queries
        assertThat(optimizedStatementCount).isEqualTo(2L);
        assertThat(optimizedStatementCount).isLessThan(baselineStatementCount);

        // Acceptance Criteria: Zero-count statuses/categories are still returned in enum order
        Map<String, Long> byStatus = stats.get("byStatus");
        assertThat(byStatus.keySet()).containsExactly(
                ReportStatus.REPORTED.name(),
                ReportStatus.IN_PROGRESS.name(),
                ReportStatus.FIXED.name(),
                ReportStatus.REJECTED.name()
        );
        assertThat(byStatus.get("REPORTED")).isEqualTo(2L);
        assertThat(byStatus.get("IN_PROGRESS")).isZero();
        assertThat(byStatus.get("FIXED")).isEqualTo(1L);
        assertThat(byStatus.get("REJECTED")).isZero();

        Map<String, Long> byCategory = stats.get("byCategory");
        assertThat(byCategory.keySet()).containsExactly(
                Category.POTHOLE.name(),
                Category.STREETLIGHT.name(),
                Category.GARBAGE.name(),
                Category.GRAFFITI.name(),
                Category.WATER_LEAK.name(),
                Category.OTHER.name()
        );
        assertThat(byCategory.get("POTHOLE")).isEqualTo(2L);
        assertThat(byCategory.get("STREETLIGHT")).isEqualTo(1L);
        assertThat(byCategory.get("GARBAGE")).isZero();
        assertThat(byCategory.get("GRAFFITI")).isZero();
        assertThat(byCategory.get("WATER_LEAK")).isZero();
        assertThat(byCategory.get("OTHER")).isZero();
    }

    private Report createReport(String title, Category category, ReportStatus status, String area) {
        Report report = new Report();
        report.setTitle(title);
        report.setDescription("Description for " + title);
        report.setCategory(category);
        report.setArea(area);
        report.setAddress("Sample Address");
        report.setStatus(status);
        report.setReporterName("Ana");
        report.setReporterEmail("ana@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        return report;
    }
}
