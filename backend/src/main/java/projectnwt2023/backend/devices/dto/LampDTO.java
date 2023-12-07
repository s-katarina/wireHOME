package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LampDTO extends DeviceDTO {

    private boolean bulbState;

    public LampDTO(Lamp lamp) {
        this.setId(Math.toIntExact(lamp.getId()));
        this.setState(lamp.getState() == State.online);
        this.setModelName(lamp.getModelName());
        this.setUsesElectricity(lamp.isUsesElectricity());
        this.setConsumptionAmount(lamp.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(lamp.getProperty().getId()));
        this.setImagePath(lamp.getImagePath());
        this.setDeviceType(lamp.getTopic());
        this.setBulbState(lamp.isBulbState());
    }
}
