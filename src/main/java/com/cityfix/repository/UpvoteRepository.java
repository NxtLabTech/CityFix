package com.cityfix.repository;

import com.cityfix.model.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpvoteRepository extends JpaRepository<Upvote, Long> {

    boolean existsByReportIdAndVoterEmail(Long reportId, String voterEmail);
}
