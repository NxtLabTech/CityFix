package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.model.Upvote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UpvoteRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UpvoteRepository upvoteRepository;

    @Test
    void rejectsUpvoteWithNullVoterEmail() {
        Report report = new Report();
        report.setTitle("Broken streetlight");
        report.setDescription("The light on Park Road is out");
        report.setCategory(Category.STREETLIGHT);
        report.setArea("Riverside");
        report.setStatus(ReportStatus.REPORTED);
        report.setReporterName("Ana");
        report.setReporterEmail("ana@example.com");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.saveAndFlush(report);

        Upvote upvote = new Upvote();
        upvote.setReport(savedReport);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> upvoteRepository.saveAndFlush(upvote)
        );
    }
}