package org.example.business.impl;

import org.example.domain.Appointment;
import org.example.persistance.AppointmentRepository;
import org.example.persistance.entity.AppointmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BookAppointmentImplTest {

    private AppointmentRepository appointmentRepository;
    private BookAppointmentImpl bookAppointment;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        bookAppointment = new BookAppointmentImpl(appointmentRepository);
    }

    @Test
    void book_ShouldThrowException_WhenAppointmentNotFound() {

        when(appointmentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookAppointment.book(100L, "patient123", "Some notes")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Appointment not found");

        verify(appointmentRepository).findById(100L);
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void book_ShouldThrowException_WhenSlotIsNotAvailable() {

        AppointmentEntity entity = AppointmentEntity.builder()
                .id(1L)
                .status("BOOKED")
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
                bookAppointment.book(1L, "patient123", "notes")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Appointment slot is not available");

        verify(appointmentRepository).findById(1L);
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void book_ShouldBookAppointment_WhenSlotAvailable_AndNoExistingNotes() {

        AppointmentEntity entity = AppointmentEntity.builder()
                .id(1L)
                .therapistKeycloakId("therapistABC")
                .status("AVAILABLE")
                .notes(null)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = bookAppointment.book(1L, "patient123", "My notes");

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(entity);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPatientKeycloakId()).isEqualTo("patient123");
        assertThat(result.getStatus()).isEqualTo("BOOKED");
        assertThat(result.getNotes()).isEqualTo("Patient notes: My notes");
    }

    @Test
    void book_ShouldAppendPatientNotes_WhenNotesAlreadyExist() {

        AppointmentEntity entity = AppointmentEntity.builder()
                .id(1L)
                .therapistKeycloakId("therapistABC")
                .status("AVAILABLE")
                .notes("Existing therapist notes")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = bookAppointment.book(1L, "patient123", "Patient additional notes");

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(entity);

        assertThat(result.getStatus()).isEqualTo("BOOKED");
        assertThat(result.getNotes())
                .isEqualTo("Existing therapist notes | Patient notes: Patient additional notes");
    }
}
