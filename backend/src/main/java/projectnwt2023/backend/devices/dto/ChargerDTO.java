package projectnwt2023.backend.devices.dto;

import lombok.*;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.Charger;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChargerDTO extends DeviceDTO {
    private double chargingStrength;
    private int portNumber;

    private int availablePortNumber;
    private int percentage;

    public ChargerDTO(Charger device) {
        super(device);
        this.chargingStrength = device.getChargingStrength();
        this.portNumber = device.getPortNumber();
        this.availablePortNumber = device.getAvailablePortNumber();
        this.percentage = device.getPercentage();
    }
}
