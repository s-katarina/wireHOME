package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;
import projectnwt2023.backend.devices.dto.DeviceResponseDTO;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import javax.validation.Valid;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class RegistrationController {

    @Autowired
    IDeviceService registrationService;

    @Autowired
    IPropertyService propertyService;


    private ResponseEntity<DeviceResponseDTO> saveDevice(DeviceRequestDTO deviceRequestDTO, Device device) {
        Property property = propertyService.getById((long) deviceRequestDTO.getPropertyId());

        device.setProperty(property);
        registrationService.save(device);

        DeviceResponseDTO deviceResponseDTO = new DeviceResponseDTO(device);

        return new ResponseEntity<>(deviceResponseDTO, HttpStatus.OK);
    }
    @PostMapping(value = "/airConditioner", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveAirConditioner(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new AirConditioner(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/ambientSensor", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveAmbientSensor(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new AmbientSensor(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);

    }

    @PostMapping(value = "/battery", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveBattery(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Battery(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/charger", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveCharger(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Charger(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/gate", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveGate(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Gate(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/lamp", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveLamp(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Lamp(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/solarPanel", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveSolarPanel(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new SolarPanel(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/sprinkler", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveSprinkler(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Sprinkler(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/washingMachine", produces = "application/json", consumes = "application/json")
    public ResponseEntity<DeviceResponseDTO> saveWashingMachine(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new WashingMachine(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @GetMapping(value = "/regimes", produces = "application/json")
    public ResponseEntity<ArrayList<String>> getRegimes(){
        ArrayList<String> regimes = new ArrayList<>();
        for (RegimeType regime:RegimeType.values()) {
            regimes.add(String.valueOf(regime));
        }
        return new ResponseEntity<>(regimes, HttpStatus.OK);
    }

}
