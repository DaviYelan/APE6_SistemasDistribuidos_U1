"""
Simulador de Sensor de Combustible para Flota Vehicular
========================================================
Publica datos de nivel de combustible via MQTT.
Topics: flota/{vehiculo_id}/combustible
Genera alertas cuando el nivel baja del 15%.
"""

import json
import time
import random
import paho.mqtt.client as mqtt
from datetime import datetime

# ── Configuracion del Broker MQTT ──
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
CLIENT_ID = "simulador_combustible"

# ── Vehiculos con estado de combustible ──
VEHICULOS_COMBUSTIBLE = {
    "VH-001": 85.0,
    "VH-002": 60.0,
    "VH-003": 30.0,
}

UMBRAL_BAJO = 15.0  # Porcentaje minimo


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("[FUEL] Conectado exitosamente al broker MQTT")
    else:
        print(f"[FUEL] Error de conexion. Codigo: {rc}")


def generar_datos_combustible(vehiculo_id):
    """
    Genera datos simulados de combustible.
    El nivel disminuye gradualmente simulando consumo real.
    """
    global VEHICULOS_COMBUSTIBLE

    # Consumo aleatorio entre 0.5% y 3%
    consumo = round(random.uniform(0.5, 3.0), 1)
    VEHICULOS_COMBUSTIBLE[vehiculo_id] = max(
        0, VEHICULOS_COMBUSTIBLE[vehiculo_id] - consumo
    )

    # Repostaje aleatorio (5% de probabilidad)
    if random.random() < 0.05:
        VEHICULOS_COMBUSTIBLE[vehiculo_id] = round(random.uniform(70, 100), 1)
        print(f"[FUEL] {vehiculo_id} ha repostado!")

    nivel = VEHICULOS_COMBUSTIBLE[vehiculo_id]
    alerta = nivel < UMBRAL_BAJO

    return {
        "vehicleId": vehiculo_id,
        "timestamp": datetime.now().isoformat(),
        "nivelCombustible": round(nivel, 1),
        "unidad": "porcentaje",
        "consumoReciente": consumo,
        "alerta": alerta,
        "umbral": UMBRAL_BAJO
    }


def main():
    client = mqtt.Client(CLIENT_ID)
    client.on_connect = on_connect

    print("[FUEL] Conectando al broker MQTT...")
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()

    print("[FUEL] Simulador de combustible iniciado. Publicando cada 10 segundos...")
    print(f"[FUEL] Umbral de alerta: {UMBRAL_BAJO}%")
    print("-" * 60)

    try:
        while True:
            for vehiculo_id in VEHICULOS_COMBUSTIBLE:
                datos = generar_datos_combustible(vehiculo_id)
                topic = f"flota/{vehiculo_id}/combustible"
                payload = json.dumps(datos)

                client.publish(topic, payload, qos=1)

                estado = " BAJO" if datos["alerta"] else "✓ OK"
                print(f"[FUEL] {topic} -> {datos['nivelCombustible']}% [{estado}]")

            print("-" * 60)
            time.sleep(10)

    except KeyboardInterrupt:
        print("\n[FUEL] Simulador detenido por el usuario.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
