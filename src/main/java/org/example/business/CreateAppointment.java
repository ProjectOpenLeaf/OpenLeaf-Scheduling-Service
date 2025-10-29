package org.example.business;

import org.example.domain.Appointment;

import java.time.LocalDateTime;

public interface CreateAppointment {
    Appointment create(String therapistKeycloakId, LocalDateTime startTime, LocalDateTime endTime, String notes);
}
