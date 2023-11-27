package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Lamp;

public interface ILampService {

    boolean turnOn(Lamp lamp);
    boolean turnOff(Lamp lamp);

}
