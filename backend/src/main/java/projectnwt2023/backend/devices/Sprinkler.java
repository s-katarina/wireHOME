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
public class Sprinkler extends Device{
    private boolean isOn;
    public Sprinkler(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.isOn = false;
        this.setTopic("sprinkler");
    }
}
