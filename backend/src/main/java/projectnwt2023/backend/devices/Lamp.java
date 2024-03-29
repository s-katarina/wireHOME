package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public class Lamp extends Device{
    //i ovo se automatski pali gasi mozda i tu ide lista rezima
//    private ArrayList<RegimeType> availableRegimes;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "device")
    private Collection<Regime> programedRegimes;

    private boolean isAutomatic;

    private boolean bulbState;

    public Lamp(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.programedRegimes = new ArrayList<>();
        this.setTopic("lamp");
        this.bulbState = false;
        this.isAutomatic = false;
    }
}
