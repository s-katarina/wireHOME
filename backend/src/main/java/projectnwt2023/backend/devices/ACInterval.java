package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.ACIntervalDTO;
import projectnwt2023.backend.property.Country;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class ACInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String startTime;
    private String endTime;
    @ManyToOne
    @JoinColumn(name = "air_conditioner_id")
    private AirConditioner airConditioner;
    private String action;

    public ACInterval(ACIntervalDTO acIntervalDTO, AirConditioner airConditioner) {
        this.startTime = acIntervalDTO.getStartTime();
        this.endTime = acIntervalDTO.getEndTime();
        this.airConditioner = airConditioner;
        this.action = acIntervalDTO.getAction();
    }

}
