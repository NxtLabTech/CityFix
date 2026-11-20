package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    long countByStatus(ReportStatus status);

    long countByCategory(Category category);

    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT r.category, COUNT(r) FROM Report r GROUP BY r.category")
    List<Object[]> countGroupedByCategory();
}
