package com.cityfix.service;

import com.cityfix.dto.CreateReportRequest;
import com.cityfix.exception.DuplicateUpvoteException;
import com.cityfix.exception.ReportNotFoundException;
import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.model.Upvote;
import com.cityfix.repository.ReportRepository;
import com.cityfix.repository.UpvoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UpvoteRepository upvoteRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, upvoteRepository);
    }

    @Test
    void createReportStartsAsReportedWithZeroVotes() {
        CreateReportRequest request = new CreateReportRequest("Pothole", "Deep hole in the road",
                Category.POTHOLE, "Downtown", "Main St near no. 42", "Ana", "ana@example.com");
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report report = reportService.createReport(request);

        assertThat(report.getTitle()).isEqualTo("Pothole");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REPORTED);
        assertThat(report.getUpvoteCount()).isZero();
        assertThat(report.getCreatedAt()).isNotNull();
        assertThat(report.getUpdatedAt()).isNotNull();
    }

    @Test
    void findReportByIdThrowsWhenReportDoesNotExist() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.findReportById(99L))
                .isInstanceOf(ReportNotFoundException.class)
                .hasMessage("Report with id 99 not found");
    }

    @Test
    void upvoteReportIncreasesUpvoteCount() {
        Report report = new Report();
        report.setId(1L);
        report.setUpvoteCount(2);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(upvoteRepository.existsByReportIdAndVoterEmail(1L, "ben@example.com")).thenReturn(false);
        when(reportRepository.save(report)).thenReturn(report);

        Report result = reportService.upvoteReport(1L, "ben@example.com");

        assertThat(result.getUpvoteCount()).isEqualTo(3);
        verify(upvoteRepository).save(any(Upvote.class));
    }

    @Test
    void upvoteReportThrowsWhenEmailAlreadyVoted() {
        Report report = new Report();
        report.setId(1L);
        report.setUpvoteCount(2);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(upvoteRepository.existsByReportIdAndVoterEmail(1L, "ben@example.com")).thenReturn(true);

        assertThatThrownBy(() -> reportService.upvoteReport(1L, "ben@example.com"))
                .isInstanceOf(DuplicateUpvoteException.class);

        assertThat(report.getUpvoteCount()).isEqualTo(2);
        verify(upvoteRepository, never()).save(any(Upvote.class));
    }

    @Test
    void getStatsUsesTwoGroupByQueriesAndPreservesZeroCountsAndEnumOrder() {
        List<Object[]> statusRows = List.of(
                new Object[]{ReportStatus.REPORTED, 5L},
                new Object[]{ReportStatus.FIXED, 2L}
        );
        List<Object[]> categoryRows = List.of(
                new Object[]{Category.POTHOLE, 4L},
                new Object[]{Category.WATER_LEAK, 3L}
        );

        when(reportRepository.countGroupedByStatus()).thenReturn(statusRows);
        when(reportRepository.countGroupedByCategory()).thenReturn(categoryRows);

        Map<String, Map<String, Long>> stats = reportService.getStats();

        // Acceptance Criteria: Two GROUP BY queries replace the repeated status/category count queries
        verify(reportRepository).countGroupedByStatus();
        verify(reportRepository).countGroupedByCategory();
        verify(reportRepository, never()).countByStatus(any());
        verify(reportRepository, never()).countByCategory(any());

        // Acceptance Criteria: Zero-count statuses/categories are still returned in enum order
        Map<String, Long> byStatus = stats.get("byStatus");
        assertThat(byStatus.keySet()).containsExactly(
                ReportStatus.REPORTED.name(),
                ReportStatus.IN_PROGRESS.name(),
                ReportStatus.FIXED.name(),
                ReportStatus.REJECTED.name()
        );
        assertThat(byStatus.get("REPORTED")).isEqualTo(5L);
        assertThat(byStatus.get("IN_PROGRESS")).isZero();
        assertThat(byStatus.get("FIXED")).isEqualTo(2L);
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
        assertThat(byCategory.get("POTHOLE")).isEqualTo(4L);
        assertThat(byCategory.get("STREETLIGHT")).isZero();
        assertThat(byCategory.get("GARBAGE")).isZero();
        assertThat(byCategory.get("GRAFFITI")).isZero();
        assertThat(byCategory.get("WATER_LEAK")).isEqualTo(3L);
        assertThat(byCategory.get("OTHER")).isZero();
    }
}
