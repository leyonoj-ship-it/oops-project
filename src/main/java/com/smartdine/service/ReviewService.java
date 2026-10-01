package com.smartdine.service;

import com.smartdine.dto.ReviewRequest;
import com.smartdine.model.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(ReviewRequest request);
    List<Review> getReviewsByEstablishment(Long establishmentId);
    List<Review> getFlaggedReviews();
    void flagReview(Long reviewId);
    void unflagReview(Long reviewId);
    void deleteReview(Long reviewId);
}
