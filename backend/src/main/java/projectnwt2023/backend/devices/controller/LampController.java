package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.ILampService;

@RestController
@RequestMapping("/api/lamp")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class LampController {

    @Autowired
    IDeviceService deviceService;

    @Autowired
    ILampService lampService;



    @PostMapping(value = "/on/{deviceId}", produces = "application/json")
    ResponseEntity<?> turnOn(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            lampService.turnOn((Lamp) device);
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Lamp Turn on failed");
        }
    }

    @PostMapping(value = "/off/{deviceId}", produces = "application/json")
    ResponseEntity<?> turnOff(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            lampService.turnOff((Lamp) device);
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Lamp Turn off failed");
        }
    }

}
