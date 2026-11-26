
package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.model.Upvote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UpvoteRepositoryTest {

    @Autowired
    private UpvoteRepository upvoteRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    void existsByReportIdAndVoterEmailReturnsTrueWhenUpvoteExists() {
        Report report = reportRepository.saveAndFlush(createReport());

        upvoteRepository.saveAndFlush(
                createUpvote(report, "jahnavi@example.com")
        );

        boolean exists = upvoteRepository.existsByReportIdAndVoterEmail(
                report.getId(), "jahnavi@example.com"
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByReportIdAndVoterEmailReturnsFalseWhenEmailDoesNotMatch() {
        Report report = reportRepository.saveAndFlush(createReport());

        upvoteRepository.saveAndFlush(
                createUpvote(report, "jahnavi@example.com")
        );

        boolean exists = upvoteRepository.existsByReportIdAndVoterEmail(
                report.getId(), "another@example.com"
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsByReportIdAndVoterEmailReturnsFalseForDifferentReport() {
        Report firstReport = reportRepository.saveAndFlush(createReport());
        Report secondReport = reportRepository.saveAndFlush(createReport());

        upvoteRepository.saveAndFlush(
                createUpvote(firstReport, "jahnavi@example.com")
        );

        boolean exists = upvoteRepository.existsByReportIdAndVoterEmail(
                secondReport.getId(), "jahnavi@example.com"
        );

        assertThat(exists).isFalse();
    }

    @Test
    void savingDuplicateUpvoteForSameReportAndEmailThrowsException() {
        Report report = reportRepository.saveAndFlush(createReport());

        upvoteRepository.saveAndFlush(
                createUpvote(report, "jahnavi@example.com")
        );

        Upvote duplicateUpvote = createUpvote(
                report, "jahnavi@example.com"
        );

        assertThatThrownBy(
                () -> upvoteRepository.saveAndFlush(duplicateUpvote)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Report createReport() {
        Report report = new Report();
        report.setTitle("Broken streetlight");
        report.setDescription("Streetlight is not working");
        report.setCategory(Category.STREETLIGHT);
        report.setArea("Riverside");
        report.setAddress("Park Road");
        report.setStatus(ReportStatus.REPORTED);
        report.setReporterName("Jahnavi");
        report.setReporterEmail("reporter@example.com");

        return report;
    }

    private Upvote createUpvote(Report report, String email) {
        Upvote upvote = new Upvote();
        upvote.setReport(report);
        upvote.setVoterEmail(email);

        return upvote;
    }
}
