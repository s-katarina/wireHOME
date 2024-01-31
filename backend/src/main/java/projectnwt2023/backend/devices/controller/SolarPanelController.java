package projectnwt2023.backend.devices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.*;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.dto.model.BatteryDTO;
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.GraphRequestDTO;
import projectnwt2023.backend.devices.dto.model.SolarPanelDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.helper.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/device/largeEnergy")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SolarPanelController {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    InfluxDBService influxDBService;

    @Autowired
    Gateway mqttGateway;

    @Autowired
    IAppUserService appUserService;

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

    @GetMapping(value = "/charger/{deviceId}", produces = "application/json")
    ResponseEntity<ChargerDTO> getCharger(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        System.out.println(deviceId);
        Charger charger = (Charger) device;
//        if (charger.getAvailablePortNumber() == 0) {
//            charger.setAvailablePortNumber(charger.getPortNumber());
//            deviceService.save(charger);
//        }
        return new ResponseEntity<>(new ChargerDTO(charger), HttpStatus.OK);
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
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRecentGateEvents(@PathVariable Integer deviceId,
                                                                        @RequestParam String measurement){

        List<GateEventMeasurement> res = deviceService.getRecentEvents(Long.valueOf(deviceId), measurement);
        List<GateEventDTO> ret = new ArrayList<>();
        for (GateEventMeasurement event : res) {
            ret.add(new GateEventDTO(event.getCaller(), event.getValue(), String.valueOf(event.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

    @GetMapping(value = "/{deviceId}/range", produces = "application/json")
    ResponseEntity<ApiResponse<List<GateEventDTO>>> getRangeGateEvents(@PathVariable Integer deviceId,
                                                                       @RequestParam String start,
                                                                       @RequestParam String end,
                                                                       @RequestParam String measurement){

        List<GateEventMeasurement> res = deviceService.getDateRangeEvents(Long.valueOf(deviceId), start, end, measurement);
        List<GateEventDTO> ret = new ArrayList<>();
        for (GateEventMeasurement event : res) {
            ret.add(new GateEventDTO(event.getCaller(), event.getValue(), String.valueOf(event.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

    @PutMapping(value = "/charger/{deviceId}/port", produces = "application/json")
    ResponseEntity<ChargerDTO> changePort(@PathVariable Integer deviceId,
                                            @RequestParam("val") int percentage){
        Charger charger = (Charger) deviceService.getById(Long.valueOf(deviceId));
        charger.setPercentage(percentage);
        deviceService.save(charger);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<AppUser> user = appUserService.findByEmail(userDetails.getUsername());
        if (!user.isPresent()) return  new ResponseEntity<>(new ChargerDTO((charger)), HttpStatus.OK);
        mqttGateway.sendToMqtt(String.valueOf(percentage) + ";" + user.get().getEmail(), "charger/"+String.valueOf(deviceId)+"/port-set");
        return new ResponseEntity<>(new ChargerDTO((charger)), HttpStatus.OK);
    }


}
