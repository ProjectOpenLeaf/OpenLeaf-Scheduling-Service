package org.example.business;

import org.example.domain.Appointment;

import java.util.List;

public interface GetAvailableSlots {
    List<Appointment> getAvailable(String therapistKeycloakId);
}
