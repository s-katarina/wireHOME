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
public class Gate extends Device{
    private boolean isPublic;

    public Gate(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.isPublic = true;
    }
    //mogao bi u bazi eventualno da ima i spisak vozila ali necemo to sebi raditi
}
