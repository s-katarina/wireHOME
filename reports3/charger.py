from datetime import datetime, timedelta
import math
import random
import string
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS

INFLUX_TOKEN = "T4haMiCWLq7vaga61f2oXIczOF2bHcAJNfk07am0VnzQVnU4CdigDz3VMfPBWJjX4hV1HYgLDiQn0--AwWKUSA=="
ORG = "ftn"
BUCKET = "proba"

url = f"http://localhost:8086"

influxdb_client = InfluxDBClient(url=url, token=INFLUX_TOKEN, org=ORG)

# data := fmt.Sprintf("charger-event,device-id=%d value=\"%s\",caller=\"%s\"", charger.Id, event, caller)
# data := fmt.Sprintf("energy-maintaining,device-id=%d,property-id=%d,device-type=%s value=%f", charger.Id, charger.PropertyId, charger.DeviceType, -batteryCapacity/100)
# data := fmt.Sprintf("charger-vehicle,device-id=%d,battery-capacity=%d,percentage=%d,energy-consumed=%f value=\"%s\"", charger.Id, batteryCapacity, percentage, energyConsumed, plateNumber)
def generate_random_string():
    # Generate random uppercase letters for the first two characters
    letters = ''.join(random.choices(string.ascii_uppercase, k=2))

    # Generate random digits for the last three characters
    numbers = ''.join(random.choices(string.digits, k=3))

    # Concatenate the letters and numbers to form the final string
    result = letters + numbers

    return result
    

if __name__ == "__main__":
    deviceId = 6
    propertyId = 1
    email = "selena.milutin@gmail.com"
    write_api = influxdb_client.write_api(write_options=SYNCHRONOUS)
    current_time = datetime.utcnow()
    for i in range( (90 * 24 * 60 * 60) // 40):
        timestamp = (current_time - timedelta(seconds=i*40)).isoformat()

        point = (
        Point("energy-maintaining")
        .tag("device-id", deviceId)
        .tag("property-id", propertyId)
        .tag("device-type", 'charger')
        .tag("topic", f"energy/{deviceId}/any-device")
        .field("value", 12.5 * math.sin((i * 2 * math.pi)/600) + 12.5 - 30)
        .time(timestamp))
        write_api.write(bucket=BUCKET, org=ORG, record=point)

        plate = generate_random_string()
        point = (
        Point("charger-event")
        .tag("device-id", deviceId)
        .tag("caller", plate)
        .field("value", "charging start")
        .time(timestamp))
        write_api.write(bucket=BUCKET, org=ORG, record=point)

        point = (
        Point("charger-event")
        .tag("device-id", deviceId)
        .tag("caller", plate)
        .field("value", "charging end")
        .time(timestamp))
        write_api.write(bucket=BUCKET, org=ORG, record=point)

        point = (
        Point("charger-event")
        .tag("device-id", deviceId)
        .tag("caller", email)
        .field("value", "port changed to " + str(random.randint(40, 100)))
        .time(timestamp))
        write_api.write(bucket=BUCKET, org=ORG, record=point)