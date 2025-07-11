package com.phegon.FoodApp.review.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReviewDTO {

    private  Long id;
    private Long menuId;
    private Long orderId;


    private String userName;

    @NotNull(message = "Rating cannot be null")
    @Min(1)
    @Max(10)
    private Integer rating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;

    private String menuName;
    private LocalDateTime createdAt;
}
