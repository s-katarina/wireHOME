package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.Measurement.BulbOnOffMeasurement;
import projectnwt2023.backend.devices.dto.Measurement;
import projectnwt2023.backend.devices.dto.PayloadDTO;

import java.util.List;

public interface ILampService {

    void parseRequest(String topic, PayloadDTO payloadDTO);

    Lamp changeBulbState(Long lampId, String requestType);
    Lamp setAutomaticRegime(Long lampId, String usedFor);
    public List<Measurement> getDateRangeLightSensor(Long gateId, String start, String end);


    List<BulbOnOffMeasurement> getDateRangeBulb(Long gateId, String start, String end);
}
