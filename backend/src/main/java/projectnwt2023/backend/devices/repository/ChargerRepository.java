package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.Charger;
import projectnwt2023.backend.devices.Device;

import java.util.ArrayList;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    ArrayList<Charger> findByTopic(String battery);

}
