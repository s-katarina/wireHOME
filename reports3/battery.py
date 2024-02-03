from datetime import datetime, timedelta
import math
import random
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS

INFLUX_TOKEN = "T4haMiCWLq7vaga61f2oXIczOF2bHcAJNfk07am0VnzQVnU4CdigDz3VMfPBWJjX4hV1HYgLDiQn0--AwWKUSA=="
ORG = "ftn"
BUCKET = "proba"

url = f"http://localhost:8086"

influxdb_client = InfluxDBClient(url=url, token=INFLUX_TOKEN, org=ORG)


def changeLevel(level):
    level += random.uniform(-3, 3)
    if level < 0:
        return 0.0
    if level > 20:
        return 20.0
    return level

def generate_property(timestamp, i):
    parsed_timestamp = datetime.fromisoformat(timestamp)
    current_hour = parsed_timestamp.hour
    if 23 <= current_hour or current_hour < 5:
        return -8.0

    # Adjust the sine function to create a larger spike between 6 am and 8 pm
    base_value = 12.5 * math.sin((i * 2 * math.pi) / 600) + 12.5
    daily_variation = 15 * math.sin((i * 2 * math.pi) / (24 * 60))  # Larger spike between 6 am and 8 pm
    return base_value + daily_variation - 50

def generate_electrodistribution(timestamp, i):
    parsed_timestamp = datetime.fromisoformat(timestamp)
    current_hour = parsed_timestamp.hour
    if 23 <= current_hour or current_hour < 6:
        return -5.0

    # Adjust the sine function to create a larger spike between 6 am and 8 pm
    base_value = 15.5 * math.sin((i * 2 * math.pi) / 600) + 22.5
    daily_variation = 17 * math.sin((i * 2 * math.pi) / (24 * 60))  # Larger spike between 6 am and 8 pm
    return base_value + daily_variation - 30

if __name__ == "__main__":
    deviceId = 6
    propertyId = 1
    city_id = 30
    email = "selena.milutin@gmail.com"
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    battery_level = 20.0
    for i in range((90 * 24 * 60 * 60) // 60):
        timestamp = (current_time - timedelta(seconds=i * 60)).isoformat()
        energy_value = generate_property(timestamp, i)

        point_energy = (
            Point("property-electricity")
            .tag("property-id", propertyId)
            .tag("city-id", city_id)
            .field("value", energy_value)
            .time(timestamp)
        )
        write_api.write(bucket=BUCKET, org=ORG, record=point_energy)

        energy_value = generate_electrodistribution(timestamp, i)

        point_energy = (
            Point("electrodeposition")
            .tag("property-id", propertyId)
            .tag("city-id", city_id)
            .field("value", energy_value)
            .time(timestamp)
        )
        write_api.write(bucket=BUCKET, org=ORG, record=point_energy)


        battery_level = changeLevel(battery_level)
        point_on_off = (
            Point("battery")
            .tag("device-id", deviceId)
            .field("value", battery_level)
            .time(timestamp)
        )
        write_api.write(bucket=BUCKET, org=ORG, record=point_on_off)
