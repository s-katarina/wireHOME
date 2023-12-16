package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;

public interface IAmbientSensorService {

    public void parseRequest(String topic, Message<?> message);

}
