package com.fleet.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.monitor.config.RabbitMQConfig;
import com.fleet.monitor.entity.FuelLevel;
import com.fleet.monitor.entity.Notification;
import com.fleet.monitor.entity.TemperatureAlert;
import com.fleet.monitor.model.FuelMessage;
import com.fleet.monitor.model.TemperatureMessage;
import com.fleet.monitor.repository.FuelLevelRepository;
import com.fleet.monitor.repository.NotificationRepository;
import com.fleet.monitor.repository.TemperatureAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumidor de alertas de temperatura y combustible.
 * Persiste los datos y reenvia alertas criticas a la cola de notificaciones.
 * Demuestra el patron de enrutamiento y competing consumers.
 */
@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);
    private final RabbitTemplate rabbitTemplate;
    private final TemperatureAlertRepository tempRepository;
    private final FuelLevelRepository fuelRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public AlertConsumer(RabbitTemplate rabbitTemplate,
                         TemperatureAlertRepository tempRepository,
                         FuelLevelRepository fuelRepository,
                         NotificationRepository notificationRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.tempRepository = tempRepository;
        this.fuelRepository = fuelRepository;
        this.notificationRepository = notificationRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Consume alertas de temperatura desde cola.alertas.temperatura.
     * Si la alerta es critica, la reenvia a cola.notificaciones.
     */
    @RabbitListener(queues = RabbitMQConfig.TEMP_ALERT_QUEUE)
    public void consumeTempAlert(String message) {
        try {
            log.warn("[Alert Consumer] ALERTA TEMPERATURA: {}", message);

            TemperatureMessage tempMsg = objectMapper.readValue(message, TemperatureMessage.class);

            // Persistir en H2
            TemperatureAlert entity = new TemperatureAlert();
            entity.setVehicleId(tempMsg.getVehicleId());
            entity.setSensorTimestamp(tempMsg.getTimestamp());
            entity.setTemperatura(tempMsg.getTemperatura());
            entity.setHumedad(tempMsg.getHumedad());
            entity.setAlerta(tempMsg.isAlerta());
            entity.setPriority(tempMsg.getPriority());
            tempRepository.save(entity);

            // Si es alerta critica, reenviar a cola de notificaciones
            if (tempMsg.isAlerta()) {
                String notifMsg = String.format(
                    "{\"vehicleId\":\"%s\",\"type\":\"TEMPERATURA\",\"message\":\"Temperatura fuera de rango: %.1f°C\",\"priority\":\"%s\",\"timestamp\":\"%s\"}",
                    tempMsg.getVehicleId(), tempMsg.getTemperatura(),
                    tempMsg.getPriority(), tempMsg.getTimestamp()
                );
                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, notifMsg);
                log.warn("[Alert Consumer] Notificacion enviada para {}", tempMsg.getVehicleId());

                // Tambien persistir la notificacion
                Notification notif = new Notification();
                notif.setVehicleId(tempMsg.getVehicleId());
                notif.setNotificationType("TEMPERATURA");
                notif.setMessage("Temperatura fuera de rango: " + tempMsg.getTemperatura() + "°C");
                notif.setPriority(tempMsg.getPriority());
                notificationRepository.save(notif);
            }

        } catch (Exception e) {
            log.error("[Alert Consumer] Error procesando alerta temperatura: {}", e.getMessage(), e);
        }
    }

    /**
     * Consume alertas de combustible desde cola.combustible.nivel.
     * Si el nivel es bajo, reenvia a cola.notificaciones.
     */
    @RabbitListener(queues = RabbitMQConfig.FUEL_QUEUE)
    public void consumeFuelAlert(String message) {
        try {
            log.warn("[Alert Consumer] ALERTA COMBUSTIBLE: {}", message);

            FuelMessage fuelMsg = objectMapper.readValue(message, FuelMessage.class);

            // Persistir en H2
            FuelLevel entity = new FuelLevel();
            entity.setVehicleId(fuelMsg.getVehicleId());
            entity.setSensorTimestamp(fuelMsg.getTimestamp());
            entity.setNivelCombustible(fuelMsg.getNivelCombustible());
            entity.setConsumoReciente(fuelMsg.getConsumoReciente());
            entity.setAlerta(fuelMsg.isAlerta());
            entity.setPriority(fuelMsg.getPriority());
            fuelRepository.save(entity);

            // Si es alerta critica, reenviar a cola de notificaciones
            if (fuelMsg.isAlerta()) {
                String notifMsg = String.format(
                    "{\"vehicleId\":\"%s\",\"type\":\"COMBUSTIBLE\",\"message\":\"Nivel de combustible bajo: %.1f%%\",\"priority\":\"%s\",\"timestamp\":\"%s\"}",
                    fuelMsg.getVehicleId(), fuelMsg.getNivelCombustible(),
                    fuelMsg.getPriority(), fuelMsg.getTimestamp()
                );
                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, notifMsg);
                log.warn("[Alert Consumer] Notificacion enviada para {}", fuelMsg.getVehicleId());

                Notification notif = new Notification();
                notif.setVehicleId(fuelMsg.getVehicleId());
                notif.setNotificationType("COMBUSTIBLE");
                notif.setMessage("Nivel de combustible bajo: " + fuelMsg.getNivelCombustible() + "%");
                notif.setPriority(fuelMsg.getPriority());
                notificationRepository.save(notif);
            }

        } catch (Exception e) {
            log.error("[Alert Consumer] Error procesando alerta combustible: {}", e.getMessage(), e);
        }
    }
}
