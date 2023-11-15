package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}
