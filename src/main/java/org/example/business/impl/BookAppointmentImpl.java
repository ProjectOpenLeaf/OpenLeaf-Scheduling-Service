package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.BookAppointment;
import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookAppointmentImpl implements BookAppointment {

    private final AppointmentRepository appointmentRepository;

    @Override
    public Appointment book(Long appointmentId, String patientKeycloakId, String notes) {
        AppointmentEntity entity = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if slot is available
        if (!"AVAILABLE".equals(entity.getStatus())) {
            throw new RuntimeException("Appointment slot is not available");
        }

        // Book the appointment
        entity.setPatientKeycloakId(patientKeycloakId);
        entity.setStatus("BOOKED");
        if (notes != null && !notes.isEmpty()) {
            entity.setNotes(entity.getNotes() != null ?
                    entity.getNotes() + " | Patient notes: " + notes : "Patient notes: " + notes);
        }

        AppointmentEntity saved = appointmentRepository.save(entity);
        return toAppointment(saved);
    }

    private Appointment toAppointment(AppointmentEntity entity) {
        return Appointment.builder()
                .id(entity.getId())
                .therapistKeycloakId(entity.getTherapistKeycloakId())
                .patientKeycloakId(entity.getPatientKeycloakId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
