package com.fleet.monitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para almacenar datos de combustible.
 */
@Entity
@Table(name = "fuel_levels")
public class FuelLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "sensor_timestamp")
    private String sensorTimestamp;

    @Column(name = "nivel_combustible")
    private double nivelCombustible;

    @Column(name = "consumo_reciente")
    private double consumoReciente;

    private boolean alerta;
    private String priority;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getSensorTimestamp() { return sensorTimestamp; }
    public void setSensorTimestamp(String st) { this.sensorTimestamp = st; }

    public double getNivelCombustible() { return nivelCombustible; }
    public void setNivelCombustible(double n) { this.nivelCombustible = n; }

    public double getConsumoReciente() { return consumoReciente; }
    public void setConsumoReciente(double c) { this.consumoReciente = c; }

    public boolean isAlerta() { return alerta; }
    public void setAlerta(boolean alerta) { this.alerta = alerta; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
