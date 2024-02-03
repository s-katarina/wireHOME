from json import JSONDecodeError
import random
import time
from locust import task, HttpUser, between

property_img = 'property-test-img.jpg'

class AppUser(HttpUser):

    wait_time = between(1,2)
    users = []

    def on_start(self):
        # regular
        for i in range (1, 11):
            self.users.append({"email": f"test{i}@gmail.com", "password": "333"})
        print("Ready to start")

    @task
    def login_get_properties_get_devices_for_property(self):
        random_index = random.randint(0, 9)
        token = ""
        property_id = -1
        with self.client.post("/api/user/login", json={"email": self.users[random_index]["email"], "password": self.users[random_index]["password"]}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")        

        with self.client.get("/api/property", headers={'Authorization': token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
                else:
                    if (len(response.json())>0):
                        property_id = response.json()[0]['id']
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")
        
        with self.client.get("/api/device/appliances/%i" % property_id, name="/api/device/appliances/[propertyId]", headers={'Authorization': token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get appliances fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get appliances fail")
        
        with self.client.get("/api/device/outdoor/%i" % property_id, name="/api/device/outdoor/[propertyId]", headers={'Authorization': token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get appliances fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get appliances fail")
        
        with self.client.get("/api/device/energyDevices/%i" % property_id, name="/api/device/energyDevices/[propertyId]", headers={'Authorization': token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get appliances fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get appliances fail")


    @task
    def login_post_property(self):
        random_index = random.randint(0, 9)
        token = ""
        property_id = -1
        with self.client.post("/api/user/login", json={"email": self.users[random_index]["email"], "password": self.users[random_index]["password"]}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail in post property")
                else:
                    token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail in post property")        

        with self.client.post("/api/property", 
            json={"propertyType": "HOUSE", "address": "Blue Jay Road 3864", "cityId": 3, "area": 78.5, "floorCount": 2}, 
            headers={'Authorization': token}, 
            catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - post property fail")
                else:
                    property_id = response.json()["data"]["id"]
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - post property fail")
        
        print("Property id:", property_id)
        with self.client.post("/api/images/property/upload?propertyId=%i" % property_id, name="/api/images/property/upload?propertyId=[id]",
            files={'file': open(property_img, 'rb')}, 
            headers={'Authorization': token}, 
            catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - post property image fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - post property image fail")

    @task
    def login_get_online_report(self):
        token = ""
        start = "1704221849998"
        end = "1706813849998"
        device_id = 2
        with self.client.post("/api/user/login", json={"email": "...", "password": "333"}, catch_response=True) as response:
            if (response.status_code != 200):
                response.failure(f"Response status code {response.status_code} - login fail")
            else:
                token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'

        with self.client.get(f"/api/device/onlinePercent/{device_id}?start={start}&end={end}", name="/api/device/onlinePercent/[deviceId]", 
            headers={'Authorization': token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code}")

        with self.client.get(f"/api/device/onlineIntervals/{device_id}?start={start}&end={end}", name="/api/device/onlineIntervals/[deviceId]", 
            headers={'Authorization': token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code} - get property fail")
        
        with self.client.get(f"/api/device/onlinePerUnit/{device_id}?start={start}&end={end}", name="/api/device/onlinePerUnit/[deviceId]", 
            headers={'Authorization': token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code} - get property fail")

    @task
    def login_get_gate_events_month(self):
        token = ""
        start = "1704224678766"
        end = "1706816678766"
        device_id = 1
        with self.client.post("/api/user/login", json={"email": "...", "password": "333"}, catch_response=True) as response:
            if (response.status_code != 200):
                response.failure(f"Response status code {response.status_code} - login fail")
            else:
                token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'

        with self.client.get(f"/api/gate/{device_id}/range?start={start}&end={end}", name="/api/gate/[deviceId]/range", 
            headers={'Authorization': token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code}")
    
    @task
    def login_get_sprinkler_commands_month(self):
        token = ""
        start = "1704224678766"
        end = "1706816678766"
        device_id = 7
        with self.client.post("/api/user/login", json={"email": "...", "password": "333"}, catch_response=True) as response:
            if (response.status_code != 200):
                response.failure(f"Response status code {response.status_code} - login fail")
            else:
                token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'

        with self.client.get(f"/api/sprinkler/{device_id}/range?start={start}&end={end}", name="/api/sprinkler/[deviceId]/range", 
            headers={'Authorization': token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code}")
        
property_id = 2000

class AdminUser(HttpUser):
    
    wait_time = between(1,2)
    users = []

    def on_start(self):
        # admin
        for i in range (1, 6):
            self.users.append({"email": f"admintest{i}@gmail.com", "password": "333"})

    @task
    def login_accept_property(self):
        global property_id
        random_index = random.randint(0, 4)
        token = ""
        with self.client.post("/api/user/login", json={"email": self.users[random_index]["email"], "password": self.users[random_index]["password"]}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail in put accept property")
                else:
                    token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail in put accept property")        

        with self.client.put("/api/property/pending/accept/%i" % property_id, name="/api/property/pending/accept/[id]",
            headers={'Authorization': token}, 
            catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - put accept property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - post property fail")
        property_id += 1


class DeviceUser(HttpUser):

    wait_time = between(1,2)

    token = ""
    start_timestamp = "1704224678766"
    end_tmestamp = "1706816678766" 
    device_id = 2

    def on_start(self):
        with self.client.post("/api/user/login", json={"email": "...", "password": "333"}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")    

    @task
    def get_light_sensor(self):
        with self.client.get(f"/api/lamp/{self.device_id}/range?start={self.start_timestamp}&end={self.end_tmestamp}", name="/api/lamp/[deviceId]/range", 
            headers={'Authorization': self.token}, catch_response=True) as response:
            if (response.status_code != 200):
                print(response.json())
                response.failure(f"Response status code {response.status_code}")
        
   