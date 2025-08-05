package com.phegon.FoodApp.review.services;

import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.auth_users.services.UserService;
import com.phegon.FoodApp.enums.OrderStatus;
import com.phegon.FoodApp.exceptions.BadRequestException;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.menu.entity.Menu;
import com.phegon.FoodApp.menu.repository.MenuRepository;
import com.phegon.FoodApp.order.entity.Order;
import com.phegon.FoodApp.order.repository.OrderItemRepository;
import com.phegon.FoodApp.order.repository.OrderRepository;
import com.phegon.FoodApp.response.Response;
import com.phegon.FoodApp.review.dtos.ReviewDTO;
import com.phegon.FoodApp.review.entity.Review;
import com.phegon.FoodApp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MenuRepository    menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    public Response<ReviewDTO> createReview(ReviewDTO reviewDTO) {
        log.info("Creating review for menu with ID: {}", reviewDTO.getMenuId());

        User user = userService.getCurrentLoggedInUser();

        // validate the required fields
        if (reviewDTO.getOrderId() == null || reviewDTO.getMenuId() == null) {
            throw  new BadRequestException("Menu ID and rating are required to create a review.");
        }

        // validate menu items exists
        Menu menu = menuRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Menu with ID " + reviewDTO.getOrderId() + " not found."));

        // validate order exists
        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order with ID " + reviewDTO.getOrderId() + " not found."));

        // make sure the order belongs to the user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only review your own orders.");
        }

        // validate order status is DELIVERED
        if (order.getOrderStatus() != OrderStatus.DELIVERED ){
            throw new BadRequestException("You can only review orders that have been delivered.");
        }

        // validate the that menu item is part of this order
        boolean itemInOrder = orderItemRepository.existsByOrderIdAndMenuId(
                reviewDTO.getOrderId(),
                reviewDTO.getMenuId()
        );

        if (!itemInOrder) {
            throw new BadRequestException("The menu item is not part of this order.");
        }

        // check if user already reviewed this menu item
        if (reviewRepository.existsByUserIdAndMenuIdAndOrderId(
                user.getId(),
                reviewDTO.getMenuId(),
                reviewDTO.getOrderId()
        )) {
            throw new BadRequestException("You have already reviewed this menu item.");
        }

        // create the review and save it
        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .orderId(reviewDTO.getOrderId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // return response with review data
        ReviewDTO responseReviewDTO = modelMapper.map(savedReview, ReviewDTO.class);
        responseReviewDTO.setUserName(user.getName());
        responseReviewDTO.setMenuName(menu.getName());

        return Response.<ReviewDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review created successfully.")
                .data(responseReviewDTO)
                .build();
    }

    @Override
    public Response<List<ReviewDTO>> getReviewsForMenu(Long restaurantId) {
        log.info("Fetching reviews for menu with ID: {}", restaurantId);

        List<Review> reviews = reviewRepository.findByMenuIdOrderByIdDesc(restaurantId);

        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .toList();

        return Response.<List<ReviewDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Reviews fetched successfully.")
                .data(reviewDTOs)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {
        log.info("Calculating average rating for menu with ID: {}", menuId);
        Double averageRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Average rating calculated successfully.")
                .data(averageRating != null ? averageRating : 0.0)
                .build();
    }
}
