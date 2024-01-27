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

    public Integer getScheduleStart() {
        return this.schedule.getStartHour();
    }

    public Integer getScheduleEnd() {
        return this.schedule.getEndHour();
    }
    @Transactional
    public List<Integer> getScheduleWeekdays() {
        if (this.schedule.getWeekdays() != null) {
            return new ArrayList<>(this.schedule.getWeekdays());
        }
        return new ArrayList<>();
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Embeddable
class SprinklerSchedule {
    private Integer startHour;
    private Integer endHour;
    @ElementCollection
    @CollectionTable(name = "sprinkler_schedule_weekdays", joinColumns = @JoinColumn(name = "sprinkler_id"))
    @Column(name = "weekday")
    private Set<Integer> weekdays;
}
