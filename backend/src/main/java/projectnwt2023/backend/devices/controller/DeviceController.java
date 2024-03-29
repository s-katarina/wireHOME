package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.RegimeAirConditioner;
import projectnwt2023.backend.devices.RegimeType;
import projectnwt2023.backend.devices.RegimeWashingMachine;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.helper.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class DeviceController {

    @Autowired
    IDeviceService deviceService;
    @Autowired
    IAppUserService appUserService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<DeviceDTO> getDevice(@PathVariable Integer deviceId){

        Device device = deviceService.getById(deviceId.longValue());
        //System.out.Println(deviceId);
        return new ResponseEntity<>(new DeviceDTO(device), HttpStatus.OK);
    }

    @PostMapping(value = "/on/{deviceId}", produces = "application/json")
    ResponseEntity<MessageDTO> turnOn(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            //System.out.Println(deviceId);
            mqttGateway.sendToMqtt("ON", String.valueOf(deviceId));
            return new ResponseEntity<>(new MessageDTO("uspeo je", "on"), HttpStatus.OK);
        } catch (Exception e){
            //System.out.Println("greeeska");
            e.printStackTrace();
            return new ResponseEntity<>(new MessageDTO("nije uspeo", "on"), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/off/{deviceId}", produces = "application/json")
    ResponseEntity<?> turnOff(@PathVariable Integer deviceId){
        Device device = deviceService.getById(deviceId.longValue());
        try {
            mqttGateway.sendToMqtt("OFF", String.valueOf(deviceId));
            return new ResponseEntity<>(new MessageDTO("uspeo je", "off"), HttpStatus.OK);
        } catch (Exception e){
            System.out.println("greeeska");
            e.printStackTrace();
            return new ResponseEntity<>(new MessageDTO("nije uspeo", "off"), HttpStatus.OK);
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

    @GetMapping(value = "/appliances/{propertyId}", produces = "application/json")
    public ResponseEntity<ArrayList<DeviceDTO>> getAppliances(@PathVariable Integer propertyId){
        ArrayList<Device> devices = deviceService.getAppliancesByProperty(Long.valueOf(propertyId));
        ArrayList<DeviceDTO> devicedtos = new ArrayList<>();
        for (Device device: devices) {
            devicedtos.add(new DeviceDTO(device));
        }
        return new ResponseEntity<>(devicedtos, HttpStatus.OK);
    }

    @GetMapping(value = "/outdoor/{propertyId}", produces = "application/json")
    public ResponseEntity<ArrayList<DeviceDTO>> getOutdoor(@PathVariable Integer propertyId){
        ArrayList<Device> devices = deviceService.getOutdoorDevicesByProperty(Long.valueOf(propertyId));
        ArrayList<DeviceDTO> devicedtos = new ArrayList<>();
        for (Device device: devices) {
            devicedtos.add(new DeviceDTO(device));
        }
        return new ResponseEntity<>(devicedtos, HttpStatus.OK);
    }

    @GetMapping(value = "/energyDevices/{propertyId}", produces = "application/json")
    public ResponseEntity<ArrayList<DeviceDTO>> getEnergyDevices(@PathVariable Integer propertyId){
        ArrayList<Device> devices = deviceService.getElectricalDevicesByProperty(Long.valueOf(propertyId));
        ArrayList<DeviceDTO> devicedtos = new ArrayList<>();
        for (Device device: devices) {
            devicedtos.add(new DeviceDTO(device));
        }
        return new ResponseEntity<>(devicedtos, HttpStatus.OK);
    }



    @GetMapping(value = "/onlinePercent/{deviceId}", produces = "application/json") // koristi i za elektrodistribuciju i za samu potrosnju
    ResponseEntity<ArrayList<PyChartDTO>> getOnlinePercentageInRange(@PathVariable Integer deviceId,
                                                                     @RequestParam String start,
                                                                     @RequestParam String end){
        ArrayList<PyChartDTO> graphData = deviceService.getOnlineOfflineTime(deviceId, start, end);
        if (graphData == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(graphData, HttpStatus.OK);
    }

    @GetMapping(value = "/onlineIntervals/{deviceId}", produces = "application/json")
    ResponseEntity<ApiResponse<List<ValueTimestampDTO>>> getOnlineIntervals(@PathVariable Integer deviceId,
                                                                     @RequestParam String start,
                                                                     @RequestParam String end){
        ArrayList<GateEventMeasurement> res = deviceService.getOnlineOfflineIntervals(deviceId, start, end);
        List<ValueTimestampDTO> ret = new ArrayList<>();
        if (res != null) {
            for (GateEventMeasurement measurement : res) {
                ret.add(new ValueTimestampDTO(String.valueOf(measurement.getValue()), String.valueOf(measurement.getTimestamp().getTime())));
            }
            return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/onlinePerUnit/{deviceId}", produces = "application/json")
    ResponseEntity<ApiResponse<List<ValueTimestampDTO>>> getOnlinePerUnit(@PathVariable Integer deviceId,
                                                                            @RequestParam String start,
                                                                            @RequestParam String end){
        List<ValueTimestampDTO> ret = deviceService.getOnlinePerTimeUnit(deviceId, start, end);
        if (ret != null) {
            return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/owner/{ownerId}", produces = "application/json")
    ResponseEntity<ArrayList<DeviceDTO>> getDevicesByOwner(@PathVariable Long ownerId) {

        Optional<AppUser> owner = appUserService.findById(ownerId);
        ArrayList<Device> devices = deviceService.findAllByOwnerOfProperty(owner.get());
        ArrayList<DeviceDTO> ret = new ArrayList<>();

        for (Device device : devices)
            ret.add(new DeviceDTO(device));

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

}
