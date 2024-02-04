package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.SharedDevice;
import projectnwt2023.backend.devices.SharedProperty;
import projectnwt2023.backend.devices.dto.ShareActionDTO;
import projectnwt2023.backend.devices.dto.SharedDeviceDTO;
import projectnwt2023.backend.devices.dto.SharedPropertyDTO;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.ISharingService;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/sharing")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SharingController {

    @Autowired
    private ISharingService sharingService;
    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private IAppUserService appUserService;
    @Autowired
    private IPropertyService propertyService;

    @GetMapping(value = "/property/{shareWithId}", produces = "application/json")
    ResponseEntity<ArrayList<SharedPropertyDTO>> getSharedProperties(@PathVariable Integer shareWithId) {

        Optional<AppUser> shareWith = appUserService.findById((long) shareWithId);
        ArrayList<SharedProperty> properties = sharingService.findAllSharedPropertiesByShareWith(shareWith.get());

        ArrayList<SharedPropertyDTO> ret = new ArrayList<>();
        for (SharedProperty property : properties)
            ret.add(new SharedPropertyDTO(property));

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @PostMapping(value = "/property", produces = "application/json")
    ResponseEntity<SharedPropertyDTO> addSharedProperty(@RequestBody ShareActionDTO shareActionDTO) {

        Optional<AppUser> shareWith = appUserService.findByEmail(shareActionDTO.getEmail());
        Property property = propertyService.getById(shareActionDTO.getId());

        SharedProperty sharedProperty = new SharedProperty();
        sharedProperty.setShareWith(shareWith.get());
        sharedProperty.setProperty(property);

        SharedProperty saved = sharingService.saveSharedProperty(sharedProperty);

        return new ResponseEntity<>(new SharedPropertyDTO(saved), HttpStatus.OK);
    }

    @PostMapping(value = "/property/delete", produces = "application/json")
    void deleteSharedProperties(@RequestBody ShareActionDTO shareActionDTO) {
        Optional<AppUser> shareWith = appUserService.findByEmail(shareActionDTO.getEmail());

        for (SharedProperty sharedProperty : sharingService.findAllSharedPropertiesByShareWith(shareWith.get()))
            if (sharedProperty.getProperty().getId() == shareActionDTO.getId())
                sharingService.deleteSharedPropertyById(sharedProperty.getId());
    }

    @GetMapping(value = "/device/{shareWithId}", produces = "application/json")
    ResponseEntity<ArrayList<SharedDeviceDTO>> getSharedDevices(@PathVariable Integer shareWithId) {

        Optional<AppUser> shareWith = appUserService.findById((long) shareWithId);
        ArrayList<SharedDevice> devices = sharingService.findAllSharedDevicesByShareWith(shareWith.get());

        ArrayList<SharedDeviceDTO> ret = new ArrayList<>();
        for (SharedDevice device : devices)
            ret.add(new SharedDeviceDTO(device));

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @PostMapping(value = "/device", produces = "application/json")
    ResponseEntity<SharedDeviceDTO> addSharedDevice(@RequestBody ShareActionDTO shareActionDTO) {

        Optional<AppUser> shareWith = appUserService.findByEmail(shareActionDTO.getEmail());
        Device device = deviceService.getById(shareActionDTO.getId());

        SharedDevice sharedDevice = new SharedDevice();
        sharedDevice.setShareWith(shareWith.get());
        sharedDevice.setDevice(device);

        SharedDevice saved = sharingService.saveSharedDevice(sharedDevice);

        return new ResponseEntity<>(new SharedDeviceDTO(saved), HttpStatus.OK);
    }

    @PostMapping(value = "/device/delete", produces = "application/json")
    void deleteSharedDevice(@RequestBody ShareActionDTO shareActionDTO) {

        Optional<AppUser> shareWith = appUserService.findByEmail(shareActionDTO.getEmail());

        for (SharedDevice sharedDevice : sharingService.findAllSharedDevicesByShareWith(shareWith.get()))
            if (sharedDevice.getDevice().getId() == shareActionDTO.getId())
                sharingService.deleteSharedDeviceById(sharedDevice.getId());
    }

}
