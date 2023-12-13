package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.SolarPanel;
import projectnwt2023.backend.devices.State;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolarPanelDTO extends DeviceDTO{
    private int id;

    private boolean state;

    private String modelName;

    private boolean usesElectricity;

    private double consumptionAmount;

    private int propertyId;

    private double surfaceSize;

    private double efficiency;

    private double latitude;

    private double longitude;

    public SolarPanelDTO(SolarPanel device) {
        this.id = Math.toIntExact(device.getId());
        this.state = device.getState() == State.online;
        this.modelName = device.getModelName();
        this.usesElectricity = device.isUsesElectricity();
        this.consumptionAmount = device.getConsumptionAmount();
        this.surfaceSize = device.getSurfaceSize();
        this.efficiency = device.getEfficiency();
        this.latitude = device.getProperty().getCity().getLatitude();
        this.longitude = device.getProperty().getCity().getLongitude();
        this.propertyId = Math.toIntExact(device.getProperty().getId());
    }
}
