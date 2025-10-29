package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.CreateAppointment;
import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateAppointmentImpl implements CreateAppointment {

    private final AppointmentRepository appointmentRepository;

    @Override
    public Appointment create(String therapistKeycloakId, LocalDateTime startTime,
                              LocalDateTime endTime, String notes) {

        // Validate time slot is not in the past
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot create appointment in the past");
        }

        // Validate end time is after start time
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }

        // Check for overlapping appointments
        if (appointmentRepository.existsByTherapistKeycloakIdAndStartTimeBetween(
                therapistKeycloakId, startTime.minusMinutes(1), endTime)) {
            throw new RuntimeException("Time slot conflicts with existing appointment");
        }

        AppointmentEntity entity = AppointmentEntity.builder()
                .therapistKeycloakId(therapistKeycloakId)
                .startTime(startTime)
                .endTime(endTime)
                .status("AVAILABLE")
                .notes(notes)
                .build();

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
