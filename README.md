# Sistema de Monitoreo de Flota Logística

## Mensajería Distribuida con RabbitMQ y MQTT

**Asignatura:** Sistemas Distribuidos — 6to Ciclo  
**Universidad Nacional de Loja — FEIRNNR — Carrera de Computación**  
**Práctica Nro. 6/Unidad 1**
**Por: Eberson Guayllas, Joel Tapia y Luis Armijos**

---

## Tabla de Contenidos

1. [Descripción del Sistema](#descripción-del-sistema)
2. [Arquitectura](#arquitectura)
3. [Requisitos Previos](#requisitos-previos)
4. [Instalación de Herramientas](#instalación-de-herramientas)
5. [Estructura del Proyecto](#estructura-del-proyecto)
6. [Guía de Ejecución Paso a Paso](#guía-de-ejecución-paso-a-paso)
7. [Endpoints de la API REST](#endpoints-de-la-api-rest)
8. [Pruebas con Postman y curl](#pruebas-con-postman-y-curl)
9. [Verificación del Funcionamiento](#verificación-del-funcionamiento)
10. [Explicación de la Arquitectura](#explicación-de-la-arquitectura)
11. [Flujo Completo de Mensajes](#flujo-completo-de-mensajes)
12. [Patrones de Mensajería Aplicados](#patrones-de-mensajería-aplicados)
13. [Exchanges, Colas, Routing Keys y Topics](#exchanges-colas-routing-keys-y-topics)
14. [Seguridad Implementada](#seguridad-implementada)
15. [Preguntas de Control](#preguntas-de-control)
16. [Solución de Problemas](#solución-de-problemas)

---

## Descripción del Sistema

Este proyecto simula un **Sistema de Monitoreo de Flota Logística** para una empresa que necesita rastrear en tiempo real sus vehículos de reparto. Cada vehículo tiene sensores IoT que envían:

- **Posición GPS** (latitud, longitud, velocidad)
- **Temperatura** del compartimento de carga (cadena de frío)
- **Nivel de combustible**

El sistema combina dos protocolos de mensajería:

- **MQTT** (capa IoT): protocolo ligero para sensores que publican telemetría
- **RabbitMQ/AMQP** (capa empresarial): broker robusto para procesamiento de alertas y microservicios

Un **servicio puente en Python** conecta ambos mundos: consume de MQTT y republica en RabbitMQ, enriqueciendo los datos en el proceso.

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                   CAPA IoT (SENSORES)                       │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────┐         │
│  │Sensor GPS│  │Sensor Temp.  │  │Sensor Combus. │         │
│  │(Python)  │  │(Python)      │  │(Python)       │         │
│  └────┬─────┘  └──────┬───────┘  └───────┬───────┘         │
│       │               │                  │                  │
│       └───────────────┼──────────────────┘                  │
│                       │ MQTT (puerto 1883)                  │
└───────────────────────┼─────────────────────────────────────┘
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              BROKER MQTT (MOSQUITTO)                         │
│  Topics:                                                     │
│    flota/{vehiculo_id}/gps                                  │
│    flota/{vehiculo_id}/temperatura                          │
│    flota/{vehiculo_id}/combustible                          │
│                                                              │
│  ┌────────────────────┐  ┌──────────────┐                   │
│  │ Python Subscriber  │  │ SQLite DB    │                   │
│  │ (almacena datos)   │──│ (persistencia│                   │
│  └────────────────────┘  │  local)      │                   │
│                          └──────────────┘                   │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           SERVICIO PUENTE (Python)                           │
│  - Consume de MQTT                                          │
│  - Transforma y enriquece datos                             │
│  - Publica en RabbitMQ via AMQP                             │
└───────────────────────┬─────────────────────────────────────┘
                        │ AMQP (puerto 5672)
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           BROKER RabbitMQ                                    │
│  Exchange: exchange.fleet (tipo Direct)                      │
│                                                              │
│  ┌──────────────────┐ ┌──────────────────┐                  │
│  │cola.gps.telemetr.│ │cola.alertas.temp │                  │
│  │(gps.routing)     │ │(temp.alert)      │                  │
│  └──────────────────┘ └──────────────────┘                  │
│  ┌──────────────────┐ ┌──────────────────┐                  │
│  │cola.combustible  │ │cola.notificacion.│                  │
│  │(fuel.routing)    │ │(notif.routing)   │                  │
│  └──────────────────┘ └──────────────────┘                  │
└───────────────────────┬─────────────────────────────────────┘
                        │ Spring AMQP Consumers
                        ▼
┌─────────────────────────────────────────────────────────────┐
│        MICROSERVICIOS SPRING BOOT                           │
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────────┐  │
│  │GPS Consumer  │  │Alert Consumer │  │Notification     │  │
│  │(telemetría)  │  │(temp + fuel)  │  │Consumer         │  │
│  └──────────────┘  └───────────────┘  └─────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────┐           │
│  │          API REST (FleetController)          │           │
│  │  /api/fleet/status                           │           │
│  │  /api/fleet/vehicles                         │           │
│  │  /api/fleet/vehicle/{id}/telemetria          │           │
│  │  /api/fleet/alerts                           │           │
│  │  /api/fleet/notifications                    │           │
│  └──────────────────────────────────────────────┘           │
│                                                              │
│  ┌──────────────┐                                           │
│  │  H2 Database │  (Base de datos en memoria)               │
│  └──────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Requisitos Previos

| Software          | Versión Mínima | Enlace de Descarga                                      |
|-------------------|----------------|---------------------------------------------------------|
| Java JDK          | 17             | https://adoptium.net/temurin/releases/?version=17       |
| Python            | 3.8+           | https://www.python.org/downloads/                       |
| Docker Desktop    | 4.x            | https://www.docker.com/products/docker-desktop/         |
| Git               | 2.x            | https://git-scm.com/downloads                           |
| Maven (opcional)  | 3.9+           | https://maven.apache.org/download.cgi                   |
| Postman (opcional)| Última         | https://www.postman.com/downloads/                      |
| VS Code o IntelliJ| Última         | https://code.visualstudio.com/ o https://www.jetbrains.com/idea/ |

---

## Instalación de Herramientas

### 1. Java JDK 17

1. Descargue Temurin JDK 17 de https://adoptium.net/temurin/releases/?version=17
2. Ejecute el instalador `.msi` (Windows)
3. Marque la opción "Set JAVA_HOME variable"
4. Verifique:
   ```cmd
   java -version
   javac -version
   ```

### 2. Python 3

1. Descargue Python de https://www.python.org/downloads/
2. Al instalar, marque **"Add Python to PATH"**
3. Verifique:
   ```cmd
   python --version
   pip --version
   ```

### 3. Docker Desktop

1. Descargue Docker Desktop de https://www.docker.com/products/docker-desktop/
2. Instale y reinicie el equipo si se lo pide
3. Abra Docker Desktop y espere a que inicie
4. Verifique:
   ```cmd
   docker --version
   docker-compose --version
   ```

### 4. Instalar dependencias Python

```cmd
cd mqtt-python
pip install -r requirements.txt
```

Esto instala:
- `paho-mqtt` — Cliente MQTT para Python
- `pika` — Cliente AMQP (RabbitMQ) para Python

---

## Estructura del Proyecto

```
APE6_Distribuidos_U1/
│
├── docker/                           # Configuración Docker
│   ├── docker-compose.yml            # Mosquitto + RabbitMQ
│   └── mosquitto/
│       └── mosquitto.conf            # Configuración Mosquitto
│
├── mqtt-python/                      # Componentes Python
│   ├── requirements.txt              # Dependencias pip
│   ├── simuladores/
│   │   ├── simulador_gps.py          # Simulador sensor GPS
│   │   ├── simulador_temperatura.py  # Simulador sensor temperatura
│   │   └── simulador_combustible.py  # Simulador sensor combustible
│   ├── suscriptores/
│   │   └── suscriptor_sqlite.py      # Suscriptor MQTT → SQLite
│   └── puente/
│       └── bridge_mqtt_rabbitmq.py   # Puente MQTT → RabbitMQ
│
├── spring-boot-fleet/                # Microservicios Java
│   ├── pom.xml                       # Dependencias Maven
│   └── src/main/
│       ├── java/com/fleet/monitor/
│       │   ├── FleetMonitorApplication.java  # Main
│       │   ├── config/
│       │   │   └── RabbitMQConfig.java       # Exchanges, colas, bindings
│       │   ├── model/
│       │   │   ├── GpsMessage.java           # DTO GPS
│       │   │   ├── TemperatureMessage.java   # DTO Temperatura
│       │   │   └── FuelMessage.java          # DTO Combustible
│       │   ├── entity/
│       │   │   ├── GpsTelemetry.java         # Entidad JPA GPS
│       │   │   ├── TemperatureAlert.java     # Entidad JPA Temperatura
│       │   │   ├── FuelLevel.java            # Entidad JPA Combustible
│       │   │   └── Notification.java         # Entidad JPA Notificación
│       │   ├── repository/
│       │   │   ├── GpsTelemetryRepository.java
│       │   │   ├── TemperatureAlertRepository.java
│       │   │   ├── FuelLevelRepository.java
│       │   │   └── NotificationRepository.java
│       │   ├── consumer/
│       │   │   ├── GpsConsumer.java          # Consumidor GPS
│       │   │   ├── AlertConsumer.java        # Consumidor alertas
│       │   │   └── NotificationConsumer.java # Consumidor notificaciones
│       │   └── controller/
│       │       └── FleetController.java      # API REST
│       └── resources/
│           └── application.properties        # Configuración Spring
│
├── scripts/                          # Scripts de ejecución (Windows)
│   ├── 01_iniciar_docker.bat
│   ├── 02_instalar_python_deps.bat
│   ├── 03_iniciar_puente.bat
│   ├── 04_iniciar_spring_boot.bat
│   ├── 05_iniciar_simuladores.bat
│   ├── 06_iniciar_suscriptor.bat
│   ├── 07_probar_api.bat
│   └── 08_detener_todo.bat
│
└── README.md                         # Este archivo
```

---

## Guía de Ejecución Paso a Paso

Necesitará **5 ventanas de terminal** abiertas simultáneamente.

### Paso 1: Iniciar Docker (Mosquitto + RabbitMQ)

```cmd
cd docker
docker-compose up -d
```

Espere 15 segundos y verifique:
```cmd
docker ps
```

Debe ver dos contenedores: `mosquitto_broker` y `rabbitmq_broker`.

Acceda a la interfaz web de RabbitMQ: http://localhost:15672  
**Usuario:** `admin` | **Contraseña:** `admin123`

### Paso 2: Instalar dependencias Python

```cmd
cd mqtt-python
pip install -r requirements.txt
```

### Paso 3: Iniciar el Servicio Puente (Terminal 1)

```cmd
cd mqtt-python/puente
python bridge_mqtt_rabbitmq.py
```

Verá: `[BRIDGE] Puente activo. Reenviando mensajes...`

### Paso 4: Iniciar Spring Boot (Terminal 2)

```cmd
cd spring-boot-fleet
mvnw.cmd spring-boot:run
```

En Linux/Mac:
```bash
./mvnw spring-boot:run
```

Si no tiene Maven wrapper, use Maven directo:
```cmd
mvn spring-boot:run
```

Espere hasta ver: `Fleet Monitor System - Iniciado`

### Paso 5: Iniciar el Suscriptor SQLite (Terminal 3) — Opcional

```cmd
cd mqtt-python/suscriptores
python suscriptor_sqlite.py
```

### Paso 6: Iniciar los Simuladores IoT (Terminales 4, 5, 6)

Terminal 4 — GPS:
```cmd
cd mqtt-python/simuladores
python simulador_gps.py
```

Terminal 5 — Temperatura:
```cmd
cd mqtt-python/simuladores
python simulador_temperatura.py
```

Terminal 6 — Combustible:
```cmd
cd mqtt-python/simuladores
python simulador_combustible.py
```

O use el script que abre todo:
```cmd
scripts\05_iniciar_simuladores.bat
```

### Paso 7: Probar la API REST

```cmd
curl http://localhost:8081/api/fleet/status
curl http://localhost:8081/api/fleet/vehicles
curl http://localhost:8081/api/fleet/vehicle/VH-001/telemetria
curl http://localhost:8081/api/fleet/alerts
curl http://localhost:8081/api/fleet/notifications
```

---

## Endpoints de la API REST

| Método | Endpoint                              | Descripción                               |
|--------|---------------------------------------|-------------------------------------------|
| GET    | `/api/fleet/status`                   | Estado general de la flota                |
| GET    | `/api/fleet/vehicles`                 | Lista de vehículos con último estado      |
| GET    | `/api/fleet/vehicle/{id}/telemetria`  | Telemetría completa de un vehículo        |
| GET    | `/api/fleet/vehicle/{id}/gps`         | Últimos 10 registros GPS                  |
| GET    | `/api/fleet/alerts`                   | Alertas activas (temperatura + combustible)|
| GET    | `/api/fleet/notifications`            | Notificaciones no leídas                  |

---

## Pruebas con Postman y curl

### Con curl

```cmd
REM Estado de la flota
curl http://localhost:8081/api/fleet/status

REM Telemetría del vehículo VH-001
curl http://localhost:8081/api/fleet/vehicle/VH-001/telemetria

REM Alertas activas
curl http://localhost:8081/api/fleet/alerts
```

### Con Postman

1. Abra Postman
2. Cree una nueva colección llamada "Fleet Monitor"
3. Agregue las siguientes peticiones GET:
   - `http://localhost:8081/api/fleet/status`
   - `http://localhost:8081/api/fleet/vehicles`
   - `http://localhost:8081/api/fleet/vehicle/VH-001/telemetria`
   - `http://localhost:8081/api/fleet/vehicle/VH-002/telemetria`
   - `http://localhost:8081/api/fleet/vehicle/VH-003/telemetria`
   - `http://localhost:8081/api/fleet/alerts`
   - `http://localhost:8081/api/fleet/notifications`

### Consola H2 (Base de datos)

Acceda a: http://localhost:8081/h2-console

- JDBC URL: `jdbc:h2:mem:fleetdb`
- User: `sa`
- Password: (vacío)

Consultas SQL de prueba:
```sql
SELECT * FROM GPS_TELEMETRY ORDER BY CREATED_AT DESC;
SELECT * FROM TEMPERATURE_ALERTS WHERE ALERTA = TRUE;
SELECT * FROM FUEL_LEVELS WHERE ALERTA = TRUE;
SELECT * FROM NOTIFICATIONS ORDER BY CREATED_AT DESC;
```

---

## Verificación del Funcionamiento

### Verificar MQTT (Mosquitto)

En el terminal del puente o suscriptor, debe ver mensajes como:
```
[BRIDGE] MQTT -> RabbitMQ | flota/VH-001/gps -> gps.routing [NORMAL]
[BRIDGE] MQTT -> RabbitMQ | flota/VH-002/temperatura -> temp.alert [HIGH]
```

### Verificar RabbitMQ

1. Abra http://localhost:15672 (admin/admin123)
2. En la pestaña **Queues**, verifique que existan:
   - `cola.gps.telemetria`
   - `cola.alertas.temperatura`
   - `cola.combustible.nivel`
   - `cola.notificaciones`
3. En la pestaña **Exchanges**, verifique que exista `exchange.fleet`

### Verificar Spring Boot

En el terminal de Spring Boot, debe ver logs como:
```
[GPS Consumer] Datos almacenados para vehiculo: VH-001
[Alert Consumer] ALERTA TEMPERATURA: {...}
[Notification] NUEVA NOTIFICACION: {...}
```

### Verificar la API REST

```cmd
curl http://localhost:8081/api/fleet/status
```

Debe retornar un JSON con contadores de registros mayores a 0.

---

## Explicación de la Arquitectura

### Componentes del Sistema

**1. Simuladores de Sensores IoT (Python)**
Tres scripts Python que simulan sensores embebidos en los vehículos VH-001, VH-002 y VH-003. Cada uno publica datos periódicamente via MQTT con diferentes intervalos: GPS cada 5 segundos, temperatura cada 7 segundos y combustible cada 10 segundos. Los simuladores generan datos aleatorios realistas, incluyendo un 20% de datos fuera de rango para disparar alertas.

**2. Broker MQTT — Mosquitto**
Servidor de mensajería ligero que opera con el patrón publicador/suscriptor. Los sensores publican en topics jerárquicos (`flota/{vehiculo_id}/{tipo_sensor}`). Escucha en el puerto 1883 para MQTT y 9001 para WebSocket.

**3. Suscriptor MQTT con SQLite (Python)**
Un consumidor Python que se suscribe a todos los topics usando wildcards (`flota/+/gps`) y almacena los datos de telemetría en una base de datos SQLite local. Esto proporciona persistencia independiente en la capa IoT.

**4. Servicio Puente MQTT→RabbitMQ (Python)**
El componente clave que integra ambos protocolos. Consume mensajes de MQTT, los enriquece con metadatos (timestamp del puente, protocolo de origen, prioridad) y los republica en RabbitMQ usando routing keys específicas a través de un exchange de tipo Direct.

**5. Broker RabbitMQ**
Broker empresarial que implementa AMQP 0-9-1. Recibe los mensajes del puente a través del exchange `exchange.fleet` y los distribuye a cuatro colas según la routing key. Opera en el puerto 5672 (AMQP) y 15672 (interfaz web de administración).

**6. Microservicios Spring Boot**
Tres consumidores que procesan mensajes de RabbitMQ:
- **GpsConsumer**: persiste datos GPS en H2
- **AlertConsumer**: evalúa alertas de temperatura y combustible, genera notificaciones
- **NotificationConsumer**: registra notificaciones (en producción enviaría emails/SMS)

**7. API REST (FleetController)**
Expone endpoints HTTP para consultar el estado de la flota, telemetría individual, alertas y notificaciones. Utiliza Spring Data JPA para consultar la base de datos H2.

---

## Flujo Completo de Mensajes

1. El **simulador GPS** genera coordenadas para VH-001 y publica en `flota/VH-001/gps` via MQTT (QoS 1)

2. **Mosquitto** recibe el mensaje y lo entrega a todos los suscriptores del topic

3. El **suscriptor SQLite** recibe una copia y la guarda en la base de datos local

4. El **servicio puente** recibe otra copia, la enriquece con metadatos y la publica en RabbitMQ: exchange `exchange.fleet` con routing key `gps.routing`

5. RabbitMQ enruta el mensaje a `cola.gps.telemetria` (porque el binding conecta esa cola con `gps.routing`)

6. El **GpsConsumer** de Spring Boot recibe el mensaje, lo deserializa y lo persiste en H2

7. Mientras tanto, el **simulador de temperatura** publica una lectura de 12°C (fuera del rango 2-8°C). El puente la etiqueta como prioridad HIGH y la envía a RabbitMQ con routing key `temp.alert`

8. El **AlertConsumer** recibe la alerta, la persiste, y reenvía una notificación a `cola.notificaciones`

9. El **NotificationConsumer** recibe la notificación y la registra

10. Un operador consulta `GET /api/fleet/status` y ve el resumen completo con las alertas activas

---

## Patrones de Mensajería Aplicados

### Pub/Sub (Publicador/Suscriptor)
Usado en MQTT: los sensores publican sin conocer quién consume. Múltiples suscriptores (SQLite, puente) reciben los mismos mensajes. En RabbitMQ, si se usara un exchange de tipo Fanout, se lograría el mismo efecto.

### Routing (Enrutamiento Directo)
Usado en RabbitMQ con el exchange Direct: cada mensaje se enruta a la cola correcta según su routing key. `gps.routing` va solo a `cola.gps.telemetria`, `temp.alert` va solo a `cola.alertas.temperatura`.

### Competing Consumers (Consumidores Competidores)
Si se desplegaran múltiples instancias de GpsConsumer escuchando la misma cola, RabbitMQ distribuiría los mensajes entre ellas usando round-robin, permitiendo balanceo de carga sin configuración adicional.

### Message Transformation
El servicio puente aplica transformación: enriquece los mensajes con metadatos, clasifica prioridades y cambia el formato de routing MQTT a routing keys AMQP.

---

## Exchanges, Colas, Routing Keys y Topics

### Topics MQTT (Mosquitto)

| Topic                            | Publicador          | Datos                    |
|----------------------------------|---------------------|--------------------------|
| `flota/VH-001/gps`              | Simulador GPS       | lat, lng, speed, heading |
| `flota/VH-001/temperatura`      | Simulador Temp.     | temperatura, humedad     |
| `flota/VH-001/combustible`      | Simulador Fuel      | nivelCombustible         |
| `flota/+/gps`                   | (wildcard suscr.)   | Todos los GPS            |

### Exchange RabbitMQ

- **Nombre:** `exchange.fleet`
- **Tipo:** Direct
- **Durable:** Sí (sobrevive reinicios del broker)

### Colas y Bindings

| Cola                       | Routing Key           | Consumidor              |
|----------------------------|-----------------------|-------------------------|
| `cola.gps.telemetria`      | `gps.routing`         | GpsConsumer             |
| `cola.alertas.temperatura` | `temp.alert`          | AlertConsumer           |
| `cola.combustible.nivel`   | `fuel.routing`        | AlertConsumer           |
| `cola.notificaciones`      | `notification.routing`| NotificationConsumer    |

Todas las colas son **durables** (segundo parámetro `true` en `new Queue(name, true)`), lo que significa que sobreviven reinicios del broker.

---

## Seguridad Implementada

### MQTT (Mosquitto)
- Configuración actual: `allow_anonymous true` (desarrollo)
- En producción se debe agregar al `mosquitto.conf`:
  ```
  allow_anonymous false
  password_file /mosquitto/config/passwd
  ```
- Para TLS: configurar `certfile`, `cafile` y `keyfile`

### RabbitMQ
- Autenticación por usuario/contraseña (`admin`/`admin123`)
- Interfaz de administración protegida en puerto 15672
- En producción: crear usuarios con permisos específicos por vhost
- Para TLS: configurar certificados en `rabbitmq.conf`

### Spring Boot
- API REST con CORS habilitado (`@CrossOrigin`)
- En producción: agregar Spring Security con JWT

### Servicio Puente
- Credenciales de RabbitMQ configuradas como variables
- En producción: usar variables de entorno o vault de secretos

---

## Solución de Problemas

| Problema | Solución |
|----------|----------|
| Docker no inicia | Verifique que Docker Desktop esté ejecutándose |
| Puerto 5672 ocupado | `docker ps` y `docker stop` el contenedor conflictivo |
| Spring Boot no conecta a RabbitMQ | Espere 15 segundos después de iniciar Docker |
| Los simuladores no publican | Verifique que Mosquitto esté activo: `docker logs mosquitto_broker` |
| El puente no conecta | Inicie Docker primero, luego el puente |
| API retorna datos vacíos | Inicie los simuladores y espere 10-15 segundos |
| Error "Connection refused" | Verifique que todos los servicios estén corriendo |
| Maven no encontrado | Use `mvnw.cmd` (wrapper) o instale Maven globalmente |

### Importar en IntelliJ IDEA

1. File → Open → Seleccione la carpeta `spring-boot-fleet`
2. IntelliJ detectará el `pom.xml` automáticamente
3. Espere a que descargue las dependencias
4. Click derecho en `FleetMonitorApplication.java` → Run

### Importar en VS Code

1. Abra la carpeta `spring-boot-fleet`
2. Instale la extensión "Extension Pack for Java"
3. VS Code detectará el proyecto Maven
4. Use el panel "Spring Boot Dashboard" para ejecutar
