package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    private Long id;
    private String therapistKeycloakId;
    private String patientKeycloakId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // AVAILABLE, BOOKED, CANCELLED, COMPLETED
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
