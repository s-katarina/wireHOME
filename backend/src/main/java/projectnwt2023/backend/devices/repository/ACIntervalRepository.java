package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;

import java.util.ArrayList;

public interface ACIntervalRepository extends JpaRepository<ACInterval, Long> {

    ArrayList<ACInterval> findByAirConditioner(AirConditioner airConditioner);
    void deleteById(Long id);
    ACInterval save(ACInterval acInterval);
}
