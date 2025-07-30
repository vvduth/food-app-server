package com.phegon.FoodApp.payment.services;


import com.phegon.FoodApp.email_notification.dtos.NotificationDTO;
import com.phegon.FoodApp.email_notification.services.NotificationService;
import com.phegon.FoodApp.enums.OrderStatus;
import com.phegon.FoodApp.enums.PaymentGateway;
import com.phegon.FoodApp.enums.PaymentStatus;
import com.phegon.FoodApp.exceptions.BadRequestException;
import com.phegon.FoodApp.order.entity.Order;
import com.phegon.FoodApp.order.repository.OrderRepository;
import com.phegon.FoodApp.payment.dtos.PaymentDTO;
import com.phegon.FoodApp.payment.entity.Payment;
import com.phegon.FoodApp.payment.repository.PaymentRepository;
import com.phegon.FoodApp.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final ModelMapper modelMapper;

    @Value(("${stripe.api.secret.key}"))
    private String secretKey;

    @Value(("${frontend.base.url}"))
    private String frontendBaseUrl;



    @Override
    public Response<?> initiatePayment(PaymentDTO paymentRequest) {
        log.info("Initiating payment for order: {}", paymentRequest.getOrderId());
        Stripe.apiKey = secretKey;

        Long orderId = paymentRequest.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already completed for order ID: " + orderId);
        }
        if (paymentRequest.getAmount() == null ){
            throw new BadRequestException("Payment amount cannot be null for order ID: " + orderId);
        }
        if (order.getTotalAmount().compareTo(paymentRequest.getAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match order total for order ID: " + orderId);
        }
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("eur")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();// Convert to cents
            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId = intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Payment initiated successfully for order ID: " + orderId)
                    .data(uniqueTransactionId)
                    .build();
        } catch (Exception e) {
            log.error("Error initiating payment for order ID: {}", orderId, e);
            throw new RuntimeException("Payment initiation failed for order ID: " + orderId, e);
        }

    }

    @Override
    public void updatePaymentForOrder(PaymentDTO paymentDTO) {
        log.info("Updating payment for order: {}", paymentDTO.getOrderId());

        Long orderId = paymentDTO.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentDTO.getAmount());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentStatus(paymentDTO.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setOrder(order);

        if (!paymentDTO.isSuccess()) {
            payment.setFailureReason(paymentDTO.getFailureReason());
        }

        paymentRepository.save(payment);

        // prepare email conect import from thymeleaf
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("orderId", order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount", "$" + paymentDTO.getAmount());

        if (paymentDTO.isSuccess()) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            context.setVariable("transactionId", paymentDTO.getTransactionId());
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern(("dd MMMM yyyy HH:mm:ss"))));
            context.setVariable("frontendBaseUrl", this.frontendBaseUrl);

            String emailBody = templateEngine.process("payment-success", context);
            notificationService.sendEmail(
                    NotificationDTO.builder()
                            .recipient(order.getUser().getEmail())
                            .subject("Payment Successful for Order #" + order.getId())
                            .body(emailBody)
                            .isHtml(true)
                            .build()
            );
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            context.setVariable("failureReason", paymentDTO.getFailureReason());


            String emailBody = templateEngine.process("payment-failed", context);
            notificationService.sendEmail(
                    NotificationDTO.builder()
                            .recipient(order.getUser().getEmail())
                            .subject("Payment Failed for Order #" + order.getId())
                            .body(emailBody)
                            .isHtml(true)
                            .build()
            );
        }

    }

    @Override
    public Response<List<PaymentDTO>> getAllPayments() {
        log.info("Fetching all payments");

        List<Payment> paymentList = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PaymentDTO> paymentDTOS = modelMapper.map(paymentList, new TypeToken<List<PaymentDTO>>() {}.getType());

        paymentDTOS.forEach(item ->{
            item.setOrder(null);
            item.setUser(null);
        });

        return Response.<List<PaymentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payments fetched successfully")
                .data(paymentDTOS)
                .build();
    }

    @Override
    public Response<PaymentDTO> getPaymentById(Long paymentId) {
        log.info("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        PaymentDTO paymentDTO = modelMapper.map(payment, PaymentDTO.class);

        paymentDTO.getUser().setRoles(null);
        paymentDTO.getOrder().setUser(null);
        paymentDTO.getOrder().getOrderItems().forEach(item -> {
            item.getMenu().setReviews(null);
        });

        return Response.<PaymentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment fetched successfully")
                .data(paymentDTO)
                .build();
    }
}
