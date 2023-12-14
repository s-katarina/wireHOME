package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.PayloadDTO;

public interface ILampService {

    void parseRequest(String topic, PayloadDTO payloadDTO);

    Lamp changeBulbState(Long lampId, String requestType);
    Lamp setAutomaticRegime(Long lampId, String usedFor);

}
