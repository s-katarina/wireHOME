package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.Device;

import java.util.ArrayList;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    ArrayList<Device> findByTopicInAndPropertyId(ArrayList<String> types, Long propertyId);

    ArrayList<Battery> findByTopicAndPropertyId(String battery, Long propertyId);
}
