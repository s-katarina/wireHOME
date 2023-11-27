package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.ILampService;

@Service
public class LampService implements ILampService {

    @Autowired
    Gateway mqttGateway;

    static String topic = "simulation/lamp/";

    @Override
    public boolean turnOn(Lamp lamp) {
        // write to influx
        mqttGateway.sendToMqtt("ON", topic + lamp.getId() + "/on");
        return true;
    }

    @Override
    public boolean turnOff(Lamp lamp) {
        // write to influx
        mqttGateway.sendToMqtt("ON", topic + lamp.getId() + "/off");
        return true;
    }
}
