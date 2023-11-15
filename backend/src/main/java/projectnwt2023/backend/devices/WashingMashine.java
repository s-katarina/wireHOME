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
public class WashingMashine extends Device{

    private ArrayList<RegimeType> availableRegimes;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "device")
    private Collection<Regime> programedRegimes;

    public WashingMashine(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.availableRegimes = new ArrayList<>();
        this.programedRegimes = new ArrayList<>();
    }
}
