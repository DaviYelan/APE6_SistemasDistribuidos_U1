package com.fleet.monitor.repository;

import com.fleet.monitor.entity.FuelLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FuelLevelRepository extends JpaRepository<FuelLevel, Long> {
    List<FuelLevel> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    List<FuelLevel> findByAlertaTrueOrderByCreatedAtDesc();
    long countByAlertaTrue();
}
