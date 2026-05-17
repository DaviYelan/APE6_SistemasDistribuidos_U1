"""
Simulador de Sensor de Temperatura para Flota Vehicular
========================================================
Publica datos de temperatura del compartimento de carga via MQTT.
Topics: flota/{vehiculo_id}/temperatura
Genera alertas cuando la temperatura sale del rango permitido (2-8 °C).
"""

import json
import time
import random
import paho.mqtt.client as mqtt
from datetime import datetime

# ── Configuracion del Broker MQTT ──
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
CLIENT_ID = "simulador_temperatura"

# ── Vehiculos de la flota ──
VEHICULOS = ["VH-001", "VH-002", "VH-003"]

# ── Umbrales de temperatura (cadena de frio) ──
TEMP_MIN = 2.0   # °C
TEMP_MAX = 8.0   # °C


def on_connect(client, userdata, flags, rc):
    """Callback cuando se conecta al broker MQTT."""
    if rc == 0:
        print("[TEMP] Conectado exitosamente al broker MQTT")
    else:
        print(f"[TEMP] Error de conexion. Codigo: {rc}")


def generar_datos_temperatura(vehiculo_id):
    """
    Genera datos simulados de temperatura.
    El 20% de las veces genera valores fuera de rango para disparar alertas.
    """
    # 80% normal, 20% fuera de rango
    if random.random() < 0.2:
        temperatura = round(random.choice([
            random.uniform(-2, 1),    # Muy fria
            random.uniform(9, 15)     # Muy caliente
        ]), 1)
        alerta = True
    else:
        temperatura = round(random.uniform(TEMP_MIN, TEMP_MAX), 1)
        alerta = False

    return {
        "vehicleId": vehiculo_id,
        "timestamp": datetime.now().isoformat(),
        "temperatura": temperatura,
        "unidad": "celsius",
        "humedad": round(random.uniform(40, 80), 1),
        "alerta": alerta,
        "rango_min": TEMP_MIN,
        "rango_max": TEMP_MAX
    }


def main():
    """Funcion principal del simulador de temperatura."""
    client = mqtt.Client(CLIENT_ID)
    client.on_connect = on_connect

    print("[TEMP] Conectando al broker MQTT...")
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()

    print("[TEMP] Simulador de temperatura iniciado. Publicando cada 7 segundos...")
    print(f"[TEMP] Rango permitido: {TEMP_MIN}°C - {TEMP_MAX}°C")
    print("-" * 60)

    try:
        while True:
            for vehiculo_id in VEHICULOS:
                datos = generar_datos_temperatura(vehiculo_id)
                topic = f"flota/{vehiculo_id}/temperatura"
                payload = json.dumps(datos)

                client.publish(topic, payload, qos=1)

                estado = " ALERTA" if datos["alerta"] else "✓ Normal"
                print(f"[TEMP] {topic} -> {datos['temperatura']}°C [{estado}]")

            print("-" * 60)
            time.sleep(7)

    except KeyboardInterrupt:
        print("\n[TEMP] Simulador detenido por el usuario.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
