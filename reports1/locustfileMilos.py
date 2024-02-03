import random
from locust import HttpUser, task

class NVTUser(HttpUser):

  def on_start(self):
    self.client.headers = {'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzc3BhcmtsZXouY2F0QGdtYWlsLmNvbSIsInJvbGUiOiJBVVRIX1VTRVIiLCJhY3RpdmUiOnRydWUsImlkIjoyLCJleHAiOjE3MDY4MDAyODYsImlhdCI6MTcwNjc4MjI4Nn0.8qKcIN8JaU5fGS0MDvXjtFtZAeCWqGnVDDt59ZJji7g_lCscqqXTfJiHvJx8EiLtfrH6A6_TpbgDgW4ck4EMSw'}

  # @task
  # def register_user(self):
  #   rnd = random.randint(1, 100000000)
  #   self.client.post("/user", json={
  #     "id": None,
  #     "name":"pera",
  #     "lastName":"markic",
  #     "email": f"pera{rnd}@tra.com",
  #     "password": "123",
  #     "active": None
  #   })

  # @task
  # def register_admin(self):
  #   rnd = random.randint(1, 100000000)
  #   self.client.post("/user/admin", json={
  #     "id": None,
  #     "name":"pera",
  #     "lastName":"markic",
  #     "email": f"pera{rnd}@tra.com",
  #     "password": "123",
  #     "active": True
  #   })

  # @task
  # def login_user(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })

  # @task
  # def give_property_permission(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   self.client.post("/sharing/property", json={
  #     "id": 1,
  #     "email": "mikicamiki.bat@gmail.com"
  #   })
    
  # @task
  # def remove_property_permission(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   self.client.post("/sharing/property/delete", json={
  #     "id": 100,
  #     "email": "mikicamiki.bat@gmail.com"
  #   })
    
  # @task
  # def give_device_permission(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   self.client.post("/sharing/device", json={
  #     "id": 2,
  #     "email": "mikicamiki.bat@gmail.com"
  #   })
    
  # @task
  # def remove_device_permission(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   self.client.post("/sharing/device/delete", json={
  #     "id": 100,
  #     "email": "mikicamiki.bat@gmail.com"
  #   })
    
  # @task
  # def get_ambient_sensor_history(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   start = 1690880587
  #   end = 1706782030
  #   self.client.get(f"/ambientSensor/2/values?from={start}&to={end}")
    
  # @task
  # def get_air_conditioner_history(self):
  #   self.client.post("/user/login", json={
  #     "email": f"mikicamiki.bat@gmail.com",
  #     "password": "123"
  #   })
  #   self.client.get(f"/airConditioner/3/actions")
    
  @task
  def get_washing_machine_history(self):
    self.client.post("/user/login", json={
      "email": f"mikicamiki.bat@gmail.com",
      "password": "123"
    })
    self.client.get(f"/washingMachine/6/actions")