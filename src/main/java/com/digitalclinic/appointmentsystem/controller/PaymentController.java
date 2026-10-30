package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.PaymentRequestDTO;
import com.digitalclinic.appointmentsystem.dto.PaymentResponseDTO;
import com.digitalclinic.appointmentsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponseDTO> createIntent(@RequestBody PaymentRequestDTO requestDTO) {
        // Return a mock intent
        return ResponseEntity.ok(paymentService.createIntent(requestDTO));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(
            @RequestBody PaymentRequestDTO requestDTO,
            @RequestParam Long patientId) {

        return ResponseEntity.ok(paymentService.processPayment(requestDTO, patientId));
    }
}
