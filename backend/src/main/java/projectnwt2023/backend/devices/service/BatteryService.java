package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IBatteryService;

import java.util.ArrayList;

@Service
public class BatteryService implements IBatteryService {

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public ArrayList<Battery> getBatteriesByPropertyId(Long propertyId) {
        return deviceRepository.findByTopicAndPropertyId("battery", propertyId);
    }

    @Override
    public ArrayList<Battery> getOnlineBatteriesByPropertyId(Long propertyId) {
        return deviceRepository.findByTopicAndPropertyIdAndState("battery", propertyId, State.online);
    }

    @Override
    public ArrayList<Battery> getAllBatteries() {
        return deviceRepository.findByTopic("battery");

    }

    @Override
    public ArrayList<Battery> getOnlineAllBatteries() {
        return deviceRepository.findByTopicAndState("battery", State.online);
    }
}
