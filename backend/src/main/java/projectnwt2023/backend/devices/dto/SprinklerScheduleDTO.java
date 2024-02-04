package projectnwt2023.backend.devices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SprinklerScheduleDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer deviceId;
    private Integer startHour;
    private Integer endHour;
    private int[] weekdays;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private String caller;
}
