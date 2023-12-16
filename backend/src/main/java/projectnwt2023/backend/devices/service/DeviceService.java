package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
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

    @Autowired
    IAppUserService appUserService;

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
        if (device.getState() != state) {
            //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        AppUser sender = appUserService.findByEmail(authentication.getName());
            Map<String, String> values = new HashMap<>();
            values.put("device-id", String.valueOf(device.getId()));
            values.put("caller", "kata");
            influxDBService.save("online/offline", state.getNumericValue(), new Date(), values);
        }
        device.setState(state);
        device.setLastHeartbeat(LocalDateTime.now());

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
        if (device.getState() == State.offline){
            System.out.println("offline je");
            return null;
        }
        device.setDeviceOn(isOn);
        device.setLastHeartbeat(LocalDateTime.now());
        Map<String, String> values = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Optional<AppUser> sender = appUserService.findByEmail(authentication.getName());
            if (!sender.isPresent()) return null;
            values.put("caller", sender.get().getEmail());
        } else {
            values.put("caller", "kata");
        }
        values.put("device-id", String.valueOf(device.getId()));

        influxDBService.save("on/off", isOn ? 1 : 0, new Date(), values);
        return deviceRepository.save(device);
    }

    @Override
    public List<GateEventMeasurement> getDateRangeEvents(Long id, String start, String end) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        try {
            List<GateEventMeasurement> res = influxDBService.findDateRangeEvents(String.valueOf(id), Long.parseLong(start), Long.parseLong(end), "on/off");
            return res;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<GateEventMeasurement> getRecentEvents(Long id) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        return influxDBService.findRecentEvents(id.toString(), "on/off");
    }

}
