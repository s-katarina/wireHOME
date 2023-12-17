package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.AmbientSensor;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IAmbientSensorService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/ambientSensor")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AmbientSensorController {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IAmbientSensorService ambientSensorService;

    @Autowired
    private InfluxDBService influxDBService;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<AmbientSensorDTO> getAmbientSensor(@PathVariable Integer deviceId) {
        AmbientSensor device = (AmbientSensor) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new AmbientSensorDTO(device), HttpStatus.OK);
    }

    @GetMapping(value = "/{deviceId}/values", produces = "application/json")
    ResponseEntity<AmbientSensorTempHumDTO> getAmbientSensorTemp(@PathVariable Integer deviceId, @RequestParam Long from, @RequestParam Long to) {

        AmbientSensorDateValueDTO temp = influxDBService.getAllTempForAmbientSensorInPeriod(deviceId, from, to);
        AmbientSensorDateValueDTO hum = influxDBService.getAllHumForAmbientSensorInPeriod(deviceId, from, to);

        AmbientSensorTempHumDTO dto = new AmbientSensorTempHumDTO();
        dto.setTemp(temp);
        dto.setHum(hum);
        dto.setLength((long) temp.getDates().size());

        return new ResponseEntity<>(dto, HttpStatus.OK);

    }

}
