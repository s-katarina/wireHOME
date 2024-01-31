package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.WMTask;
import projectnwt2023.backend.devices.WashingMachine;

import java.util.ArrayList;

public interface IWashingMachineService {

    public void parseRequest(String topic, Message<?> message);
    public ArrayList<WMTask> findWMTaskByWashingMachine(WashingMachine washingMachine);
    public WMTask saveWMTask(WMTask wmTask);
    public void deleteWMTaskById(Long id);

}
