package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;

import java.util.ArrayList;

public interface IAirConditionerService {

    public void parseRequest(String topic, Message<?> message);
    public ArrayList<ACInterval> findIntervalsByAirConditioner(AirConditioner airConditioner);
    public ACInterval saveInterval(ACInterval acInterval);
    public void deleteIntervalById(Long id);

}
