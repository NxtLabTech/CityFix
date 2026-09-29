package com.cityfix.controller;

import com.cityfix.dto.CreateReportRequest;
import com.cityfix.dto.UpdateStatusRequest;
import com.cityfix.dto.UpvoteRequest;
import com.cityfix.dto.ReportResponse;
import com.cityfix.mapper.ReportMapper;
import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import com.cityfix.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportMapper mapper;
    private final ReportService reportService;

    public ReportController(ReportMapper mapper, ReportService reportService) {
        this.mapper = mapper;
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(@Valid @RequestBody CreateReportRequest request) {
        Report report = reportService.createReport(request);
        return mapper.toDTO(report);
    }

    @GetMapping
    public List<ReportResponse> listReports(@RequestParam(required = false) String area,
                                    @RequestParam(required = false) ReportStatus status,
                                    @RequestParam(required = false) Category category,
                                    @RequestParam(defaultValue = "newest") String sort) {
        List<Report> reports = reportService.findReports(area, status, category, sort);

        return mapper.toDTO(reports);
    }

    @GetMapping("/stats")
    public Map<String, Map<String, Long>> getStats() {
        return reportService.getStats();
    }

    @GetMapping("/{id}")
    public ReportResponse getReport(@PathVariable Long id) {
        Report report = reportService.findReportById(id);

        return mapper.toDTO(report);
    }

    @PatchMapping("/{id}/status")
    public ReportResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Report report = reportService.updateStatus(id, request.status());

        return mapper.toDTO(report);
    }

    @PostMapping("/{id}/upvotes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse upvoteReport(@PathVariable Long id, @Valid @RequestBody UpvoteRequest request) {
        Report report = reportService.upvoteReport(id, request.voterEmail());

        return mapper.toDTO(report);
    }
}
