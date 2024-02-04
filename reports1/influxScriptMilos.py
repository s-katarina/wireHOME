from datetime import datetime, timedelta
import math
import random
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS

INFLUX_TOKEN = "O-rpPmvuYpaFJYp2kiJE15pGlRQqta80KCbUL13sdjD5MbAnjoBZn9HHrGT9EDVoAygtjxnVCQ_4mb4xlfMbZA=="
ORG = "FTN"
BUCKET = "nvt_bucket"

url = f"http://localhost:8086"

influxdb_client = InfluxDBClient(url=url, token=INFLUX_TOKEN, org=ORG)

def ambSensorTemp(deviceId):
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    for i in range(5):
        timestamp = (current_time - timedelta(seconds=i*10)).isoformat()
        point = (
        Point("temp")
        .tag("device-id", deviceId)
        .tag("topic", f"ambientSensor/{deviceId}/temp")
        .field("value", 12.5 * math.sin((i * 2 * math.pi)/600) + 12.5)
        .time(timestamp))
        print(point)
        write_api.write(bucket=BUCKET, org=ORG, record=point)

def ambSensorHum(deviceId):
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    curr_hum = 80
    for i in range(5):
        timestamp = (current_time - timedelta(seconds=i*10)).isoformat()
        curr_hum += random.choice([1, -1, 2, -2, 3, -3])
        point = (
        Point("hum")
        .tag("device-id", deviceId)
        .tag("topic", f"ambientSensor/{deviceId}/hum")
        .field("value", float(curr_hum))
        .time(timestamp))
        print(point)
        write_api.write(bucket=BUCKET, org=ORG, record=point)

def airConditioner(deviceId):
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    emails = ["3sparklez.cat@gmail.com", "mikicamiki.bat@gmail.com"]
    actions = ["colling", "heating", "ventilation", "automatic", "off"]
    for i in range(5):
        timestamp = (current_time - timedelta(seconds=i*10)).isoformat()
        point = (
        Point("airEvent")
        .tag("device-id", deviceId)
        .tag("email", random.choice(emails))
        .tag("topic", f"airConditioner/{deviceId}/event")
        .field("value", random.choice(actions))
        .time(timestamp))
        print(point)
        write_api.write(bucket=BUCKET, org=ORG, record=point)

def washingMachine(deviceId):
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    emails = ["3sparklez.cat@gmail.com", "mikicamiki.bat@gmail.com"]
    actions = ["off", "delicate", "automatic", "wool30", "wool40", "cotton30", "cotton40"]
    for i in range(5):
        timestamp = (current_time - timedelta(seconds=i*10)).isoformat()
        point = (
        Point("washingEvent")
        .tag("device-id", deviceId)
        .tag("email", random.choice(emails))
        .tag("topic", f"washingMachine/{deviceId}/event")
        .field("value", random.choice(actions))
        .time(timestamp))
        print(point)
        write_api.write(bucket=BUCKET, org=ORG, record=point)

if __name__ == "__main__":
    washingMachine(7)  # ovde da se ubace emailovi korisnika