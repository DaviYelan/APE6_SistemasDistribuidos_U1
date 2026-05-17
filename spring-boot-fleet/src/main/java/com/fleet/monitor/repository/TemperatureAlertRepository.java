package com.fleet.monitor.repository;

import com.fleet.monitor.entity.TemperatureAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemperatureAlertRepository extends JpaRepository<TemperatureAlert, Long> {
    List<TemperatureAlert> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    List<TemperatureAlert> findByAlertaTrueOrderByCreatedAtDesc();
    long countByAlertaTrue();
}
