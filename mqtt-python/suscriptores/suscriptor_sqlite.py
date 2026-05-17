"""
Suscriptor MQTT con almacenamiento en SQLite
=============================================
Se suscribe a todos los topics de la flota y almacena
los datos de telemetria en una base de datos SQLite local.
"""

import json
import sqlite3
import paho.mqtt.client as mqtt
from datetime import datetime

# ── Configuracion ──
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
CLIENT_ID = "suscriptor_sqlite"
DB_PATH = "telemetria_flota.db"


def inicializar_db():
    """Crea las tablas de la base de datos si no existen."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS telemetria_gps (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            lat REAL NOT NULL,
            lng REAL NOT NULL,
            speed REAL,
            heading REAL,
            ruta TEXT,
            created_at TEXT DEFAULT (datetime('now'))
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS telemetria_temperatura (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            temperatura REAL NOT NULL,
            humedad REAL,
            alerta INTEGER DEFAULT 0,
            created_at TEXT DEFAULT (datetime('now'))
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS telemetria_combustible (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            vehicle_id TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            nivel_combustible REAL NOT NULL,
            consumo_reciente REAL,
            alerta INTEGER DEFAULT 0,
            created_at TEXT DEFAULT (datetime('now'))
        )
    """)

    conn.commit()
    conn.close()
    print("[DB] Base de datos inicializada correctamente.")


def guardar_gps(datos):
    """Guarda datos GPS en SQLite."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO telemetria_gps (vehicle_id, timestamp, lat, lng, speed, heading, ruta)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, (
        datos["vehicleId"], datos["timestamp"],
        datos["lat"], datos["lng"],
        datos.get("speed", 0), datos.get("heading", 0),
        datos.get("ruta", "")
    ))
    conn.commit()
    conn.close()


def guardar_temperatura(datos):
    """Guarda datos de temperatura en SQLite."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO telemetria_temperatura (vehicle_id, timestamp, temperatura, humedad, alerta)
        VALUES (?, ?, ?, ?, ?)
    """, (
        datos["vehicleId"], datos["timestamp"],
        datos["temperatura"], datos.get("humedad", 0),
        1 if datos.get("alerta", False) else 0
    ))
    conn.commit()
    conn.close()


def guardar_combustible(datos):
    """Guarda datos de combustible en SQLite."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO telemetria_combustible (vehicle_id, timestamp, nivel_combustible, consumo_reciente, alerta)
        VALUES (?, ?, ?, ?, ?)
    """, (
        datos["vehicleId"], datos["timestamp"],
        datos["nivelCombustible"], datos.get("consumoReciente", 0),
        1 if datos.get("alerta", False) else 0
    ))
    conn.commit()
    conn.close()


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("[SUB] Conectado al broker MQTT")
        # Suscribirse a todos los topics de la flota con wildcard
        client.subscribe("flota/+/gps", qos=1)
        client.subscribe("flota/+/temperatura", qos=1)
        client.subscribe("flota/+/combustible", qos=1)
        print("[SUB] Suscrito a flota/+/gps, flota/+/temperatura, flota/+/combustible")
    else:
        print(f"[SUB] Error de conexion: {rc}")


def on_message(client, userdata, msg):
    """Procesa cada mensaje recibido y lo almacena en SQLite."""
    try:
        topic = msg.topic
        datos = json.loads(msg.payload.decode())

        if "/gps" in topic:
            guardar_gps(datos)
            print(f"[SUB] GPS almacenado: {datos['vehicleId']} -> "
                  f"({datos['lat']}, {datos['lng']})")

        elif "/temperatura" in topic:
            guardar_temperatura(datos)
            estado = "⚠ ALERTA" if datos.get("alerta") else "✓ OK"
            print(f"[SUB] Temperatura almacenada: {datos['vehicleId']} -> "
                  f"{datos['temperatura']}°C [{estado}]")

        elif "/combustible" in topic:
            guardar_combustible(datos)
            estado = "⚠ BAJO" if datos.get("alerta") else "✓ OK"
            print(f"[SUB] Combustible almacenado: {datos['vehicleId']} -> "
                  f"{datos['nivelCombustible']}% [{estado}]")

    except Exception as e:
        print(f"[SUB] Error procesando mensaje: {e}")


def main():
    inicializar_db()

    client = mqtt.Client(CLIENT_ID)
    client.on_connect = on_connect
    client.on_message = on_message

    print("[SUB] Conectando al broker MQTT...")
    client.connect(MQTT_BROKER, MQTT_PORT, 60)

    print("[SUB] Suscriptor iniciado. Esperando mensajes...")
    print("=" * 60)

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\n[SUB] Suscriptor detenido.")
        client.disconnect()


if __name__ == "__main__":
    main()
