package projectnwt2023.backend.devices.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EnergyDTO {
    private String name;
    private int deviceId;
    private int propertyId;
    private double consumptionAmount;
    private Date timestamp;
}
