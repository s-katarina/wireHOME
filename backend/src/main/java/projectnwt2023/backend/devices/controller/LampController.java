package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.DeviceDTO;
import projectnwt2023.backend.devices.dto.LampDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.ILampService;
import projectnwt2023.backend.property.dto.PropertyResponseDTO;

@RestController
@RequestMapping("/api/lamp")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class LampController {

    @Autowired
    IDeviceService deviceService;

    @Autowired
    ILampService lampService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<DeviceDTO> getLamp(@PathVariable Integer deviceId){

        Lamp device = (Lamp) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new LampDTO(device), HttpStatus.OK);
    }

    @PutMapping(value = "/{deviceId}/bulb-on", produces = "application/json")
    ResponseEntity<?> turnBulbOn(@PathVariable Integer deviceId){
        try {
            mqttGateway.sendToMqtt("ON", "lamp/" + String.valueOf(deviceId)+"/bulb/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Lamp Bulb Turn on failed");
        }
    }

    @PutMapping(value = "/{deviceId}/bulb-off", produces = "application/json")
    ResponseEntity<?> turnBulbOff(@PathVariable Integer deviceId){
        try {
            mqttGateway.sendToMqtt("OFF", "lamp/" +String.valueOf(deviceId)+"/bulb/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Lamp Bulb Turn off failed");
        }
    }

    @PutMapping(value = "/{deviceId}/automatic", produces = "application/json")
    ResponseEntity<?>setAutomatic(@PathVariable Integer deviceId,
                                   @RequestParam("val") boolean automatic){
        try {
            if (automatic) mqttGateway.sendToMqtt("ON", "lamp/" + String.valueOf(deviceId)+"/automatic/set");
            else mqttGateway.sendToMqtt("OFF", "lamp/" + String.valueOf(deviceId)+"/automatic/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Lamp Bulb Turn off failed");
        }
    }

}
