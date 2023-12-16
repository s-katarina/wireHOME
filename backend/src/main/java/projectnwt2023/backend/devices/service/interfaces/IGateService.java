package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
import projectnwt2023.backend.devices.dto.GateEventPayloadDTO;
import projectnwt2023.backend.devices.dto.PayloadDTO;

import java.util.List;

public interface IGateService {

    public void parseRequest(String topic, Message<?> request);
    public Gate changeGateRegime(Long gateId, String regime);
    public Gate changeGateOpen(Long gateId, String openClose);
    void processEvent(String payload);

    public List<GateEventMeasurement> getRecentGateEvents(Long gateId);
    public List<GateEventMeasurement> getDateRangeGateEvents(Long gateId, String start, String end);
}
