package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PaymentRequestDTO;
import com.digitalclinic.appointmentsystem.dto.PaymentResponseDTO;
import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.model.Payment;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final AppointmentRepository appointmentRepository;
        private final NotificationService notificationService;

        public PaymentResponseDTO createIntent(PaymentRequestDTO request) {
                // Return mock intent success
                return PaymentResponseDTO.builder()
                                .status("SUCCESS")
                                .transactionId("pi_" + UUID.randomUUID().toString())
                                .message("Intent created successfully")
                                .build();
        }

        @Transactional
        public PaymentResponseDTO processPayment(PaymentRequestDTO request, Long patientId) {
                Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                                .orElseThrow(() -> new RuntimeException("Appointment not found"));

                if (!appointment.getPatient().getId().equals(patientId)) {
                        throw new RuntimeException("Not authorized to pay for this appointment");
                }

                if (appointment.getPaymentStatus() == Appointment.PaymentStatus.PAID) {
                        throw new RuntimeException("Appointment is already paid.");
                }

                // Mock payment logic
                boolean isSuccess = !request.getCardNumberMock().startsWith("4000000000000001"); // Simple mock trigger
                                                                                                 // for
                                                                                                 // failure
                String txId = "TX-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

                Payment payment = Payment.builder()
                                .appointment(appointment)
                                .amount(new BigDecimal(request.getAmount()))
                                .currency("USD")
                                .provider(request.getProvider())
                                .transactionId(txId)
                                .status(isSuccess ? "SUCCESS" : "FAILED")
                                .build();

                Payment savedPayment = paymentRepository.save(payment);

                if (isSuccess) {
                        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                        appointment.setPaymentStatus(Appointment.PaymentStatus.PAID);
                        appointmentRepository.save(appointment);

                        // Notify Doctor that appointment is booked and paid
                        notificationService.sendNotification(
                                        appointment.getDoctor().getUser().getId(),
                                        "New Paid Appointment",
                                        "Patient " + appointment.getPatient().getFullName()
                                                        + " has confirmed and paid for an appointment on "
                                                        + appointment.getAppointmentDate(),
                                        "APPOINTMENT");

                        // Notify Patient
                        notificationService.sendNotification(
                                        appointment.getPatient().getUser().getId(),
                                        "Payment Successful",
                                        "Your payment of $" + request.getAmount() + " for appointment on "
                                                        + appointment.getAppointmentDate() + " was successful.",
                                        "PAYMENT");

                } else {
                        notificationService.sendNotification(
                                        appointment.getPatient().getUser().getId(),
                                        "Payment Failed",
                                        "Your recent payment attempt for $" + request.getAmount()
                                                        + " failed. Please try again.",
                                        "PAYMENT");
                }

                return PaymentResponseDTO.builder()
                                .paymentId(savedPayment.getId())
                                .appointmentId(appointment.getId())
                                .transactionId(txId)
                                .status(savedPayment.getStatus())
                                .amount(savedPayment.getAmount().toString())
                                .currency(savedPayment.getCurrency())
                                .paymentDate(savedPayment.getPaymentDate())
                                .message(isSuccess ? "Payment processed successfully"
                                                : "Payment failed. Please check your card.")
                                .build();
        }
}
