package projectnwt2023.backend.property.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.dto.GraphDTO;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabeledGraphDTO {

    private String label;
    private ArrayList<GraphDTO> graphDTOS;
}
