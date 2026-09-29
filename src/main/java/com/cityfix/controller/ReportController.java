package com.cityfix.controller;

import com.cityfix.dto.CreateReportRequest;
import com.cityfix.dto.UpdateStatusRequest;
import com.cityfix.dto.UpvoteRequest;
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

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Report createReport(@Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(request);
    }

    @GetMapping
    public List<Report> listReports(@RequestParam(required = false) String area,
                                    @RequestParam(required = false) ReportStatus status,
                                    @RequestParam(required = false) Category category,
                                    @RequestParam(defaultValue = "newest") String sort) {
        return reportService.findReports(area, status, category, sort);
    }

    @GetMapping("/stats")
    public Map<String, Map<String, Long>> getStats() {
        return reportService.getStats();
    }

    @GetMapping("/{id}")
    public Report getReport(@PathVariable Long id) {
        return reportService.findReportById(id);
    }

    @PatchMapping("/{id}/status")
    public Report updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return reportService.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/upvotes")
    @ResponseStatus(HttpStatus.CREATED)
    public Report upvoteReport(@PathVariable Long id, @Valid @RequestBody UpvoteRequest request) {
        return reportService.upvoteReport(id, request.voterEmail());
    }
}
