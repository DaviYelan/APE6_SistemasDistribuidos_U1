package com.fleet.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modelo de datos para mensajes GPS recibidos via RabbitMQ.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsMessage {

    private String vehicleId;
    private String timestamp;
    private double lat;
    private double lng;
    private double speed;
    private double heading;
    private String ruta;
    private String priority;
    private String bridge_timestamp;
    private String source_protocol;
    private String target_protocol;
    private String message_type;

    // ── Constructores ──
    public GpsMessage() {}

    // ── Getters y Setters ──
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getHeading() { return heading; }
    public void setHeading(double heading) { this.heading = heading; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getBridge_timestamp() { return bridge_timestamp; }
    public void setBridge_timestamp(String bridge_timestamp) { this.bridge_timestamp = bridge_timestamp; }

    public String getSource_protocol() { return source_protocol; }
    public void setSource_protocol(String source_protocol) { this.source_protocol = source_protocol; }

    public String getTarget_protocol() { return target_protocol; }
    public void setTarget_protocol(String target_protocol) { this.target_protocol = target_protocol; }

    public String getMessage_type() { return message_type; }
    public void setMessage_type(String message_type) { this.message_type = message_type; }

    @Override
    public String toString() {
        return "GpsMessage{vehicleId='" + vehicleId + "', lat=" + lat +
               ", lng=" + lng + ", speed=" + speed + "}";
    }
}
