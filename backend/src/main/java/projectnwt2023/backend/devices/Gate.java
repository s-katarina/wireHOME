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

    private boolean isOpen;

    public Gate(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.isPublic = true;
        this.isOpen = false;
        this.setTopic("gate");

    }
    //mogao bi u bazi eventualno da ima i spisak vozila ali necemo to sebi raditi
}
