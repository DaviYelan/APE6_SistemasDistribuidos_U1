"""
Servicio Puente: MQTT -> RabbitMQ
==================================
Consume mensajes del broker MQTT (Mosquitto) y los republica
en RabbitMQ usando el protocolo AMQP, aplicando transformacion
y enriquecimiento de datos.

Routing Keys:
  - gps.routing       -> cola.gps.telemetria
  - temp.alert        -> cola.alertas.temperatura
  - fuel.routing      -> cola.combustible.nivel
"""

import json
import time
import paho.mqtt.client as mqtt
import pika
from datetime import datetime

# ── Configuracion MQTT ──
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_CLIENT_ID = "bridge_mqtt_rabbitmq"

# ── Configuracion RabbitMQ ──
RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USER = "admin"
RABBITMQ_PASS = "admin123"
EXCHANGE_NAME = "exchange.fleet"

# ── Mapeo de topics MQTT a routing keys RabbitMQ ──
ROUTING_MAP = {
    "gps":          "gps.routing",
    "temperatura":  "temp.alert",
    "combustible":  "fuel.routing",
}

# ── Conexion RabbitMQ (global) ──
rabbitmq_connection = None
rabbitmq_channel = None


def conectar_rabbitmq():
    """Establece conexion con RabbitMQ y declara el exchange."""
    global rabbitmq_connection, rabbitmq_channel

    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials
    )

    rabbitmq_connection = pika.BlockingConnection(parameters)
    rabbitmq_channel = rabbitmq_connection.channel()

    # Declarar el exchange de tipo direct
    rabbitmq_channel.exchange_declare(
        exchange=EXCHANGE_NAME,
        exchange_type='direct',
        durable=True
    )

    # Declarar las colas y sus bindings
    colas = {
        "cola.gps.telemetria":      "gps.routing",
        "cola.alertas.temperatura": "temp.alert",
        "cola.combustible.nivel":   "fuel.routing",
        "cola.notificaciones":      "notification.routing",
    }

    for cola, routing_key in colas.items():
        rabbitmq_channel.queue_declare(queue=cola, durable=True)
        rabbitmq_channel.queue_bind(
            exchange=EXCHANGE_NAME,
            queue=cola,
            routing_key=routing_key
        )

    print("[BRIDGE] Conectado a RabbitMQ. Exchange y colas configurados.")


def publicar_en_rabbitmq(routing_key, mensaje):
    """Publica un mensaje en RabbitMQ con la routing key correspondiente."""
    global rabbitmq_channel

    try:
        rabbitmq_channel.basic_publish(
            exchange=EXCHANGE_NAME,
            routing_key=routing_key,
            body=json.dumps(mensaje),
            properties=pika.BasicProperties(
                delivery_mode=2,  # Mensaje persistente
                content_type='application/json'
            )
        )
    except Exception as e:
        print(f"[BRIDGE] Error publicando en RabbitMQ: {e}")
        # Reconectar
        conectar_rabbitmq()
        rabbitmq_channel.basic_publish(
            exchange=EXCHANGE_NAME,
            routing_key=routing_key,
            body=json.dumps(mensaje),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type='application/json'
            )
        )


def enriquecer_mensaje(tipo, datos):
    """
    Enriquece el mensaje con metadatos adicionales antes de
    enviarlo a RabbitMQ (transformacion de datos).
    """
    datos["bridge_timestamp"] = datetime.now().isoformat()
    datos["source_protocol"] = "MQTT"
    datos["target_protocol"] = "AMQP"
    datos["message_type"] = tipo

    # Agregar prioridad segun el tipo de dato
    if tipo == "temperatura" and datos.get("alerta", False):
        datos["priority"] = "HIGH"
    elif tipo == "combustible" and datos.get("alerta", False):
        datos["priority"] = "HIGH"
    else:
        datos["priority"] = "NORMAL"

    return datos


def on_connect(client, userdata, flags, rc):
    """Callback de conexion MQTT."""
    if rc == 0:
        print("[BRIDGE] Conectado al broker MQTT")
        client.subscribe("flota/+/gps", qos=1)
        client.subscribe("flota/+/temperatura", qos=1)
        client.subscribe("flota/+/combustible", qos=1)
        print("[BRIDGE] Suscrito a todos los topics de la flota")
    else:
        print(f"[BRIDGE] Error MQTT: {rc}")


def on_message(client, userdata, msg):
    """
    Callback de mensaje MQTT.
    Transforma y reenvia el mensaje a RabbitMQ.
    """
    try:
        topic = msg.topic
        datos = json.loads(msg.payload.decode())

        # Determinar el tipo de sensor
        partes = topic.split("/")
        tipo_sensor = partes[2] if len(partes) >= 3 else "desconocido"

        # Obtener routing key
        routing_key = ROUTING_MAP.get(tipo_sensor)
        if not routing_key:
            print(f"[BRIDGE] Topic desconocido: {topic}")
            return

        # Enriquecer el mensaje
        mensaje_enriquecido = enriquecer_mensaje(tipo_sensor, datos)

        # Publicar en RabbitMQ
        publicar_en_rabbitmq(routing_key, mensaje_enriquecido)

        prioridad = mensaje_enriquecido["priority"]
        print(f"[BRIDGE] MQTT -> RabbitMQ | {topic} -> {routing_key} "
              f"[{prioridad}]")

    except Exception as e:
        print(f"[BRIDGE] Error procesando mensaje: {e}")


def main():
    """Funcion principal del servicio puente."""
    print("=" * 60)
    print("  SERVICIO PUENTE: MQTT -> RabbitMQ")
    print("  Sistema de Monitoreo de Flota Logistica")
    print("=" * 60)

    # Conectar a RabbitMQ
    print("[BRIDGE] Conectando a RabbitMQ...")
    conectar_rabbitmq()

    # Conectar a MQTT
    mqtt_client = mqtt.Client(MQTT_CLIENT_ID)
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message

    print("[BRIDGE] Conectando al broker MQTT...")
    mqtt_client.connect(MQTT_BROKER, MQTT_PORT, 60)

    print("[BRIDGE] Puente activo. Reenviando mensajes...")
    print("-" * 60)

    try:
        mqtt_client.loop_forever()
    except KeyboardInterrupt:
        print("\n[BRIDGE] Puente detenido.")
        mqtt_client.disconnect()
        if rabbitmq_connection:
            rabbitmq_connection.close()


if __name__ == "__main__":
    main()
