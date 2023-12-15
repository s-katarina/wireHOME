package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeviceService implements IDeviceService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    InfluxDBService influxDBService;

    @Override
    public Device save(Device device) {
        Device saved = deviceRepository.save(device);
        saved.setImagePath((String.format("images/device-%s.jpg", saved.getId())));
        return deviceRepository.save(saved);
    }

    @Override
    public Device getById(Long id) {
        Optional<Device> device = deviceRepository.findById(id);

        if (!device.isPresent())
            throw new EntityNotFoundException(Device.class);

        return device.get();
    }

    @Override
    public Device changeDeviceState(Long id, State state) {
        Device device = getById(id);
        device.setState(state);
        device.setLastHeartbeat(LocalDateTime.now());
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        AppUser sender = appUserService.findByEmail(authentication.getName());
        Map<String, String> values = new HashMap<>();
        values.put("device-id", String.valueOf(device.getId()));
        values.put("user-email", "kata");
        influxDBService.save("online/offline", state.getNumericValue(), new Date(), values);
        return deviceRepository.save(device);
    }

    @Override
    public ArrayList<Device> getAppliancesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("airConditioner", "ambientSensor", "washingMachine"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public ArrayList<Device> getOutdoorDevicesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("gate", "lamp", "sprinkler"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public ArrayList<Device> getElectricalDevicesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("battery", "charger", "solarPanel"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public Device changeDeviceOnOff(long deviceId, boolean isOn) {
        Device device = getById(deviceId);
        device.setDeviceOn(isOn);
        device.setLastHeartbeat(LocalDateTime.now());
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        AppUser sender = appUserService.findByEmail(authentication.getName());
        Map<String, String> values = new HashMap<>();
        values.put("device-id", String.valueOf(device.getId()));
        values.put("user-email", "kata");

        influxDBService.save("on/off", isOn ? 1 : 0, new Date(), values);
        return deviceRepository.save(device);
    }

}
