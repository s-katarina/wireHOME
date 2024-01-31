package projectnwt2023.backend.property.dto;


import lombok.*;
import projectnwt2023.backend.devices.dto.GraphDTO;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LabeledGraphDTO {

    private String label;
    private ArrayList<GraphDTO> graphDTOS;
}
