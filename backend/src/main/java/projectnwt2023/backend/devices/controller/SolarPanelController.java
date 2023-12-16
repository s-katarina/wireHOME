package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.SolarPanel;
import projectnwt2023.backend.devices.dto.DeviceDTO;
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.GraphRequestDTO;
import projectnwt2023.backend.devices.dto.SolarPanelDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/device/solar")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SolarPanelController {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    InfluxDBService influxDBService;
    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<SolarPanelDTO> getSolarPanel(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        System.out.println(deviceId);
        return new ResponseEntity<>(new SolarPanelDTO((SolarPanel) device), HttpStatus.OK);
    }

    @PostMapping(value = "/panelReadings", produces = "application/json")
    ResponseEntity<ArrayList<GraphDTO>> getSolarPanelReadings(@RequestBody GraphRequestDTO graphRequestDTO){
        ArrayList<GraphDTO> grapgData = influxDBService.findDeviceEnergyForDate(graphRequestDTO);
        return new ResponseEntity<>(grapgData, HttpStatus.OK);
    }

    @PostMapping(value = "/propertyEnergy", produces = "application/json") // koristi i za elektrodistribuciju i za samu potrosnju
    ResponseEntity<ArrayList<GraphDTO>> getElectoByProperty(@RequestBody GraphRequestDTO graphRequestDTO){
        ArrayList<GraphDTO> grapgData = influxDBService.findPropertyEnergyForDate(graphRequestDTO);
        return new ResponseEntity<>(grapgData, HttpStatus.OK);
    }

}
