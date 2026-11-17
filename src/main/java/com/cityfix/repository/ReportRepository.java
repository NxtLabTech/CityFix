package com.cityfix.repository;

import com.cityfix.model.Category;
import com.cityfix.model.Report;
import com.cityfix.model.ReportStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
            SELECT r
            FROM Report r
            WHERE (:area IS NULL OR LOWER(r.area) = LOWER(:area))
              AND (:status IS NULL OR r.status = :status)
              AND (:category IS NULL OR r.category = :category)
            """)
    List<Report> findFilteredReports(
            @Param("area") String area,
            @Param("status") ReportStatus status,
            @Param("category") Category category,
            Sort sort);

    long countByStatus(ReportStatus status);

    long countByCategory(Category category);
}
