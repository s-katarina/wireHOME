package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.AmbientSensor;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IAirConditionerService;
import projectnwt2023.backend.devices.service.interfaces.IAmbientSensorService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/airConditioner")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AirConditionerController {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IAirConditionerService airConditionerService;

    @Autowired
    private InfluxDBService influxDBService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<AirConditionerDTO> getAirConditioner(@PathVariable Integer deviceId) {
        AirConditioner device = (AirConditioner) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new AirConditionerDTO(device), HttpStatus.OK);
    }

    @PostMapping(value = "/{deviceId}/action", produces = "text/plain")
    void postActionRequest(@PathVariable Integer deviceId, @RequestBody AirConditionerActionRequest request) {

        // posalji mqtt simulatoru   airConditioner/3/request   colling;3sparklez.cat@gmail.com
        mqttGateway.sendToMqtt(request.getAction() + ";" + request.getUserEmail(), "airConditioner/" + deviceId + "/request");
    }

    @GetMapping(value = "/{deviceId}/actions", produces = "application/json")
    ResponseEntity<ArrayList<AirConditionerActionDTO>> getAirConditionerActions(@PathVariable Integer deviceId) {

        ArrayList<AirConditionerActionDTO> ret = influxDBService.getAllAirConditionerActions(deviceId);

        return new ResponseEntity<>(ret, HttpStatus.OK);

    }

    @GetMapping(value = "/{deviceId}/intervals", produces = "application/json")
    ResponseEntity<ArrayList<ACIntervalDTO>> getAirConditionerIntervals(@PathVariable Integer deviceId) {

        AirConditioner device = (AirConditioner) deviceService.getById(deviceId.longValue());
        ArrayList<ACInterval> intervals = airConditionerService.findIntervalsByAirConditioner(device);
        ArrayList<ACIntervalDTO> ret = new ArrayList<>();
        for (ACInterval acInterval : intervals)
            ret.add(new ACIntervalDTO(acInterval));

        return new ResponseEntity<>(ret, HttpStatus.OK);

    }

    @DeleteMapping(value = "/{deviceId}/interval/{intervalId}", produces = "application/json")
    void deleteAirConditionerInterval(@PathVariable Integer deviceId, @PathVariable Integer intervalId) {
        airConditionerService.deleteIntervalById((long) intervalId);
    }

    @PostMapping(value = "/{deviceId}/interval", produces = "application/json")
    ResponseEntity<ACIntervalDTO> addAirConditionerInterval(@PathVariable Integer deviceId, @RequestBody ACIntervalDTO acIntervalDTO) {

        AirConditioner airConditioner = (AirConditioner) deviceService.getById((long) deviceId);
        ACInterval acInterval = new ACInterval(acIntervalDTO, airConditioner);
        ACInterval saved = airConditionerService.saveInterval(acInterval);

        return new ResponseEntity<>(new ACIntervalDTO(saved), HttpStatus.OK);

    }

}
