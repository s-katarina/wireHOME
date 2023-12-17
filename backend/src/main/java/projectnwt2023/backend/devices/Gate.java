package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "licence_plates")
    private List<String> licencePlates = new ArrayList<>();

    public Gate(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.isPublic = true;
        this.isOpen = false;
        this.setTopic("gate");

    }

    public void addLicencePlate(String licencePlate) {
        this.licencePlates.add(licencePlate);
    }
}
