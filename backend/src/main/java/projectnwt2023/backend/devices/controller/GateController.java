package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.DeviceDTO;
import projectnwt2023.backend.devices.dto.GateDTO;
import projectnwt2023.backend.devices.dto.LampDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.IGateService;
import projectnwt2023.backend.devices.service.interfaces.ILampService;

@RestController
@RequestMapping("/api/gate")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class GateController {

    @Autowired
    IDeviceService deviceService;

    @Autowired
    IGateService gateService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<GateDTO> getGate(@PathVariable Integer deviceId){

        Gate device = (Gate) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new GateDTO(device), HttpStatus.OK);
    }

    @PutMapping(value = "/{deviceId}/regime", produces = "application/json")
    ResponseEntity<?> setRegime(@PathVariable Integer deviceId,
                                @RequestParam("public") Boolean isPublic){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            if (isPublic) mqttGateway.sendToMqtt("PUBLIC", String.valueOf(deviceId)+"/regime/set");
            else  mqttGateway.sendToMqtt("PRIVATE", String.valueOf(deviceId)+"/regime/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Gate Regime set failed");
        }
    }

    @PutMapping(value = "/{deviceId}/open", produces = "application/json")
    ResponseEntity<?> setOpen(@PathVariable Integer deviceId,
                                @RequestParam("should") Boolean shouldOpen){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            if (shouldOpen) mqttGateway.sendToMqtt("OPEN", String.valueOf(deviceId)+"/open/set");
            else  mqttGateway.sendToMqtt("CLOSE", String.valueOf(deviceId)+"/open/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Gate Open set failed");
        }
    }

}
