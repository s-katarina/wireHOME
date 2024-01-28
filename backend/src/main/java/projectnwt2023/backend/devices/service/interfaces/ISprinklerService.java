package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;
import projectnwt2023.backend.devices.Sprinkler;
import projectnwt2023.backend.devices.dto.SprinklerCommandMeasurement;
import projectnwt2023.backend.devices.dto.SprinklerScheduleDTO;

import java.util.List;

public interface ISprinklerService {
    public void parseRequest(String topic, Message<?> request);
    public Sprinkler changeOn(Long sprinklerId, String usedFor);

    Sprinkler setSchedule(SprinklerScheduleDTO sprinklerScheduleDTO);

    Sprinkler turnOffSchedule(Integer deviceId);

    List<SprinklerCommandMeasurement> getRecentCommands(Long gateId);

    List<SprinklerCommandMeasurement> getDateRangeCommands(Long gateId, String start, String end);
}
