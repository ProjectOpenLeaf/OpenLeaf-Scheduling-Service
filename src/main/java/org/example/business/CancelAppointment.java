package org.example.business;

public interface CancelAppointment {
    void cancel(Long appointmentId, String userKeycloakId);
}
