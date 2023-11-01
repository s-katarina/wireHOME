package projectnwt2023.backend.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Country;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class CityDTO {

    @NotNull
    private Integer id;

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private CountryDTO country;

    public CityDTO(City city){
        this.id = city.getId().intValue();
        this.name = city.getName();
        this.country = new CountryDTO(city.getCountry().getId().intValue(), city.getCountry().getName());
    }

}
