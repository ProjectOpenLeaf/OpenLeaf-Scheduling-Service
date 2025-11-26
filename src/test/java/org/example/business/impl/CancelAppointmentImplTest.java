package org.example.business.impl;

import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelAppointmentImplTest {

    private AppointmentRepository appointmentRepository;
    private CancelAppointmentImpl cancelAppointment;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        cancelAppointment = new CancelAppointmentImpl(appointmentRepository);
    }

    private AppointmentEntity baseEntity() {
        return AppointmentEntity.builder()
                .id(1L)
                .therapistKeycloakId("therapist123")
                .patientKeycloakId("patient123")
                .status("BOOKED")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void cancel_ShouldThrow_WhenAppointmentNotFound() {

        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                cancelAppointment.cancel(1L, "patient123")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Appointment not found");

        verify(appointmentRepository).findById(1L);
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void cancel_ShouldThrow_WhenUserNotAuthorized() {

        AppointmentEntity entity = baseEntity();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                cancelAppointment.cancel(1L, "randomUser999")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("User not authorized to cancel this appointment");

        verify(appointmentRepository).findById(1L);
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void cancel_ShouldThrow_WhenStatusIsNotCancellable() {

        AppointmentEntity entity = baseEntity();
        entity.setStatus("CANCELLED");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                cancelAppointment.cancel(1L, "therapist123")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Appointment cannot be cancelled");

        verify(appointmentRepository).findById(1L);
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void cancel_ShouldThrow_WhenCompleted() {

        AppointmentEntity entity = baseEntity();
        entity.setStatus("COMPLETED");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                cancelAppointment.cancel(1L, "patient123")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Appointment cannot be cancelled");

        verify(appointmentRepository).findById(1L);
        verifyNoMoreInteractions(appointmentRepository);
    }


    @Test
    void cancel_ShouldCancelBooked_WhenTherapistCancels() {

        AppointmentEntity entity = baseEntity();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        cancelAppointment.cancel(1L, "therapist123");

        assertThat(entity.getStatus()).isEqualTo("CANCELLED");
        assertThat(entity.getPatientKeycloakId()).isNull();

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void cancel_ShouldCancelBooked_WhenPatientCancels() {

        AppointmentEntity entity = baseEntity();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        cancelAppointment.cancel(1L, "patient123");

        assertThat(entity.getStatus()).isEqualTo("CANCELLED");
        assertThat(entity.getPatientKeycloakId()).isNull();

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void cancel_ShouldCancelAvailable_WhenTherapistCancels() {

        AppointmentEntity entity = baseEntity();
        entity.setStatus("AVAILABLE");
        entity.setPatientKeycloakId(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        cancelAppointment.cancel(1L, "therapist123");

        assertThat(entity.getStatus()).isEqualTo("CANCELLED");

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(entity);
    }
}
