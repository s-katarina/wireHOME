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
public class AirConditioner extends Device{

    private ArrayList<RegimeAirConditioner> availableRegimes;

    @OneToMany(fetch = FetchType.LAZY,
    mappedBy = "device")
    private Collection<Regime> programedRegimes;

    private int temp;

    private int minTemp;

    private int maxTemp;

    public AirConditioner(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        ArrayList<RegimeAirConditioner> types = new ArrayList<>();
        for (String type:deviceRequestDTO.getRegimes()) {
            types.add(RegimeAirConditioner.valueOf(type));
        }
        this.availableRegimes = types;
        this.programedRegimes = new ArrayList<>();
        this.temp = deviceRequestDTO.getMinTemp();
        this.maxTemp = deviceRequestDTO.getMaxTemp();
        this.minTemp = deviceRequestDTO.getMinTemp();
//        this.setTopic("airConditioner");
    }
}
