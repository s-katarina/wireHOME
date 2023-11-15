package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.State;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponseDTO {

    private int id;

    private String state;

    private String modelName;

    private boolean usesElectricity;

    private double consumptionAmount;

    private int propertyId;

    public DeviceResponseDTO(Device device) {
        this.id = Math.toIntExact(device.getId());
        this.state = String.valueOf(device.getState());
        this.modelName = device.getModelName();
        this.usesElectricity = device.getState() == State.online;
        this.consumptionAmount = device.getConsumptionAmount();
        this.propertyId = Math.toIntExact(device.getProperty().getId());

    }
}
