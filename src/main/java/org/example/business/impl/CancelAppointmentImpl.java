package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.CancelAppointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelAppointmentImpl implements CancelAppointment {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void cancel(Long appointmentId, String userKeycloakId) {
        AppointmentEntity entity = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify user is either the therapist or patient
        boolean isTherapist = entity.getTherapistKeycloakId().equals(userKeycloakId);
        boolean isPatient = entity.getPatientKeycloakId() != null &&
                entity.getPatientKeycloakId().equals(userKeycloakId);

        if (!isTherapist && !isPatient) {
            throw new RuntimeException("User not authorized to cancel this appointment");
        }

        // Check if appointment can be cancelled
        if ("CANCELLED".equals(entity.getStatus()) || "COMPLETED".equals(entity.getStatus())) {
            throw new RuntimeException("Appointment cannot be cancelled");
        }

        // If appointment was booked, make it available again
        if ("BOOKED".equals(entity.getStatus())) {
            entity.setPatientKeycloakId(null);
            entity.setStatus("CANCELLED");
        } else {
            entity.setStatus("CANCELLED");
        }

        appointmentRepository.save(entity);
    }
}