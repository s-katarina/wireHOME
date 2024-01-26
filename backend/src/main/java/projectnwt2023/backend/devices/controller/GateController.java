package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.dto.model.GateDTO;
import projectnwt2023.backend.devices.dto.GateEventDTO;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.IGateService;
import projectnwt2023.backend.helper.ApiResponse;

import java.util.ArrayList;
import java.util.List;

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
            if (isPublic) mqttGateway.sendToMqtt("PUBLIC", "gate/"+String.valueOf(deviceId)+"/regime/set");
            else  mqttGateway.sendToMqtt("PRIVATE", "gate/"+String.valueOf(deviceId)+"/regime/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Gate Regime set failed");
        }
    }

    @PutMapping(value = "/{deviceId}/open", produces = "application/json")
    ResponseEntity<?> setOpen(@PathVariable Integer deviceId,
                                @RequestParam("val") Boolean shouldOpen){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            if (shouldOpen) mqttGateway.sendToMqtt("OPEN", "gate/"+String.valueOf(deviceId)+"/open/set");
            else  mqttGateway.sendToMqtt("CLOSE", "gate/"+String.valueOf(deviceId)+"/open/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Gate Open set failed");
        }
    }

    @GetMapping(value = "/{deviceId}/recent", produces = "application/json")
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRecentGateEvents(@PathVariable Integer deviceId){

        List<GateEventMeasurement> res = gateService.getRecentGateEvents(Long.valueOf(deviceId));
        List<GateEventDTO> ret = new ArrayList<>();
        for (GateEventMeasurement measurement : res) {
            ret.add(new GateEventDTO(measurement.getCaller(), measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

    @GetMapping(value = "/{deviceId}/range", produces = "application/json")
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRangeGateEvents(@PathVariable Integer deviceId,
                                                                       @RequestParam String start,
                                                                       @RequestParam String end) {

        List<GateEventMeasurement> res = gateService.getDateRangeGateEvents(Long.valueOf(deviceId), start, end);
        List<GateEventDTO> ret = new ArrayList<>();
        if (res != null) {
            for (GateEventMeasurement measurement : res) {
                ret.add(new GateEventDTO(measurement.getCaller(), measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
            }
            return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

        @PutMapping(value = "/{deviceId}/licencePlate", produces = "application/json")
    ResponseEntity<GateDTO> addLicencePlate(@PathVariable Integer deviceId,
                                            @RequestParam("val") String licencePlate){
        Gate gate = gateService.addLicencePlate(Long.valueOf(deviceId), licencePlate);
        mqttGateway.sendToMqtt(licencePlate, "gate/"+String.valueOf(deviceId)+"/licencePlate/set");
        return new ResponseEntity<>(new GateDTO((gate)), HttpStatus.OK);
    }

}
