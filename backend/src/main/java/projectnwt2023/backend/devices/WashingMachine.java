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
public class WashingMachine extends Device{

    private ArrayList<RegimeWashingMachine> availableRegimes;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "device")
    private Collection<Regime> programedRegimes;

    private String currentAction;

    public WashingMachine(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        ArrayList<RegimeWashingMachine> types = new ArrayList<>();
        for (String type:deviceRequestDTO.getRegimes()) {
            types.add(RegimeWashingMachine.valueOf(type));
        }
        this.availableRegimes = types;
        this.programedRegimes = new ArrayList<>();
        this.setTopic("washingMachine");
        this.setCurrentAction("OFF");
    }
}
