package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long appointmentId;
    private String provider;
    private String cardNumberMock; // Just for dummy gateway
    private String amount; // Readonly validation from client
}
