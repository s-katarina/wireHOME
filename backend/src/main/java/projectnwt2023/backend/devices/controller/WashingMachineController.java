package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.WMTask;
import projectnwt2023.backend.devices.WashingMachine;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IAirConditionerService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.IWashingMachineService;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/washingMachine")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class WashingMachineController {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IWashingMachineService washingMachineService;

    @Autowired
    private InfluxDBService influxDBService;

    @Autowired
    Gateway mqttGateway;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<WashingMachineDTO> getWashingMachine(@PathVariable Integer deviceId) {
        WashingMachine device = (WashingMachine) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new WashingMachineDTO(device), HttpStatus.OK);
    }

    @PostMapping(value = "/{deviceId}/action", produces = "text/plain")
    void postActionRequest(@PathVariable Integer deviceId, @RequestBody AirConditionerActionRequest request) {

        // posalji mqtt simulatoru   airConditioner/3/request   colling;3sparklez.cat@gmail.com
        mqttGateway.sendToMqtt(request.getAction() + ";" + request.getUserEmail(), "washingMachine/" + deviceId + "/request");
    }

    @GetMapping(value = "/{deviceId}/actions", produces = "application/json")
    ResponseEntity<ArrayList<AirConditionerActionDTO>> getWashingMachineActions(@PathVariable Integer deviceId) {

        ArrayList<AirConditionerActionDTO> ret = influxDBService.getAllWashingMachineActions(deviceId);

        return new ResponseEntity<>(ret, HttpStatus.OK);

    }

    @GetMapping(value = "/{deviceId}/wmtasks", produces = "application/json")
    ResponseEntity<ArrayList<WMTaskDTO>> getWashingMachineWMTasks(@PathVariable Integer deviceId) {

        WashingMachine device = (WashingMachine) deviceService.getById(deviceId.longValue());
        ArrayList<WMTask> wmTasks = washingMachineService.findWMTaskByWashingMachine(device);
        ArrayList<WMTaskDTO> ret = new ArrayList<>();
        for (WMTask wmTask : wmTasks)
            ret.add(new WMTaskDTO(wmTask));

        return new ResponseEntity<>(ret, HttpStatus.OK);

    }

    @DeleteMapping(value = "/{deviceId}/wmtask/{wmtasklId}", produces = "application/json")
    void deleteWashingMachineTask(@PathVariable Integer deviceId, @PathVariable Integer wmtasklId) {
        washingMachineService.deleteWMTaskById((long) wmtasklId);
    }

    @PostMapping(value = "/{deviceId}/wmtask", produces = "application/json")
    ResponseEntity<WMTaskDTO> addWashingMachineTask(@PathVariable Integer deviceId, @RequestBody WMTaskDTO wmTaskDTO) {

        WashingMachine washingMachine = (WashingMachine) deviceService.getById((long) deviceId);
        WMTask wmTask = new WMTask(wmTaskDTO, washingMachine);
        WMTask saved = washingMachineService.saveWMTask(wmTask);

        return new ResponseEntity<>(new WMTaskDTO(saved), HttpStatus.OK);

    }

}
