package org.example.business.impl;

import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetUserAppointmentsImplTest {

    private AppointmentRepository appointmentRepository;
    private GetUserAppointmentsImpl getUserAppointments;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        getUserAppointments = new GetUserAppointmentsImpl(appointmentRepository);
    }

    private AppointmentEntity createEntity(Long id, String therapistId, String patientId, String status) {
        return AppointmentEntity.builder()
                .id(id)
                .therapistKeycloakId(therapistId)
                .patientKeycloakId(patientId)
                .status(status)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .notes("notes")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --------------------------------------------------------
    // TEST 1 — User as patient only
    // --------------------------------------------------------
    @Test
    void getUserAppointments_ShouldReturnAppointments_WhenUserIsPatient() {
        AppointmentEntity patientAppt = createEntity(1L, "therapist123", "user123", "BOOKED");

        when(appointmentRepository.findByPatientKeycloakId("user123"))
                .thenReturn(List.of(patientAppt));
        when(appointmentRepository.findByTherapistKeycloakIdAndStatus("user123", "BOOKED"))
                .thenReturn(List.of());

        List<Appointment> result = getUserAppointments.getUserAppointments("user123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(appointmentRepository).findByPatientKeycloakId("user123");
        verify(appointmentRepository).findByTherapistKeycloakIdAndStatus("user123", "BOOKED");
    }

    // --------------------------------------------------------
    // TEST 2 — User as therapist only
    // --------------------------------------------------------
    @Test
    void getUserAppointments_ShouldReturnAppointments_WhenUserIsTherapist() {
        AppointmentEntity therapistAppt = createEntity(2L, "user123", "patient456", "BOOKED");

        when(appointmentRepository.findByPatientKeycloakId("user123")).thenReturn(List.of());
        when(appointmentRepository.findByTherapistKeycloakIdAndStatus("user123", "BOOKED"))
                .thenReturn(List.of(therapistAppt));

        List<Appointment> result = getUserAppointments.getUserAppointments("user123");

        assertThat(result).hasSize(1);
        Appointment appt = result.get(0);
        assertThat(appt.getId()).isEqualTo(2L);
        assertThat(appt.getTherapistKeycloakId()).isEqualTo("user123");
        assertThat(appt.getPatientKeycloakId()).isEqualTo("patient456");

        verify(appointmentRepository).findByPatientKeycloakId("user123");
        verify(appointmentRepository).findByTherapistKeycloakIdAndStatus("user123", "BOOKED");
    }

    // --------------------------------------------------------
    // TEST 3 — User as both therapist and patient with duplicates
    // --------------------------------------------------------
    @Test
    void getUserAppointments_ShouldDeduplicateAppointments() {
        AppointmentEntity asPatient = createEntity(1L, "therapist123", "user123", "BOOKED");
        AppointmentEntity asTherapist = createEntity(1L, "user123", "user123", "BOOKED"); // duplicate id

        when(appointmentRepository.findByPatientKeycloakId("user123")).thenReturn(List.of(asPatient));
        when(appointmentRepository.findByTherapistKeycloakIdAndStatus("user123", "BOOKED"))
                .thenReturn(List.of(asTherapist));

        List<Appointment> result = getUserAppointments.getUserAppointments("user123");

        // deduplicated by .distinct()
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(appointmentRepository).findByPatientKeycloakId("user123");
        verify(appointmentRepository).findByTherapistKeycloakIdAndStatus("user123", "BOOKED");
    }

    // --------------------------------------------------------
    // TEST 4 — Mapping correctness
    // --------------------------------------------------------
    @Test
    void getUserAppointments_ShouldMapEntityToDomain() {
        AppointmentEntity entity = createEntity(10L, "therapistX", "user123", "BOOKED");

        when(appointmentRepository.findByPatientKeycloakId("user123")).thenReturn(List.of(entity));
        when(appointmentRepository.findByTherapistKeycloakIdAndStatus("user123", "BOOKED"))
                .thenReturn(List.of());

        Appointment appt = getUserAppointments.getUserAppointments("user123").get(0);

        assertThat(appt.getId()).isEqualTo(10L);
        assertThat(appt.getTherapistKeycloakId()).isEqualTo("therapistX");
        assertThat(appt.getPatientKeycloakId()).isEqualTo("user123");
        assertThat(appt.getStatus()).isEqualTo("BOOKED");
        assertThat(appt.getNotes()).isEqualTo("notes");
    }
}
