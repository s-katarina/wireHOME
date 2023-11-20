package projectnwt2023.backend.devices;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table
public class Regime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;

    private LocalTime endTime;

    private RegimeType regimeType;

    @ManyToOne
    @JoinColumn(name="device_id", nullable=false)
    private Device device;
}
