package org.example.persistance;

import org.example.persistance.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    List<AppointmentEntity> findByTherapistKeycloakIdAndStatus(String therapistKeycloakId, String status);

    List<AppointmentEntity> findByTherapistKeycloakIdAndStartTimeAfter(String therapistKeycloakId, LocalDateTime startTime);

    List<AppointmentEntity> findByPatientKeycloakId(String patientKeycloakId);

    boolean existsByTherapistKeycloakIdAndStartTimeBetween(String therapistKeycloakId, LocalDateTime start, LocalDateTime end);

    int deleteByPatientKeycloakId(String patientKeycloakId);

    int deleteByTherapistKeycloakId(String therapistKeycloakId);
}