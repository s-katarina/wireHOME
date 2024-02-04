package projectnwt2023.backend.devices;

import lombok.*;
import org.springframework.transaction.annotation.Transactional;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
public class Sprinkler extends Device{
    private boolean isOn;
    private boolean scheduleMode;

    @Embedded
    private SprinklerSchedule schedule;
    public Sprinkler(DeviceRequestDTO deviceRequestDTO) {
        super(deviceRequestDTO);
        this.isOn = false;
        this.setTopic("sprinkler");
        this.scheduleMode = false;
    }

    public void CustomSetSchedule(int startHour, int endHour, int[] weekdays){
        SprinklerSchedule schedule = new SprinklerSchedule();
        schedule.setStartHour(startHour);
        schedule.setEndHour(endHour);
        schedule.setWeekdays(Arrays.stream(weekdays)
                .boxed()
                .collect(Collectors.toSet()));
        this.schedule = schedule;
    }

}

