package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PrescriptionDTO;
import com.digitalclinic.appointmentsystem.dto.PrescriptionMedicationDTO;
import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Prescription;
import com.digitalclinic.appointmentsystem.model.PrescriptionMedication;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.PrescriptionRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PrescriptionService {

        private final PrescriptionRepository prescriptionRepository;
        private final PatientRepository patientRepository;
        private final DoctorRepository doctorRepository;
        private final AppointmentRepository appointmentRepository;

        @Value("${app.upload.dir.prescriptions:uploads/prescriptions}")
        private String prescriptionUploadDir;

        @Transactional
        public PrescriptionDTO createPrescription(PrescriptionDTO dto) {
                Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new RuntimeException("Patient not found"));
                Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                                .orElseThrow(() -> new RuntimeException("Doctor not found"));

                Appointment appointment = null;
                if (dto.getAppointmentId() != null) {
                        appointment = appointmentRepository.findById(dto.getAppointmentId()).orElse(null);
                }

                Prescription prescription = Prescription.builder()
                                .patient(patient)
                                .doctor(doctor)
                                .appointment(appointment)
                                .diagnosis(dto.getDiagnosis())
                                .generalInstructions(dto.getGeneralInstructions())
                                .followUpNotes(dto.getFollowUpNotes())
                                .build();

                if (dto.getMedications() != null) {
                        for (PrescriptionMedicationDTO medDto : dto.getMedications()) {
                                PrescriptionMedication med = PrescriptionMedication.builder()
                                                .medicineName(medDto.getMedicineName())
                                                .dosage(medDto.getDosage())
                                                .frequency(medDto.getFrequency())
                                                .duration(medDto.getDuration())
                                                .timing(medDto.getTiming())
                                                .instructions(medDto.getInstructions())
                                                .build();
                                prescription.addMedication(med);
                        }
                }

                Prescription saved = prescriptionRepository.save(prescription);

                // Generate PDF
                try {
                        String pdfUrl = generatePdf(saved);
                        saved.setPdfUrl(pdfUrl);
                        saved = prescriptionRepository.save(saved);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to generate PDF", e);
                }

                return mapToDTO(saved);
        }

        public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
                return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId)
                                .stream().map(this::mapToDTO).collect(Collectors.toList());
        }

        public PrescriptionDTO getPrescriptionById(Long id) {
                return prescriptionRepository.findById(id)
                                .map(this::mapToDTO)
                                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        }

        private String generatePdf(Prescription prescription) throws IOException, DocumentException {
                Path uploadPath = Paths.get(prescriptionUploadDir);
                if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                }

                String fileName = "prescription_" + prescription.getId() + "_" + UUID.randomUUID().toString() + ".pdf";
                Path filePath = uploadPath.resolve(fileName);

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
                document.open();

                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

                Paragraph title = new Paragraph("Digital Clinic - Prescription", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Doctor: Dr. " + prescription.getDoctor().getFullName(), normalFont));
                document.add(new Paragraph("Specialty: " + prescription.getDoctor().getSpecialization(), normalFont));
                document.add(new Paragraph("Patient: " + prescription.getPatient().getFullName(), normalFont));
                document.add(new Paragraph("Date: " + prescription.getPrescriptionDate().toLocalDate().toString(),
                                normalFont));
                document.add(new Paragraph("Diagnosis: "
                                + (prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "N/A"),
                                normalFont));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Medications:", subTitleFont));
                document.add(new Paragraph(" "));

                if (!prescription.getMedications().isEmpty()) {
                        PdfPTable table = new PdfPTable(5);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[] { 3, 1.5f, 2, 1.5f, 2 });

                        table.addCell(new PdfPCell(new Phrase("Medicine", normalFont)));
                        table.addCell(new PdfPCell(new Phrase("Dosage", normalFont)));
                        table.addCell(new PdfPCell(new Phrase("Frequency", normalFont)));
                        table.addCell(new PdfPCell(new Phrase("Duration", normalFont)));
                        table.addCell(new PdfPCell(new Phrase("Instructions", normalFont)));

                        for (PrescriptionMedication med : prescription.getMedications()) {
                                table.addCell(new Phrase(med.getMedicineName(), normalFont));
                                table.addCell(new Phrase(med.getDosage() != null ? med.getDosage() : "", normalFont));
                                table.addCell(new Phrase(med.getFrequency() != null ? med.getFrequency() : "",
                                                normalFont));
                                table.addCell(new Phrase(med.getDuration() != null ? med.getDuration() : "",
                                                normalFont));
                                table.addCell(new Phrase(med.getInstructions() != null ? med.getInstructions() : "",
                                                normalFont));
                        }
                        document.add(table);
                } else {
                        document.add(new Paragraph("No medications prescribed.", normalFont));
                }

                document.add(new Paragraph(" "));
                document.add(new Paragraph("General Instructions:", subTitleFont));
                document.add(new Paragraph(
                                prescription.getGeneralInstructions() != null ? prescription.getGeneralInstructions()
                                                : "None",
                                normalFont));

                document.add(new Paragraph(" "));
                document.add(new Paragraph("Follow-up Notes:", subTitleFont));
                document.add(new Paragraph(
                                prescription.getFollowUpNotes() != null ? prescription.getFollowUpNotes() : "None",
                                normalFont));

                document.close();

                return "/api/prescriptions/download/" + fileName;
        }

        private PrescriptionDTO mapToDTO(Prescription prescription) {
                return PrescriptionDTO.builder()
                                .id(prescription.getId())
                                .patientId(prescription.getPatient().getId())
                                .patientName(prescription.getPatient().getFullName())
                                .doctorId(prescription.getDoctor().getId())
                                .doctorName("Dr. " + prescription.getDoctor().getFullName())
                                .appointmentId(prescription.getAppointment() != null
                                                ? prescription.getAppointment().getId()
                                                : null)
                                .diagnosis(prescription.getDiagnosis())
                                .generalInstructions(prescription.getGeneralInstructions())
                                .followUpNotes(prescription.getFollowUpNotes())
                                .pdfUrl(prescription.getPdfUrl())
                                .prescriptionDate(prescription.getPrescriptionDate())
                                .medications(prescription.getMedications().stream()
                                                .map(m -> PrescriptionMedicationDTO.builder()
                                                                .id(m.getId())
                                                                .medicineName(m.getMedicineName())
                                                                .dosage(m.getDosage())
                                                                .frequency(m.getFrequency())
                                                                .duration(m.getDuration())
                                                                .timing(m.getTiming())
                                                                .instructions(m.getInstructions())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }
}
