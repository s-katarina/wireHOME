package projectnwt2023.backend.devices.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SprinklerCommandMeasurement {
    private String name;
    private String value;
    private Date timestamp;
    private String caller;
}