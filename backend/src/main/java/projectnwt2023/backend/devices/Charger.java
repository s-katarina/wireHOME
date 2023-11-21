package projectnwt2023.backend.devices;
import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
public class Charger extends Device{
    private double chargingStrength;
    private int portNumber;
    private int percentage;

    public Charger(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.chargingStrength = deviceRequestDTO.getCapacity();
        this.portNumber = deviceRequestDTO.getPortNumber();
        this.percentage = 100;
//        this.setTopic("charger");
    }
}
