package org.example.business.impl;

import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateAppointmentImplTest {

    private AppointmentRepository appointmentRepository;
    private CreateAppointmentImpl createAppointment;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        createAppointment = new CreateAppointmentImpl(appointmentRepository);
    }

    // --------------------------------------------------------
    // TEST 1 — Cannot create in the past
    // --------------------------------------------------------
    @Test
    void create_ShouldThrow_WhenStartTimeIsInThePast() {
        String therapistId = "therapist123";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);

        assertThatThrownBy(() ->
                createAppointment.create(therapistId, startTime, endTime, "notes")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot create appointment in the past");

        verifyNoInteractions(appointmentRepository);
    }

    // --------------------------------------------------------
    // TEST 2 — End time must be after start time
    // --------------------------------------------------------
    @Test
    void create_ShouldThrow_WhenEndTimeIsBeforeOrEqualToStart() {
        String therapistId = "therapist123";
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime; // invalid: equal

        assertThatThrownBy(() ->
                createAppointment.create(therapistId, startTime, endTime, "notes")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("End time must be after start time");

        verifyNoInteractions(appointmentRepository);
    }

    // --------------------------------------------------------
    // TEST 3 — Overlapping appointments must be blocked
    // --------------------------------------------------------
    @Test
    void create_ShouldThrow_WhenTimeSlotOverlaps() {
        String therapistId = "therapist123";
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);

        when(appointmentRepository.existsByTherapistKeycloakIdAndStartTimeBetween(
                eq(therapistId), any(), eq(endTime))
        ).thenReturn(true);

        assertThatThrownBy(() ->
                createAppointment.create(therapistId, startTime, endTime, "notes")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Time slot conflicts with existing appointment");

        verify(appointmentRepository).existsByTherapistKeycloakIdAndStartTimeBetween(
                eq(therapistId), any(), eq(endTime)
        );
        verifyNoMoreInteractions(appointmentRepository);
    }

    // --------------------------------------------------------
    // TEST 4 — Successful creation
    // --------------------------------------------------------
    @Test
    void create_ShouldSaveAppointment_WhenValid() {

        String therapistId = "therapist123";
        LocalDateTime startTime = LocalDateTime.now().plusHours(3);
        LocalDateTime endTime = startTime.plusHours(1);
        String notes = "Initial notes";

        AppointmentEntity saved = AppointmentEntity.builder()
                .id(1L)
                .therapistKeycloakId(therapistId)
                .startTime(startTime)
                .endTime(endTime)
                .status("AVAILABLE")
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(appointmentRepository.existsByTherapistKeycloakIdAndStartTimeBetween(
                anyString(), any(), any())
        ).thenReturn(false);

        when(appointmentRepository.save(any(AppointmentEntity.class)))
                .thenReturn(saved);

        // Act
        Appointment result = createAppointment.create(therapistId, startTime, endTime, notes);

        // Capture entity passed to save()
        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(appointmentRepository).save(captor.capture());
        AppointmentEntity passed = captor.getValue();

        // Validate fields of the entity BEFORE saving
        assertThat(passed.getTherapistKeycloakId()).isEqualTo(therapistId);
        assertThat(passed.getStartTime()).isEqualTo(startTime);
        assertThat(passed.getEndTime()).isEqualTo(endTime);
        assertThat(passed.getStatus()).isEqualTo("AVAILABLE");
        assertThat(passed.getNotes()).isEqualTo(notes);

        // Validate returned domain object matches saved entity
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTherapistKeycloakId()).isEqualTo(therapistId);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.getNotes()).isEqualTo(notes);
    }
}
