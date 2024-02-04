package projectnwt2023.backend.devices.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.*;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class WashingMachineDTO extends DeviceDTO {

    private ArrayList<RegimeWashingMachine> regimes;
    private String currentAction;

    public WashingMachineDTO(WashingMachine washingMachine) {
        this.setId(Math.toIntExact(washingMachine.getId()));
        this.setState(washingMachine.getState() == State.online);
        this.setModelName(washingMachine.getModelName());
        this.setUsesElectricity(washingMachine.isUsesElectricity());
        this.setConsumptionAmount(washingMachine.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(washingMachine.getProperty().getId()));
        this.setImagePath(washingMachine.getImagePath());
        this.setDeviceType(washingMachine.getTopic());

        this.setRegimes(washingMachine.getAvailableRegimes());
        this.setCurrentAction(washingMachine.getCurrentAction());
    }

}
