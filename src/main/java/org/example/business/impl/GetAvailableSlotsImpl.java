package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.GetAvailableSlots;
import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAvailableSlotsImpl implements GetAvailableSlots {

    private final AppointmentRepository appointmentRepository;

    @Override
    public List<Appointment> getAvailable(String therapistKeycloakId) {
        // Get all available appointments for the therapist that are in the future
        List<AppointmentEntity> entities = appointmentRepository
                .findByTherapistKeycloakIdAndStartTimeAfter(therapistKeycloakId, LocalDateTime.now())
                .stream()
                .filter(entity -> "AVAILABLE".equals(entity.getStatus()))
                .collect(Collectors.toList());

        return entities.stream()
                .map(this::toAppointment)
                .collect(Collectors.toList());
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
