package org.example.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.business.dto.AccountDeletionEvent;
import org.example.config.RabbitMQConfig;
import org.example.persistance.AppointmentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for account deletion events in Scheduling Service
 * Deletes all appointments related to the deleted user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingDeletionConsumer {

    private final AppointmentRepository appointmentRepository;

    /**
     * Listen for account deletion events and delete related appointments
     *
     * @param event The account deletion event
     */
    @RabbitListener(queues = RabbitMQConfig.SCHEDULING_DELETION_QUEUE)
    @Transactional
    public void handleAccountDeletion(AccountDeletionEvent event) {
        try {
            String userKeycloakId = event.getUserKeycloakId();
            log.info("Received account deletion event for user: {} - Reason: {}",
                    userKeycloakId, event.getReason());

            // Delete appointments where user is the patient
            int deletedAsPatient = appointmentRepository.deleteByPatientKeycloakId(userKeycloakId);
            log.info("Deleted {} appointments where user was patient", deletedAsPatient);

            // Delete appointments where user is the therapist
            int deletedAsTherapist = appointmentRepository.deleteByTherapistKeycloakId(userKeycloakId);
            log.info("Deleted {} appointments where user was therapist", deletedAsTherapist);

            log.info("Successfully processed account deletion for user: {} in Scheduling Service",
                    userKeycloakId);

        } catch (Exception e) {
            log.error("Failed to process account deletion event for user: {}",
                    event.getUserKeycloakId(), e);
            // In production, you might want to implement a dead letter queue
            // or retry mechanism here
            throw e; // This will trigger RabbitMQ redelivery
        }
    }
}
