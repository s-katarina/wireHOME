package projectnwt2023.backend.property.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Setter
public class CityGraphDTO {
    private Long id;
    private Long from;
    private Long to;
    private String measurement;
    private String whatId;
}
