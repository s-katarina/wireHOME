package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
public class SolarPanel extends Device{
    private double surfaceSize;
    private double efficiency;

    public SolarPanel(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.surfaceSize = deviceRequestDTO.getPanelSize();
        this.efficiency = deviceRequestDTO.getEfficiency();
        this.setTopic("solarPanel");
    }
}
