package org.example.business;

import org.example.domain.Appointment;

import java.util.List;

public interface GetUserAppointments {
    List<Appointment> getUserAppointments(String userKeycloakId);
}
