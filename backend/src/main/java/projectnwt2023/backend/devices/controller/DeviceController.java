package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.RegimeAirConditioner;
import projectnwt2023.backend.devices.RegimeType;
import projectnwt2023.backend.devices.RegimeWashingMachine;
import projectnwt2023.backend.devices.dto.DeviceResponseDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.dto.PropertyResponseDTO;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class DeviceController {

    @Autowired
    IDeviceService deviceService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<DeviceResponseDTO> getDevice(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        System.out.println(deviceId);
        return new ResponseEntity<>(new DeviceResponseDTO(device), HttpStatus.OK);
    }

    @PostMapping(value = "/on/{deviceId}", produces = "application/json")
    ResponseEntity<?> turnOn(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            mqttGateway.sendToMqtt("ON", device.getTopic() + deviceId);
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            System.out.println("greeeska");
            e.printStackTrace();
            return ResponseEntity.ok("Nije hteo da posaljke");
        }
    }

    @PostMapping(value = "/off/{deviceId}", produces = "application/json")
    ResponseEntity<?> turnOff(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            mqttGateway.sendToMqtt("OFF", device.getTopic() + deviceId);
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            System.out.println("greeeska");
            e.printStackTrace();
            return ResponseEntity.ok("Nije hteo da posaljke");
        }
    }

    @GetMapping(value = "/regimes", produces = "application/json")
    public ResponseEntity<ArrayList<String>> getRegimes(){
        ArrayList<String> regimes = new ArrayList<>();
        for (RegimeType regime:RegimeType.values()) {
            regimes.add(String.valueOf(regime));
        }
        return new ResponseEntity<>(regimes, HttpStatus.OK);
    }

    @GetMapping(value = "/regimesAirConditioner", produces = "application/json")
    public ResponseEntity<ArrayList<String>> getRegimesAirConditioner(){
        ArrayList<String> regimes = new ArrayList<>();
        for (RegimeAirConditioner regime:RegimeAirConditioner.values()) {
            regimes.add(String.valueOf(regime));
        }
        return new ResponseEntity<>(regimes, HttpStatus.OK);
    }

    @GetMapping(value = "/regimesWashingMachine", produces = "application/json")
    public ResponseEntity<ArrayList<String>> getRegimesWashingMachine(){
        ArrayList<String> regimes = new ArrayList<>();
        for (RegimeWashingMachine regime:RegimeWashingMachine.values()) {
            regimes.add(String.valueOf(regime));
        }
        return new ResponseEntity<>(regimes, HttpStatus.OK);
    }

}
