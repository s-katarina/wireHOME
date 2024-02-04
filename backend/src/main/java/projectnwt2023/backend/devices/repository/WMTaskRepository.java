package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.WMTask;
import projectnwt2023.backend.devices.WashingMachine;

import java.util.ArrayList;

public interface WMTaskRepository extends JpaRepository<WMTask, Long> {

    ArrayList<WMTask> findByWashingMachine(WashingMachine washingMachine);
//    void deleteById(Long id);
    WMTask save(WMTask wmTask);

}
