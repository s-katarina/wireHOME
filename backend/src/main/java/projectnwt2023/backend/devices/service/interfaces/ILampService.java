package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Lamp;

public interface ILampService {

    Lamp changeBulbState(Long lampId, String requestType);
    Lamp setAutomaticRegime(Long lampId, String usedFor);

}
