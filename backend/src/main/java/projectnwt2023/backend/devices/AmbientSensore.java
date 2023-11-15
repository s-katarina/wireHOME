package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@NoArgsConstructor
//@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
public class AmbientSensore extends Device{
    public AmbientSensore(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
    }
}
