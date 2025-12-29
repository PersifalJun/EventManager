package ru.haritonenko.eventmanager.event.registration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.eventmanager.event.registration.db.entity.EventRegistrationEntity;
import ru.haritonenko.eventmanager.event.registration.status.EventRegistrationStatus;

import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Integer> {

    @Transactional(readOnly = true)
    Optional<EventRegistrationEntity> findByUserIdAndEventId(Integer userId, Integer eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE EventRegistrationEntity r
            SET r.status = :status
            WHERE r.user.id = :userId AND r.event.id = :eventId
            """)
    int updateStatus(
            @Param("userId") Integer userId,
            @Param("eventId") Integer eventId,
            @Param("status") EventRegistrationStatus status
    );

    @Transactional(readOnly = true)
    boolean existsByUserIdAndEventIdAndStatus(Integer userId, Integer eventId, EventRegistrationStatus status);
}
