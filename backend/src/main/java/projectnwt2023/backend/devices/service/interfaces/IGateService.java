package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Gate;

public interface IGateService {

    public Gate changeGateRegime(Long gateId, String regime);
    public Gate changeGateOpen(Long gateId, String openClose);

}
