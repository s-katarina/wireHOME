package projectnwt2023.backend.devices;
import lombok.*;

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

    private ArrayList<RegimeType> availableRegimes;

    @OneToMany(fetch = FetchType.LAZY,
    mappedBy = "device")
    private Collection<Regime> programedRegimes;

    private int temp;

    private int minTemp;

    private int maxTemp;
}
