package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.devices.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class RegistrationController {

    @Autowired
    IDeviceService registrationService;

    @Autowired
    IPropertyService propertyService;


    private ResponseEntity<ApiResponse<DeviceDTO>> saveDevice(DeviceRequestDTO deviceRequestDTO, Device device) {
        Property property = propertyService.getById((long) deviceRequestDTO.getPropertyId());

        device.setProperty(property);
        registrationService.save(device);

        ApiResponse<DeviceDTO> response = new ApiResponse<>(200, new DeviceDTO(device));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping(value = "/airConditioner", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveAirConditioner(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new AirConditioner(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/ambientSensor", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveAmbientSensor(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
//        System.out.println("AAAAAAAAAAa");
        Device device = new AmbientSensor(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);

    }

    @PostMapping(value = "/battery", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveBattery(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Battery(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/charger", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveCharger(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Charger(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/gate", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveGate(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Gate(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/lamp", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveLamp(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Lamp(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/solarPanel", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveSolarPanel(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new SolarPanel(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/sprinkler", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveSprinkler(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new Sprinkler(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }

    @PostMapping(value = "/washingMachine", produces = "application/json", consumes = "application/json")
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<DeviceDTO>> saveWashingMachine(@Valid @RequestBody DeviceRequestDTO deviceRequestDTO){
        Device device = new WashingMachine(deviceRequestDTO);
        return saveDevice(deviceRequestDTO, device);
    }



}
