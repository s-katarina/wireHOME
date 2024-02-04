package projectnwt2023.backend.devices.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Battery;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatteryDTO extends DeviceDTO {
    private double capacity;
    private double currentFill;

    public BatteryDTO(Battery device) {
        super(device);
        this.capacity = device.getCapacity();
        this.currentFill = device.getCurrentFill();
    }
}
