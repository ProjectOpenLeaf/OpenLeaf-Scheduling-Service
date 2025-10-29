package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.GetUserAppointments;
import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GetUserAppointmentsImpl implements GetUserAppointments {

    private final AppointmentRepository appointmentRepository;

    @Override
    public List<Appointment> getUserAppointments(String userKeycloakId) {
        // Get appointments where user is either therapist or patient
        List<AppointmentEntity> asPatient = appointmentRepository
                .findByPatientKeycloakId(userKeycloakId);

        List<AppointmentEntity> asTherapist = appointmentRepository
                .findByTherapistKeycloakIdAndStatus(userKeycloakId, "BOOKED")
                .stream()
                .filter(e -> e.getPatientKeycloakId() != null)
                .collect(Collectors.toList());

        // Combine and remove duplicates
        return Stream.concat(asPatient.stream(), asTherapist.stream())
                .distinct()
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
