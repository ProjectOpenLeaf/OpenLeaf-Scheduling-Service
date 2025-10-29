package org.example.business;

import org.example.domain.Appointment;

public interface BookAppointment {
    Appointment book(Long appointmentId, String patientKeycloakId, String notes);
}
