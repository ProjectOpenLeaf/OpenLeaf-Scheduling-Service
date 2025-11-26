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

class GetAvailableSlotsImplTest {

    private AppointmentRepository appointmentRepository;
    private GetAvailableSlotsImpl getAvailableSlots;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        getAvailableSlots = new GetAvailableSlotsImpl(appointmentRepository);
    }

    private AppointmentEntity appointment(Long id, String status, LocalDateTime start) {
        return AppointmentEntity.builder()
                .id(id)
                .therapistKeycloakId("therapist123")
                .patientKeycloakId(null)
                .status(status)
                .startTime(start)
                .endTime(start.plusHours(1))
                .notes("notes")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --------------------------------------------------------
    // TEST 1 — Only future AVAILABLE appointments are returned
    // --------------------------------------------------------
    @Test
    void getAvailable_ShouldReturnOnlyAvailableFutureSlots() {

        LocalDateTime now = LocalDateTime.now();

        AppointmentEntity availableFuture = appointment(1L, "AVAILABLE", now.plusDays(1));
        AppointmentEntity bookedFuture = appointment(2L, "BOOKED", now.plusDays(1));
        AppointmentEntity pastAvailable = appointment(3L, "AVAILABLE", now.minusDays(1));

        when(appointmentRepository.findByTherapistKeycloakIdAndStartTimeAfter(
                eq("therapist123"), any(LocalDateTime.class))
        ).thenReturn(List.of(availableFuture, bookedFuture, pastAvailable));

        // Act
        List<Appointment> result = getAvailableSlots.getAvailable("therapist123");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");

        verify(appointmentRepository).findByTherapistKeycloakIdAndStartTimeAfter(eq("therapist123"), any(LocalDateTime.class));
    }

    // --------------------------------------------------------
    // TEST 2 — No available slots
    // --------------------------------------------------------
    @Test
    void getAvailable_ShouldReturnEmptyList_WhenNoAvailableSlots() {

        when(appointmentRepository.findByTherapistKeycloakIdAndStartTimeAfter(
                eq("therapist123"), any(LocalDateTime.class))
        ).thenReturn(List.of());

        List<Appointment> result = getAvailableSlots.getAvailable("therapist123");

        assertThat(result).isEmpty();

        verify(appointmentRepository).findByTherapistKeycloakIdAndStartTimeAfter(eq("therapist123"), any(LocalDateTime.class));
    }

    // --------------------------------------------------------
    // TEST 3 — Ensure domain model mapping correct
    // --------------------------------------------------------
    @Test
    void getAvailable_ShouldMapEntityToDomainCorrectly() {

        LocalDateTime future = LocalDateTime.now().plusHours(2);

        AppointmentEntity entity = appointment(10L, "AVAILABLE", future);

        when(appointmentRepository.findByTherapistKeycloakIdAndStartTimeAfter(
                eq("therapist123"), any(LocalDateTime.class))
        ).thenReturn(List.of(entity));

        List<Appointment> result = getAvailableSlots.getAvailable("therapist123");

        Appointment a = result.get(0);

        assertThat(a.getId()).isEqualTo(10L);
        assertThat(a.getTherapistKeycloakId()).isEqualTo("therapist123");
        assertThat(a.getStatus()).isEqualTo("AVAILABLE");
        assertThat(a.getStartTime()).isEqualTo(future);
        assertThat(a.getEndTime()).isEqualTo(future.plusHours(1));
        assertThat(a.getNotes()).isEqualTo("notes");

        verify(appointmentRepository).findByTherapistKeycloakIdAndStartTimeAfter(eq("therapist123"), any(LocalDateTime.class));
    }
}
