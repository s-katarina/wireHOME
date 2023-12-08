package projectnwt2023.backend.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Country;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CityDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private CountryDTO country;

    public CityDTO(City city){
        this.id = city.getId();
        this.name = city.getName();
        this.country = new CountryDTO(city.getCountry().getId(), city.getCountry().getName());
    }

}
