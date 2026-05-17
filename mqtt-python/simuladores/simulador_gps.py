"""
Simulador de Sensor GPS para Flota Vehicular
=============================================
Publica datos de posicion GPS via MQTT para cada vehiculo.
Topics: flota/{vehiculo_id}/gps
"""

import json
import time
import random
import paho.mqtt.client as mqtt
from datetime import datetime

# ── Configuracion del Broker MQTT ──
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
CLIENT_ID = "simulador_gps"

# ── Vehiculos de la flota ──
VEHICULOS = ["VH-001", "VH-002", "VH-003"]

# ── Coordenadas base (Loja, Ecuador) ──
RUTAS = {
    "VH-001": {"lat": -3.9931, "lng": -79.2042, "destino": "Ruta Norte"},
    "VH-002": {"lat": -4.0050, "lng": -79.2100, "destino": "Ruta Sur"},
    "VH-003": {"lat": -3.9800, "lng": -79.1950, "destino": "Ruta Centro"},
}


def on_connect(client, userdata, flags, rc):
    """Callback cuando se conecta al broker MQTT."""
    if rc == 0:
        print("[GPS] Conectado exitosamente al broker MQTT")
    else:
        print(f"[GPS] Error de conexion. Codigo: {rc}")


def generar_datos_gps(vehiculo_id):
    """Genera datos simulados de GPS para un vehiculo."""
    ruta = RUTAS[vehiculo_id]
    return {
        "vehicleId": vehiculo_id,
        "timestamp": datetime.now().isoformat(),
        "lat": round(ruta["lat"] + random.uniform(-0.01, 0.01), 6),
        "lng": round(ruta["lng"] + random.uniform(-0.01, 0.01), 6),
        "speed": round(random.uniform(0, 80), 1),
        "heading": round(random.uniform(0, 360), 1),
        "ruta": ruta["destino"]
    }


def main():
    """Funcion principal del simulador GPS."""
    # Crear cliente MQTT
    client = mqtt.Client(CLIENT_ID)
    client.on_connect = on_connect

    print("[GPS] Conectando al broker MQTT...")
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()

    print("[GPS] Simulador de GPS iniciado. Publicando cada 5 segundos...")
    print(f"[GPS] Vehiculos monitoreados: {', '.join(VEHICULOS)}")
    print("-" * 60)

    try:
        while True:
            for vehiculo_id in VEHICULOS:
                datos = generar_datos_gps(vehiculo_id)
                topic = f"flota/{vehiculo_id}/gps"
                payload = json.dumps(datos)

                client.publish(topic, payload, qos=1)
                print(f"[GPS] {topic} -> lat={datos['lat']}, lng={datos['lng']}, "
                      f"vel={datos['speed']} km/h")

            print("-" * 60)
            time.sleep(5)

    except KeyboardInterrupt:
        print("\n[GPS] Simulador detenido por el usuario.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
