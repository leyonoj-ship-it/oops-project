package com.smartdine.controller;

import com.smartdine.dto.ReviewRequest;
import com.smartdine.model.Review;
import com.smartdine.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(originPatterns = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.addReview(request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<Review>> getReviewsByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(reviewService.getReviewsByEstablishment(establishmentId));
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<Review>> getFlaggedReviews() {
        return ResponseEntity.ok(reviewService.getFlaggedReviews());
    }

    @PostMapping("/{id}/flag")
    public ResponseEntity<Void> flagReview(@PathVariable Long id) {
        reviewService.flagReview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unflag")
    public ResponseEntity<Void> unflagReview(@PathVariable Long id) {
        reviewService.unflagReview(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
