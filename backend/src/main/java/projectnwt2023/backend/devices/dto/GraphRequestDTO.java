package projectnwt2023.backend.devices.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GraphRequestDTO {
    private String id;
    private String from;
    private String to;
    private String measurement;
}
