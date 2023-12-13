package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.dto.PayloadDTO;

public interface IGateService {

    public void parseRequest(String topic, PayloadDTO payloadDTO);
    public Gate changeGateRegime(Long gateId, String regime);
    public Gate changeGateOpen(Long gateId, String openClose);

}
