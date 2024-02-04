package projectnwt2023.backend.property.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ByTimeOfDayDTO {
    private double dayElec;
    private double nightElec;
    private double dayDist;
    private double nightDist;
}
