package projectnwt2023.backend.property.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.dto.CityDTO;
import projectnwt2023.backend.property.dto.CountryDTO;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;
import projectnwt2023.backend.property.dto.PropertyResponseDTO;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/property")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class PropertyController {

    @Autowired
    IPropertyService propertyService;

    @GetMapping(value = "/{propertyId}", produces = "application/json")
    ResponseEntity<PropertyResponseDTO> getProperty(@PathVariable Integer propertyId){

        Property p = propertyService.getById(propertyId.longValue());

        return new ResponseEntity<>(new PropertyResponseDTO(p), HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<ApiResponse<PropertyResponseDTO>> addProperty(@Valid @RequestBody PropertyRequestDTO dto){

        Property p = propertyService.add(dto);

        ApiResponse<PropertyResponseDTO> response = new ApiResponse<>(200, new PropertyResponseDTO((p)));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/city", produces = "application/json")
    ResponseEntity<List<CityDTO>> getCities(){

        List<City> cities = propertyService.getCities();
        List<CityDTO> dtos = new ArrayList<>();
        for (City c : cities) {
            dtos.add(new CityDTO(c));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "", produces = "application/json")
    ResponseEntity<List<PropertyResponseDTO>> getProperties(){

        List<Property> properties = propertyService.getProperties(0L);
        List<PropertyResponseDTO> dtos = new ArrayList<>();
        for (Property p : properties) {
            dtos.add(new PropertyResponseDTO(p));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

}
