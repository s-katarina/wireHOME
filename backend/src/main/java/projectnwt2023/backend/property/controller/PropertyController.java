package projectnwt2023.backend.property.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.dto.*;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/property")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
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
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    ResponseEntity<ApiResponse<PropertyResponseDTO>> addProperty(@Valid @RequestBody PropertyRequestDTO dto){

        String username = this.extractUsername();
        Property p = propertyService.add(dto, username);

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
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    ResponseEntity<List<PropertyResponseDTO>> getPropertiesForUser(){

        List<Property> properties = propertyService.getPropertiesPendingOrAcceptedForUser(this.extractUsername());
        List<PropertyResponseDTO> dtos = new ArrayList<>();
        for (Property p : properties) {
            dtos.add(new PropertyResponseDTO(p));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/pending", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ResponseEntity<List<PropertyResponseDTO>> getPendingProperties(){

        Page<Property> properties = propertyService.getPropertiesByStatus(PropertyStatus.PENDING, Pageable.unpaged());
        List<PropertyResponseDTO> dtos = new ArrayList<>();
        for (Property p : properties.getContent()) {
            dtos.add(new PropertyResponseDTO(p));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PutMapping(value = "/pending/accept/{propertyId}", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ResponseEntity<PropertyResponseDTO> acceptPending(@PathVariable Integer propertyId){

        return new ResponseEntity<>(new PropertyResponseDTO(propertyService.acceptProperty(propertyId.longValue())), HttpStatus.OK);
    }

    @PutMapping(value = "/pending/reject/{propertyId}", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ResponseEntity<PropertyResponseDTO> rejectPending(@PathVariable Integer propertyId,
                                                      @RequestBody PropertyRejectionRequestDTO rejectionRequest){

        return new ResponseEntity<>(new PropertyResponseDTO(propertyService.rejectProperty(propertyId.longValue(), rejectionRequest.getRejectionReason())), HttpStatus.OK);
    }

    private String extractUsername() {
        // Extract user who sent request
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUsername();
    }

}
