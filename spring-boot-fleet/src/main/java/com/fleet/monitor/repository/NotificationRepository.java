package com.fleet.monitor.repository;

import com.fleet.monitor.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    List<Notification> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    long countByIsReadFalse();
}
