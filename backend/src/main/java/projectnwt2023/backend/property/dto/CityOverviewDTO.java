package projectnwt2023.backend.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CityOverviewDTO {
    private CityDTO city;
    private int propertyesNum;
    private double energy;
    private double electodistribution;
}
