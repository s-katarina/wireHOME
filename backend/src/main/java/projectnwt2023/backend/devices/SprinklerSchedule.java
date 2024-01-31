package projectnwt2023.backend.devices;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Embeddable
public class SprinklerSchedule {
    private Integer startHour;
    private Integer endHour;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sprinkler_schedule_weekdays", joinColumns = @JoinColumn(name = "sprinkler_id"))
    @Column(name = "weekday")
    private Set<Integer> weekdays;
}
