package com.fleet.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicacion Fleet Monitor.
 * Sistema de Monitoreo de Flota Logistica con RabbitMQ y MQTT.
 */
@SpringBootApplication
public class FleetMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetMonitorApplication.class, args);
        System.out.println("============================================");
        System.out.println("  Fleet Monitor System - Iniciado");
        System.out.println("  API REST: http://localhost:8080/api/fleet");
        System.out.println("  H2 Console: http://localhost:8080/h2-console");
        System.out.println("============================================");
    }
}
