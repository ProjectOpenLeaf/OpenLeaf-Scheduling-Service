package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.business.*;
import org.example.business.dto.AppointmentResponse;
import org.example.business.dto.BookAppointmentRequest;
import org.example.business.dto.CreateAppointmentRequest;
import org.example.domain.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class SchedulingController {

    private final CreateAppointment createAppointment;
    private final GetAvailableSlots getAvailableSlots;
    private final BookAppointment bookAppointment;
    private final CancelAppointment cancelAppointment;
    private final GetUserAppointments getUserAppointments;

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointmentSlot(
            @RequestHeader("X-User-Id") String therapistKeycloakId,
            @RequestBody CreateAppointmentRequest request) {

        Appointment appointment = createAppointment.create(
                therapistKeycloakId,
                request.getStartTime(),
                request.getEndTime(),
                request.getNotes()
        );

        return ResponseEntity.ok(toResponse(appointment));
    }

    @GetMapping("/therapist/{therapistId}/available")
    public ResponseEntity<List<AppointmentResponse>> getAvailableAppointments(
            @PathVariable String therapistId) {

        List<Appointment> appointments = getAvailableSlots.getAvailable(therapistId);

        List<AppointmentResponse> responses = appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{appointmentId}/book")
    public ResponseEntity<AppointmentResponse> bookAppointmentSlot(
            @PathVariable Long appointmentId,
            @RequestHeader("X-User-Id") String patientKeycloakId,
            @RequestBody(required = false) BookAppointmentRequest request) {

        String notes = request != null ? request.getNotes() : null;
        Appointment appointment = bookAppointment.book(appointmentId, patientKeycloakId, notes);

        return ResponseEntity.ok(toResponse(appointment));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<String> cancelAppointmentSlot(
            @PathVariable Long appointmentId,
            @RequestHeader("X-User-Id") String userKeycloakId) {

        cancelAppointment.cancel(appointmentId, userKeycloakId);
        return ResponseEntity.ok("Appointment cancelled successfully");
    }

    @GetMapping("/user")
    public ResponseEntity<List<AppointmentResponse>> getUserAppointmentsList(
            @RequestHeader("X-User-Id") String userKeycloakId) {

        List<Appointment> appointments = getUserAppointments.getUserAppointments(userKeycloakId);

        List<AppointmentResponse> responses = appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .therapistKeycloakId(appointment.getTherapistKeycloakId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}







