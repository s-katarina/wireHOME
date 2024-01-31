package projectnwt2023.backend.devices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import projectnwt2023.backend.appUser.dto.AppUserDTO;
import projectnwt2023.backend.devices.SharedProperty;
import projectnwt2023.backend.property.dto.PropertyResponseDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SharedPropertyDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private AppUserDTO shareWith;
    private PropertyResponseDTO property;

    public SharedPropertyDTO(SharedProperty sharedProperty) {
        this.id = sharedProperty.getId();
        this.shareWith = new AppUserDTO(sharedProperty.getShareWith());
        this.property = new PropertyResponseDTO(sharedProperty.getProperty());
    }

}
