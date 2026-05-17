package com.fleet.monitor.controller;

import com.fleet.monitor.entity.*;
import com.fleet.monitor.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador REST para consultar el estado de la flota.
 *
 * Endpoints:
 *   GET /api/fleet/status              - Estado general de la flota
 *   GET /api/fleet/vehicle/{id}/telemetria - Telemetria de un vehiculo
 *   GET /api/fleet/vehicle/{id}/gps    - Datos GPS de un vehiculo
 *   GET /api/fleet/alerts              - Todas las alertas activas
 *   GET /api/fleet/notifications       - Notificaciones pendientes
 *   GET /api/fleet/vehicles            - Lista de vehiculos con ultimo estado
 */
@RestController
@RequestMapping("/api/fleet")
@CrossOrigin(origins = "*")
public class FleetController {

    private final GpsTelemetryRepository gpsRepository;
    private final TemperatureAlertRepository tempRepository;
    private final FuelLevelRepository fuelRepository;
    private final NotificationRepository notificationRepository;

    // Vehiculos conocidos de la flota
    private static final List<String> FLEET_VEHICLES = Arrays.asList("VH-001", "VH-002", "VH-003");

    public FleetController(GpsTelemetryRepository gpsRepository,
                           TemperatureAlertRepository tempRepository,
                           FuelLevelRepository fuelRepository,
                           NotificationRepository notificationRepository) {
        this.gpsRepository = gpsRepository;
        this.tempRepository = tempRepository;
        this.fuelRepository = fuelRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * GET /api/fleet/status
     * Retorna un resumen general del estado de la flota.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFleetStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("totalVehicles", FLEET_VEHICLES.size());
        status.put("vehicleIds", FLEET_VEHICLES);
        status.put("totalGpsRecords", gpsRepository.count());
        status.put("totalTempRecords", tempRepository.count());
        status.put("totalFuelRecords", fuelRepository.count());
        status.put("activeTemperatureAlerts", tempRepository.countByAlertaTrue());
        status.put("activeFuelAlerts", fuelRepository.countByAlertaTrue());
        status.put("unreadNotifications", notificationRepository.countByIsReadFalse());
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("systemStatus", "OPERATIVO");
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/fleet/vehicle/{id}/telemetria
     * Retorna toda la telemetria reciente de un vehiculo especifico.
     */
    @GetMapping("/vehicle/{id}/telemetria")
    public ResponseEntity<Map<String, Object>> getTelemetria(@PathVariable String id) {
        Map<String, Object> telemetria = new LinkedHashMap<>();
        telemetria.put("vehicleId", id);
        telemetria.put("gps", gpsRepository.findTop10ByVehicleIdOrderByCreatedAtDesc(id));
        telemetria.put("temperatura", tempRepository.findByVehicleIdOrderByCreatedAtDesc(id));
        telemetria.put("combustible", fuelRepository.findByVehicleIdOrderByCreatedAtDesc(id));
        telemetria.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(telemetria);
    }

    /**
     * GET /api/fleet/vehicle/{id}/gps
     * Retorna los ultimos 10 registros GPS de un vehiculo.
     */
    @GetMapping("/vehicle/{id}/gps")
    public ResponseEntity<List<GpsTelemetry>> getGpsData(@PathVariable String id) {
        return ResponseEntity.ok(
            gpsRepository.findTop10ByVehicleIdOrderByCreatedAtDesc(id)
        );
    }

    /**
     * GET /api/fleet/alerts
     * Retorna todas las alertas activas (temperatura y combustible).
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> alerts = new LinkedHashMap<>();
        alerts.put("temperatureAlerts", tempRepository.findByAlertaTrueOrderByCreatedAtDesc());
        alerts.put("fuelAlerts", fuelRepository.findByAlertaTrueOrderByCreatedAtDesc());
        alerts.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/fleet/notifications
     * Retorna las notificaciones no leidas.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(
            notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()
        );
    }

    /**
     * GET /api/fleet/vehicles
     * Retorna un resumen por vehiculo con su ultimo estado conocido.
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<Map<String, Object>>> getVehicles() {
        List<Map<String, Object>> vehicles = new ArrayList<>();

        for (String vehicleId : FLEET_VEHICLES) {
            Map<String, Object> vehicleInfo = new LinkedHashMap<>();
            vehicleInfo.put("vehicleId", vehicleId);

            // Ultima posicion GPS
            List<GpsTelemetry> lastGps = gpsRepository.findTop1ByVehicleIdOrderByCreatedAtDesc(vehicleId);
            if (!lastGps.isEmpty()) {
                GpsTelemetry gps = lastGps.get(0);
                vehicleInfo.put("lastLat", gps.getLat());
                vehicleInfo.put("lastLng", gps.getLng());
                vehicleInfo.put("lastSpeed", gps.getSpeed());
                vehicleInfo.put("lastGpsUpdate", gps.getCreatedAt());
            } else {
                vehicleInfo.put("lastLat", null);
                vehicleInfo.put("lastLng", null);
                vehicleInfo.put("gpsStatus", "SIN DATOS");
            }

            vehicleInfo.put("totalGpsRecords", gpsRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId).size());
            vehicles.add(vehicleInfo);
        }

        return ResponseEntity.ok(vehicles);
    }
}
