from json import JSONDecodeError
from locust import task, HttpUser, task, between

class MyUser(HttpUser):

    wait_time = between(1, 5)
    email = "selena.milutin@gmail.com"
    password = "111"
    token = ""

    admin = "superadmin@gmail.com"
    admin_pass = "222"

    def ONEregister_device(self):
        self.logIn()        

        self.open_all_prop()

        self.save_device()
    
    def TWOregister_device(self):
        self.logIn()        

        self.open_all_prop()

        self.open_prop()

        self.save_device()
    
    def THREEadmin_overview_page(self):
        self.logIn_admin()

        self.admin_overview_page()

    def FOURadmin_overview_property(self):
        self.logIn_admin()

        self.admin_overview_page()

        self.admin_propery()
    
    def FIVEadmin_overview_city(self):
        self.logIn_admin()

        self.admin_overview_page()

        self.admin_city()

    def SIXturn_off_panel(self):
        self.logIn()        

        self.open_all_prop()

        self.open_prop()

        self.get_device()

        self.turn_off_device()


    @task
    def SEVEN_battery(self):
        self.logIn()        

        self.open_all_prop()

        self.open_prop()

        self.get_device()

        self.getGraphData()
    
    def EIGHT_user_overview(self):
        self.logIn()        

        self.open_all_prop()

        self.get_user_electricity()

    
    def NINE_charger_port(self):
        self.logIn()        

        self.open_all_prop()

        self.open_prop()

        self.get_device()

        self.changePort()

    def ten_charger_history(self):
        self.logIn()        

        self.open_all_prop()

        self.open_prop()

        self.get_charging_history()



    def save_device(self):
        payload = {
            "capacity": 0,
            "consumptionAmount": 0,
            "efficiency": 0,
            "maxTemp": 20,
            "minTemp": 1,
            "modelName": "aaa",
            "panelSize": 0,
            "portNumber": 0,
            "propertyId": 1,
            "regimes": [],
            "usesElectricity": True
}
        with self.client.post("/api/device/ambientSensor", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")


    def open_all_prop(self):
        with self.client.get("/api/property", headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")

    def open_prop(self):
        with self.client.get("/api/property/1", headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")

    def logIn(self):
        with self.client.post("/api/user/login", json={"email": self.email, "password": self.password}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

    def logIn_admin(self):
         with self.client.post("/api/user/login", json={"email": self.admin, "password": self.admin_pass}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + response.json()['accessToken'] + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

    def admin_overview_page(self):
        
        params = {"start": 1706206137, "end": 1706810937}   
        with self.client.get("/api/property/accepted", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")
        with self.client.get("/api/property/byCity", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")

    def admin_propery(self):
        params = {"year": 2024, "measurement": "electrodeposition"}   

        with self.client.get("/api/property/byMonthProperty/1", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")

        params = {"start": 1706206137, "end": 1706810937}   
        with self.client.get("/api/property/byTimeOfDay/1", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")

        payload = {"id": 1,
      "from": 1706206137,
      "to": 1706810937,
      "measurement": "electrodeposition"}
        with self.client.post("/api/property/propertyByDay", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")


    def admin_city(self):
        payload = {"id": 1,
            "from": 1706206137,
            "to": 1706810937,
            "measurement": "electrodeposition"}
        with self.client.post("/api/property/propertyEnergy", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

        payload = {"id": 1,
            "from": 1706206137,
            "to": 1706810937,
            "measurement": "property-electricity"}
        with self.client.post("/api/property/propertyEnergy", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")


    def get_device(self):
         with self.client.get("/api/device/1", headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")


    def getGraphData(self):
        payload = {"id": "1",
            "from": "1706206137",
            "to": "1706810937",
            "measurement": "property-electricity"}
        with self.client.post("/api/device/largeEnergy/panelReadings", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

    def turn_off_device(self):
        with self.client.post("/api/device/off/1", headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")


    def get_user_electricity(self):
        payload = {"id": "1",
            "from": "1706206137",
            "to": "1706810937",
            "measurement": "property-electricity"}
        with self.client.post("/api/device/largeEnergy/propertyEnergy", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

        payload = {"id": "1",
            "from": "1706206137",
            "to": "1706810937",
            "measurement": "electrodeposition"}
        with self.client.post("/api/device/largeEnergy/propertyEnergy", json=payload, headers={'Authorization': self.token}, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - login fail")
                else:
                    self.token = 'Bearer ' + '"' + '"'
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - login fail")

    def changePort(self):
        params = {"val": 60}   

        with self.client.put("/api/device/largeEnergy/charger/6/port", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")


    def get_charging_history(self):
        params = {"measurement": "charger-event"}   

        with self.client.get("/api/device/largeEnergy/charger/6/recent", headers={'Authorization': self.token}, params=params, catch_response=True) as response:
            try:
                if (response.status_code != 200):
                    response.failure(f"Response status code {response.status_code} - get property fail")
            except JSONDecodeError:
                response.failure("Response could not be decoded as JSON - get property fail")


        
   