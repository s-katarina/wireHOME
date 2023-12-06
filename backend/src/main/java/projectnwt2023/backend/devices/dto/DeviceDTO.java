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
public class DeviceDTO {

    private int id;

    private boolean state;

    private String modelName;

    private boolean usesElectricity;

    private double consumptionAmount;

    private int propertyId;

    private String imagePath;

    private String deviceType;

    public DeviceDTO(Device device) {
        this.id = Math.toIntExact(device.getId());
        this.state = device.getState() == State.online;
        this.modelName = device.getModelName();
        this.usesElectricity = device.isUsesElectricity();
        this.consumptionAmount = device.getConsumptionAmount();
        this.propertyId = Math.toIntExact(device.getProperty().getId());
        this.imagePath = device.getImagePath();
        this.deviceType = device.getTopic();

    }
}
