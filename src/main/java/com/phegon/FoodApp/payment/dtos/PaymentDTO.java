package com.phegon.FoodApp.payment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegon.FoodApp.auth_users.dtos.UserDto;
import com.phegon.FoodApp.enums.PaymentGateway;
import com.phegon.FoodApp.enums.PaymentStatus;
import com.phegon.FoodApp.order.dtos.OrderDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Data for generating getters, setters, toString, equals, and hashCode methods
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Include this annotation to avoid null fields in JSON response
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unknown properties in JSON
public class PaymentDTO {
    private Long id; // Unique identifier for the payment

    private Long orderId; // ID of the associated order

    private BigDecimal amount; // Amount paid, represented as a String to handle currency formats

    private PaymentStatus paymentStatus; // Status of the payment (e.g., SUCCESS, FAILED)

    private String transactionId; // Unique identifier for the transaction

    private PaymentGateway paymentGateway; // Payment gateway used (e.g., PAYPAL, STRIPE)

    private String failureReason; // Reason for payment failure, if applicable

    private boolean success; // Indicates if the payment was successful

    private LocalDateTime paymentDate; // Date and time of the payment, represented as a String

    private OrderDTO order; // Associated order details
    private UserDto user; // User who made the payment
}
