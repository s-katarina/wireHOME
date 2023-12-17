package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GateEventMeasurement {
    private String name;
    private String value;
    private Date timestamp;
    private String caller;
}
