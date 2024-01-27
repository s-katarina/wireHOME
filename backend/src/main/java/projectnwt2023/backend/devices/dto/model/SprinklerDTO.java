package projectnwt2023.backend.devices.dto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Sprinkler;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.SprinklerScheduleDTO;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;

@Getter
@Setter
@NoArgsConstructor
public class SprinklerDTO extends DeviceDTO {

    private boolean scheduleMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SprinklerScheduleDTO scheduleDTO;
    public SprinklerDTO(Sprinkler sprinkler) {
        this.setId(Math.toIntExact(sprinkler.getId()));
        this.setState(sprinkler.getState() == State.online);
        this.setModelName(sprinkler.getModelName());
        this.setUsesElectricity(sprinkler.isUsesElectricity());
        this.setConsumptionAmount(sprinkler.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(sprinkler.getProperty().getId()));
        this.setImagePath(sprinkler.getImagePath());
        this.setDeviceType(sprinkler.getTopic());
        this.setOn(sprinkler.isOn());
        this.setScheduleMode(sprinkler.isScheduleMode());
        if (sprinkler.isScheduleMode()) {
            this.setScheduleDTO(new SprinklerScheduleDTO(null, sprinkler.getSchedule().getStartHour(), sprinkler.getSchedule().getEndHour(), sprinkler.getSchedule().getWeekdays().stream().mapToInt(Integer::intValue).toArray()));
        }
    }
}
