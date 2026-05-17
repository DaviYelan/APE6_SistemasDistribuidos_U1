package com.fleet.monitor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ.
 * Define el exchange, las colas y los bindings para el sistema de flota.
 *
 * Exchange: exchange.fleet (tipo Direct)
 * Colas:
 *   - cola.gps.telemetria      (routing key: gps.routing)
 *   - cola.alertas.temperatura  (routing key: temp.alert)
 *   - cola.combustible.nivel    (routing key: fuel.routing)
 *   - cola.notificaciones       (routing key: notification.routing)
 */
@Configuration
public class RabbitMQConfig {

    // ── Nombres de colas ──
    public static final String GPS_QUEUE = "cola.gps.telemetria";
    public static final String TEMP_ALERT_QUEUE = "cola.alertas.temperatura";
    public static final String FUEL_QUEUE = "cola.combustible.nivel";
    public static final String NOTIFICATION_QUEUE = "cola.notificaciones";

    // ── Exchange ──
    public static final String FLEET_EXCHANGE = "exchange.fleet";

    // ── Routing Keys ──
    public static final String GPS_ROUTING_KEY = "gps.routing";
    public static final String TEMP_ROUTING_KEY = "temp.alert";
    public static final String FUEL_ROUTING_KEY = "fuel.routing";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing";

    // ── Exchange Direct ──
    @Bean
    public DirectExchange fleetExchange() {
        return new DirectExchange(FLEET_EXCHANGE, true, false);
    }

    // ── Colas durables ──
    @Bean
    public Queue gpsQueue() {
        return new Queue(GPS_QUEUE, true);
    }

    @Bean
    public Queue tempAlertQueue() {
        return new Queue(TEMP_ALERT_QUEUE, true);
    }

    @Bean
    public Queue fuelQueue() {
        return new Queue(FUEL_QUEUE, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    // ── Bindings (enlace cola <-> exchange via routing key) ──
    @Bean
    public Binding gpsBinding(Queue gpsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(gpsQueue)
                .to(fleetExchange)
                .with(GPS_ROUTING_KEY);
    }

    @Bean
    public Binding tempBinding(Queue tempAlertQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(tempAlertQueue)
                .to(fleetExchange)
                .with(TEMP_ROUTING_KEY);
    }

    @Bean
    public Binding fuelBinding(Queue fuelQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(fuelQueue)
                .to(fleetExchange)
                .with(FUEL_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(fleetExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // ── Converter: SimpleMessageConverter para recibir JSON como String ──
    @Bean
    public MessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }
}
