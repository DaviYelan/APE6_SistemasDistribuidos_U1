package com.fleet.monitor.consumer;

import com.fleet.monitor.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de notificaciones.
 * En un sistema real, este servicio enviaria emails, SMS o push notifications.
 * Aqui simplemente registra la notificacion en logs.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(String message) {
        log.info("========================================");
        log.info("[Notification] NUEVA NOTIFICACION:");
        log.info("[Notification] {}", message);
        log.info("[Notification] (En produccion: enviar email/SMS)");
        log.info("========================================");
    }
}
