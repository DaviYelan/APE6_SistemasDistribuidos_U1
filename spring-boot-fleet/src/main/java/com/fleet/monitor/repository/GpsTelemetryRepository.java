package com.fleet.monitor.repository;

import com.fleet.monitor.entity.GpsTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GpsTelemetryRepository extends JpaRepository<GpsTelemetry, Long> {
    List<GpsTelemetry> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    List<GpsTelemetry> findTop10ByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    List<GpsTelemetry> findTop1ByVehicleIdOrderByCreatedAtDesc(String vehicleId);
}
