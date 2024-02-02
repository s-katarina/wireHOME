from datetime import datetime, timedelta
import math
import random
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS

INFLUX_TOKEN = "T4haMiCWLq7vaga61f2oXIczOF2bHcAJNfk07am0VnzQVnU4CdigDz3VMfPBWJjX4hV1HYgLDiQn0--AwWKUSA=="
ORG = "ftn"
BUCKET = "nana"

url = f"http://localhost:8086"

influxdb_client = InfluxDBClient(url=url, token=INFLUX_TOKEN, org=ORG)


def generate_energy_value(timestamp, i):
    parsed_timestamp = datetime.fromisoformat(timestamp)

# Extract the hour from the datetime object
    current_hour = parsed_timestamp.hour
    # print(current_hour)
    # Set values to 0 during nighttime (8 pm to 6 am)
    if 20 <= current_hour or current_hour < 6:
        return 0.0

    # Adjust the sine function to create a larger spike between 6 am and 8 pm
    base_value = 12.5 * math.sin((i * 2 * math.pi) / 600) + 12.5
    daily_variation = 15 * math.sin((i * 2 * math.pi) / (24 * 60))  # Larger spike between 6 am and 8 pm
    return base_value + daily_variation + 30


if __name__ == "__main__":
    deviceId = 6
    propertyId = 1
    email = "selena.milutin@gmail.com"
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()

    for i in range((90 * 24 * 60 * 60) // 60):
        timestamp = (current_time - timedelta(seconds=i * 60)).isoformat()
        energy_value = generate_energy_value(timestamp, i)

        point_energy = (
            Point("energy-maintaining")
            .tag("device-id", deviceId)
            .tag("property-id", propertyId)
            .tag("device-type", 'charger')
            .tag("topic", f"energy/{deviceId}/any-device")
            .field("value", energy_value)
            .time(timestamp)
        )
        write_api.write(bucket=BUCKET, org=ORG, record=point_energy)

        point_on_off = (
            Point("on/off")
            .tag("device-id", deviceId)
            .tag("caller", email)
            .field("value", random.choice([0.0, 1.0]))
            .time(timestamp)
        )
        write_api.write(bucket=BUCKET, org=ORG, record=point_on_off)
