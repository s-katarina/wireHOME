package projectnwt2023.backend.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.property.Property;
import javax.validation.Valid;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequestDTO {

    @NotBlank(message = "Field property type is required")
    @Pattern(regexp = "HOUSE|APARTMENT", message="Field property type has incorrect value")
    private String propertyType;

    @NotBlank(message = "Field address is required")
    @Size(max = 50, message = "Field address cannot be longer than 50 characters")
    private String address;

//    @Valid
//    @NotNull
//    private CityDTO cityDTO;

    @NotNull
    private Integer cityId;

//    private Account propertyOwner;

    @NotNull
    @Max(value = 10000, message = "Field area must be less than or equal to 10000")
    @Min(value = 0, message = "Field area must be greater than 0")
    private double area;

    @NotNull
    @Max(value = 100, message = "Field floor count must be less than or equal to 100")
    @Min(value = 0, message = "Field floor count must be greater than 0")
    private int floorCount;

}
