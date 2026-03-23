package com.smartlaundry.paymentservice.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentService.PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.create(request.bookingId(), request.amount());
    }

    @PostMapping("/payments/{id}/confirm")
    public PaymentService.PaymentResponse confirm(@PathVariable UUID id) {
        return paymentService.confirm(id);
    }

    @PostMapping("/payments/{id}/reject")
    public PaymentService.PaymentResponse reject(@PathVariable UUID id, @Valid @RequestBody(required = false) RejectPaymentRequest request) {
        return paymentService.reject(id, request == null ? null : request.reason());
    }

    @GetMapping("/admin/payments/pending")
    public List<PaymentService.PaymentResponse> pending() {
        return paymentService.pendingPayments();
    }

    public record CreatePaymentRequest(
            @NotNull UUID bookingId,
            @NotNull @DecimalMin("0.01") BigDecimal amount
    ) {
    }

    public record RejectPaymentRequest(@Size(max = 255) String reason) {
    }
}
