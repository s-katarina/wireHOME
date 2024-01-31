package projectnwt2023.backend.devices.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.SolarPanel;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolarPanelDTO extends DeviceDTO {

    private double surfaceSize;

    private double efficiency;

    private double latitude;

    private double longitude;

    public SolarPanelDTO(SolarPanel device) {
        super(device);
        this.surfaceSize = device.getSurfaceSize();
        this.efficiency = device.getEfficiency();
        this.latitude = device.getProperty().getCity().getLatitude();
        this.longitude = device.getProperty().getCity().getLongitude();
    }
}
