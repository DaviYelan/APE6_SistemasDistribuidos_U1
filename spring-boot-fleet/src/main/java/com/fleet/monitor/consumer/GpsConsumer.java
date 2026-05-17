package com.fleet.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.entity.GpsTelemetry;
import com.fleet.monitor.model.GpsMessage;
import com.fleet.monitor.repository.GpsTelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor RabbitMQ para datos de telemetria GPS.
 * Escucha la cola 'cola.gps.telemetria' y persiste los datos en H2.
 */
@Component
public class GpsConsumer {

    private static final Logger log = LoggerFactory.getLogger(GpsConsumer.class);
    private final GpsTelemetryRepository gpsRepository;
    private final ObjectMapper objectMapper;

    public GpsConsumer(GpsTelemetryRepository gpsRepository) {
        this.gpsRepository = gpsRepository;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = RabbitMQConfig.GPS_QUEUE)
    public void consumeGps(String message) {
        try {
            log.info("[GPS Consumer] Mensaje recibido: {}", message);

            // Deserializar el JSON
            GpsMessage gpsMsg = objectMapper.readValue(message, GpsMessage.class);

            // Crear entidad y persistir
            GpsTelemetry entity = new GpsTelemetry();
            entity.setVehicleId(gpsMsg.getVehicleId());
            entity.setSensorTimestamp(gpsMsg.getTimestamp());
            entity.setLat(gpsMsg.getLat());
            entity.setLng(gpsMsg.getLng());
            entity.setSpeed(gpsMsg.getSpeed());
            entity.setHeading(gpsMsg.getHeading());
            entity.setRuta(gpsMsg.getRuta());

            gpsRepository.save(entity);
            log.info("[GPS Consumer] Datos almacenados para vehiculo: {}", gpsMsg.getVehicleId());

        } catch (Exception e) {
            log.error("[GPS Consumer] Error procesando mensaje: {}", e.getMessage(), e);
        }
    }
}
