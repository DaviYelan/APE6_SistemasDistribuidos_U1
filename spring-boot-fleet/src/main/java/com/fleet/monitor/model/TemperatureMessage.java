package com.fleet.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modelo de datos para mensajes de alerta de temperatura.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemperatureMessage {

    private String vehicleId;
    private String timestamp;
    private double temperatura;
    private String unidad;
    private double humedad;
    private boolean alerta;
    private double rango_min;
    private double rango_max;
    private String priority;
    private String bridge_timestamp;
    private String source_protocol;
    private String target_protocol;
    private String message_type;

    public TemperatureMessage() {}

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public double getHumedad() { return humedad; }
    public void setHumedad(double humedad) { this.humedad = humedad; }

    public boolean isAlerta() { return alerta; }
    public void setAlerta(boolean alerta) { this.alerta = alerta; }

    public double getRango_min() { return rango_min; }
    public void setRango_min(double rango_min) { this.rango_min = rango_min; }

    public double getRango_max() { return rango_max; }
    public void setRango_max(double rango_max) { this.rango_max = rango_max; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getBridge_timestamp() { return bridge_timestamp; }
    public void setBridge_timestamp(String bt) { this.bridge_timestamp = bt; }

    public String getSource_protocol() { return source_protocol; }
    public void setSource_protocol(String sp) { this.source_protocol = sp; }

    public String getTarget_protocol() { return target_protocol; }
    public void setTarget_protocol(String tp) { this.target_protocol = tp; }

    public String getMessage_type() { return message_type; }
    public void setMessage_type(String mt) { this.message_type = mt; }

    @Override
    public String toString() {
        return "TemperatureMessage{vehicleId='" + vehicleId +
               "', temp=" + temperatura + "°C, alerta=" + alerta + "}";
    }
}
