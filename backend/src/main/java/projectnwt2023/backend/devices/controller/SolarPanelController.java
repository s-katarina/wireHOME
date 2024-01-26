package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.SolarPanel;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.dto.model.BatteryDTO;
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.GraphRequestDTO;
import projectnwt2023.backend.devices.dto.model.SolarPanelDTO;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.helper.ApiResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/device/largeEnergy")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SolarPanelController {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    InfluxDBService influxDBService;
    @GetMapping(value = "/solar/{deviceId}", produces = "application/json")
    ResponseEntity<SolarPanelDTO> getSolarPanel(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        System.out.println(deviceId);
        return new ResponseEntity<>(new SolarPanelDTO((SolarPanel) device), HttpStatus.OK);
    }

    @GetMapping(value = "/battery/{deviceId}", produces = "application/json")
    ResponseEntity<BatteryDTO> getBattery(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        System.out.println(deviceId);
        return new ResponseEntity<>(new BatteryDTO((Battery) device), HttpStatus.OK);
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

    @GetMapping(value = "/{deviceId}/recent", produces = "application/json")
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRecentGateEvents(@PathVariable Integer deviceId){

        List<GateEventMeasurement> res = deviceService.getRecentEvents(Long.valueOf(deviceId));
        List<GateEventDTO> ret = new ArrayList<>();
        for (GateEventMeasurement measurement : res) {
            ret.add(new GateEventDTO(measurement.getCaller(), measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

    @GetMapping(value = "/{deviceId}/range", produces = "application/json")
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRangeGateEvents(@PathVariable Integer deviceId,
                                                                       @RequestParam String start,
                                                                       @RequestParam String end){

        List<GateEventMeasurement> res = deviceService.getDateRangeEvents(Long.valueOf(deviceId), start, end);
        List<GateEventDTO> ret = new ArrayList<>();
        for (GateEventMeasurement measurement : res) {
            ret.add(new GateEventDTO(measurement.getCaller(), measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

}
