package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.Measurement.BulbOnOffMeasurement;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;
import projectnwt2023.backend.devices.dto.model.LampDTO;
import projectnwt2023.backend.devices.dto.ValueTimestampDTO;
import projectnwt2023.backend.devices.dto.Measurement;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.ILampService;
import projectnwt2023.backend.helper.ApiResponse;

import java.sql.Date;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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

        try {
            Lamp device = (Lamp) deviceService.getById(deviceId.longValue());
            return new ResponseEntity<>(new LampDTO(device), HttpStatus.OK);

        } catch (ClassCastException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping(value = "/{deviceId}/range", produces = "application/json")
    ResponseEntity<ApiResponse<List<ValueTimestampDTO>>> getRangeLightSensor(@PathVariable Integer deviceId,
                                                                             @RequestParam String start,
                                                                             @RequestParam String end){

        List<Measurement> res = lampService.getDateRangeLightSensor(Long.valueOf(deviceId), start, end);
        List<ValueTimestampDTO> ret = new ArrayList<>();
        if (res != null) {
            for (Measurement measurement : res) {
                ret.add(new ValueTimestampDTO(String.valueOf(measurement.getValue()), String.valueOf( ( Date.from((measurement.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))).getTime() )));
            }
            return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/{deviceId}/range/bulb", produces = "application/json")
    ResponseEntity<ApiResponse<List<BulbOnOffMeasurement>>> getDateRangeBulb(@PathVariable Integer deviceId,
                                                                             @RequestParam String start,
                                                                             @RequestParam String end){

        List<BulbOnOffMeasurement> res = lampService.getDateRangeBulb(Long.valueOf(deviceId), start, end);
        if (res != null) {
            return new ResponseEntity<>(new ApiResponse<>(200, res), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

}
