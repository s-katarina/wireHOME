package projectnwt2023.backend.devices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ACIntervalDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private String startTime;
    private String endTime;
    private String action;

    public ACIntervalDTO(ACInterval acInterval) {
        this.id = acInterval.getId();
        this.startTime = acInterval.getStartTime();
        this.endTime = acInterval.getEndTime();
        this.action = acInterval.getAction();
    }

}
