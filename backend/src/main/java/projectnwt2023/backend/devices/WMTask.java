package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.ACIntervalDTO;
import projectnwt2023.backend.devices.dto.WMTaskDTO;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class WMTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String startTime;
    @ManyToOne
    @JoinColumn(name = "washing_machine_id")
    private WashingMachine washingMachine;
    private String action;

    public WMTask(WMTaskDTO wmTaskDTO, WashingMachine washingMachine) {
        this.startTime = wmTaskDTO.getStartTime();
        this.washingMachine = washingMachine;
        this.action = wmTaskDTO.getAction();
    }

}
