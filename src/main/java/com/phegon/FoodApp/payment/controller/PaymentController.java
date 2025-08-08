package com.phegon.FoodApp.payment.controller;

import com.phegon.FoodApp.payment.dtos.PaymentDTO;
import com.phegon.FoodApp.payment.services.PaymentService;
import com.phegon.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Response<?>> initializePayment(@RequestBody @Valid PaymentDTO paymentRequest) {
        return ResponseEntity.ok(paymentService.initiatePayment(paymentRequest));
    }

    @PutMapping("/update")
    public void updatePaymentForOrder(@RequestBody  PaymentDTO paymentRequest){
        paymentService.updatePaymentForOrder(paymentRequest);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<PaymentDTO>>> getAllPayments() {
        Response<List<PaymentDTO>> response = paymentService.getAllPayments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<PaymentDTO>> getPaymentById(@PathVariable Long paymentId) {
        Response<PaymentDTO> response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }
}
