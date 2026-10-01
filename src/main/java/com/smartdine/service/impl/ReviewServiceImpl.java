package com.smartdine.service.impl;

import com.smartdine.dto.ReviewRequest;
import com.smartdine.exception.EstablishmentNotFoundException;
import com.smartdine.exception.UserNotFoundException;
import com.smartdine.model.Customer;
import com.smartdine.model.Establishment;
import com.smartdine.model.Order;
import com.smartdine.model.Review;
import com.smartdine.repository.CustomerRepository;
import com.smartdine.repository.EstablishmentRepository;
import com.smartdine.repository.OrderRepository;
import com.smartdine.repository.ReviewRepository;
import com.smartdine.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final OrderRepository orderRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             CustomerRepository customerRepository,
                             EstablishmentRepository establishmentRepository,
                             OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.establishmentRepository = establishmentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Review addReview(ReviewRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new UserNotFoundException(request.getCustomerId()));

        Establishment establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(request.getEstablishmentId()));

        Review review = new Review(customer, establishment, request.getRating(), request.getComment());

        if (request.getOrderId() != null) {
            orderRepository.findById(request.getOrderId()).ifPresent(review::setOrder);
        }

        Review saved = reviewRepository.save(review);

        // Dynamically update establishment average rating
        Double avgRating = reviewRepository.calculateAverageRating(establishment.getEstablishmentId());
        long count = reviewRepository.countByEstablishment_EstablishmentId(establishment.getEstablishmentId());
        establishment.setRating(Math.round(avgRating * 10.0) / 10.0);
        establishment.setReviewCount((int) count);
        establishmentRepository.save(establishment);

        return saved;
    }

    @Override
    public List<Review> getReviewsByEstablishment(Long establishmentId) {
        return reviewRepository.findByEstablishment_EstablishmentIdOrderByCreatedAtDesc(establishmentId);
    }

    @Override
    public List<Review> getFlaggedReviews() {
        return reviewRepository.findByIsFlaggedTrueOrderByCreatedAtDesc();
    }

    @Override
    public void flagReview(Long reviewId) {
        reviewRepository.findById(reviewId).ifPresent(r -> {
            r.setIsFlagged(true);
            reviewRepository.save(r);
        });
    }

    @Override
    public void unflagReview(Long reviewId) {
        reviewRepository.findById(reviewId).ifPresent(r -> {
            r.setIsFlagged(false);
            reviewRepository.save(r);
        });
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.findById(reviewId).ifPresent(r -> {
            Establishment est = r.getEstablishment();
            reviewRepository.delete(r);
            if (est != null) {
                Double avg = reviewRepository.calculateAverageRating(est.getEstablishmentId());
                long count = reviewRepository.countByEstablishment_EstablishmentId(est.getEstablishmentId());
                est.setRating(Math.round(avg * 10.0) / 10.0);
                est.setReviewCount((int) count);
                establishmentRepository.save(est);
            }
        });
    }
}
