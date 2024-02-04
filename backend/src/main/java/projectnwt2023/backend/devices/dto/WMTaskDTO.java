package projectnwt2023.backend.devices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.WMTask;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class WMTaskDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private String startTime;
    private String action;

    public WMTaskDTO(WMTask wmTask) {
        this.id = wmTask.getId();
        this.startTime = wmTask.getStartTime();
        this.action = wmTask.getAction();
    }

}
