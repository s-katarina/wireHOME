package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.*;

import java.util.ArrayList;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    ArrayList<Device> findByTopicInAndPropertyId(ArrayList<String> types, Long propertyId);

    ArrayList<Battery> findByTopicAndPropertyId(String battery, Long propertyId);

    ArrayList<Battery> findByTopic(String battery);

    ArrayList<Battery> findByTopicAndPropertyIdAndState(String battery, Long propertyId, State online);

    ArrayList<Battery> findByTopicAndState(String battery, State online);

    ArrayList<Device> findByState(State online);
}
