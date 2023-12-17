package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.AmbientSensor;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;

@Getter
@Setter
@NoArgsConstructor
public class AmbientSensorDTO extends DeviceDTO {

    public AmbientSensorDTO(AmbientSensor ambientSensor) {
        this.setId(Math.toIntExact(ambientSensor.getId()));
        this.setState(ambientSensor.getState() == State.online);
        this.setModelName(ambientSensor.getModelName());
        this.setUsesElectricity(ambientSensor.isUsesElectricity());
        this.setConsumptionAmount(ambientSensor.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(ambientSensor.getProperty().getId()));
        this.setImagePath(ambientSensor.getImagePath());
        this.setDeviceType(ambientSensor.getTopic());
    }

}
