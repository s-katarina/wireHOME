package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.AmbientSensor;
import projectnwt2023.backend.devices.RegimeAirConditioner;
import projectnwt2023.backend.devices.State;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class AirConditionerDTO extends DeviceDTO {

    private ArrayList<RegimeAirConditioner> regimes;
    private String currentAction;
    private int temp;
    private int minTemp;
    private int maxTemp;

    public AirConditionerDTO(AirConditioner airConditioner) {
        this.setId(Math.toIntExact(airConditioner.getId()));
        this.setState(airConditioner.getState() == State.online);
        this.setModelName(airConditioner.getModelName());
        this.setUsesElectricity(airConditioner.isUsesElectricity());
        this.setConsumptionAmount(airConditioner.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(airConditioner.getProperty().getId()));
        this.setImagePath(airConditioner.getImagePath());
        this.setDeviceType(airConditioner.getTopic());

        this.setRegimes(airConditioner.getAvailableRegimes());
        this.setCurrentAction(airConditioner.getCurrentAction());
        this.setTemp(airConditioner.getTemp());
        this.setMinTemp(airConditioner.getMinTemp());
        this.setMaxTemp(airConditioner.getMaxTemp());
    }

}
