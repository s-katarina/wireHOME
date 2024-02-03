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
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.GraphRequestDTO;
import projectnwt2023.backend.devices.dto.PyChartDTO;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.dto.*;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @GetMapping(value = "/accepted", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ResponseEntity<List<PropertyResponseDTO>> getPropertiesForAdminOverview(@RequestParam Long start,
                                                                            @RequestParam Long end){

        Page<Property> properties = propertyService.getPropertiesByStatus(PropertyStatus.ACCEPTED, Pageable.unpaged());
        List<PropertyResponseDTO> dtos = new ArrayList<>();
        for (Property p : properties.getContent()) {
            double electricity = propertyService.getElictricityForProperty(p.getId(), start, end, "property-electricity");
            double electrodi = propertyService.getElictricityForProperty(p.getId(), start, end, "electrodeposition");
            dtos.add(new PropertyResponseDTO(p, electricity, electrodi));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/byCity", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<List<CityOverviewDTO>> getPropertiesByCity(@RequestParam Long start,
                                                              @RequestParam Long end){
//        System.out.println("parametri " + start + "  " + end);
        Page<Property> properties = propertyService.getPropertiesByStatus(PropertyStatus.ACCEPTED, Pageable.unpaged());

        // Group properties by city using Java streams
        Map<City, List<Property>> propertiesByCity = properties.getContent()
                .stream()
                .collect(Collectors.groupingBy(Property::getCity));

        ArrayList<PyChartDTO> grapgData = propertyService.getPychartForCities(propertiesByCity, start, end, "property-electricity");
        ArrayList<PyChartDTO> grapgData2 = propertyService.getPychartForCities(propertiesByCity, start, end, "electrodeposition");
//        System.out.println("akumulirani podaci " + grapgData);
        // Create a list of PropertyResponseDTO objects for each group
        List<CityOverviewDTO> dtos = propertiesByCity.entrySet().stream()
                .map(entry -> new CityOverviewDTO(new CityDTO(entry.getKey()), entry.getValue().size(), getValueForLabel(grapgData, entry.getKey().getName()), getValueForLabel(grapgData2, entry.getKey().getName())))
                .collect(Collectors.toList());


        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
    @PostMapping(value = "/propertyEnergy", produces = "application/json") // koristi i za elektrodistribuciju i za samu potrosnju
    ResponseEntity<ArrayList<GraphDTO>> getElectroByCity(@RequestBody CityGraphDTO graphRequestDTO){
//        System.out.println("striglo je " + graphRequestDTO);
        ArrayList<GraphDTO> grapgData = propertyService.findPropertyEnergyForDate(graphRequestDTO);
        return new ResponseEntity<>(grapgData, HttpStatus.OK);
    }

    @PostMapping(value = "/propertyByDay", produces = "application/json") // koristi i za elektrodistribuciju i za samu potrosnju
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<ArrayList<LabeledGraphDTO>> getElectroByDay(@RequestBody CityGraphDTO graphRequestDTO){
//        System.out.println("striglo je " + graphRequestDTO);
        ArrayList<LabeledGraphDTO> grapgData = propertyService.findPropertyEnergyByDayForDate(graphRequestDTO);
        return new ResponseEntity<>(grapgData, HttpStatus.OK);
    }


    private double getValueForLabel(ArrayList<PyChartDTO> graphData, String city) {
        for (PyChartDTO pyChartDTO : graphData) {
            if (city.equals(pyChartDTO.getIndexLabel())) {
                // Found the object with label 'abc'
                return pyChartDTO.getY();
            }
        }
        return 0;
    }

    @GetMapping(value = "/byCityChart", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<ArrayList<PyChartDTO>> getPyChartByCity(@RequestParam Long start,
                                                           @RequestParam Long end){

        Page<Property> properties = propertyService.getPropertiesByStatus(PropertyStatus.ACCEPTED, Pageable.unpaged());

        Map<City, List<Property>> propertiesByCity = properties.getContent()
                .stream()
                .collect(Collectors.groupingBy(Property::getCity));


        ArrayList<PyChartDTO> grapgData = propertyService.getPychartForCities(propertiesByCity, start, end, "property-electricity");
        return new ResponseEntity<>(grapgData, HttpStatus.OK);

    }

    @GetMapping(value = "/byDeviceType/{id}", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<ArrayList<PyChartDTO>> getPyChartByDeviceType(@PathVariable Integer id,
                                                                 @RequestParam Long start,
                                                                 @RequestParam Long end){


        ArrayList<PyChartDTO> grapgData = propertyService.getPychartByDeviceType(id, start, end, "energy-maintaining");
        return new ResponseEntity<>(grapgData, HttpStatus.OK);

    }

    @GetMapping(value = "/byMonthProperty/{propertyId}", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<ArrayList<BarChartDTO>> getPyChartByCity(@PathVariable Integer propertyId,
                                                           @RequestParam int year,
                                                            @RequestParam String measurement,
                                                            @RequestParam String whatId){


        ArrayList<BarChartDTO> grapgData = propertyService.getBarChartForPropertyForYear(propertyId, year, measurement, whatId);
        return new ResponseEntity<>(grapgData, HttpStatus.OK);

    }

    @GetMapping(value = "/byTimeOfDay/{propertyId}", produces = "application/json")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('AUTH_USER')")
    ResponseEntity<ByTimeOfDayDTO> getByTimeOfDay(@PathVariable Integer propertyId,
                                                            @RequestParam Long start,
                                                            @RequestParam Long end,
                                                            @RequestParam String whatId){


        ByTimeOfDayDTO timeOfDayDTO = propertyService.getByTimeOfDayForPropertyInRange(propertyId, start, end, whatId);
        return new ResponseEntity<>(timeOfDayDTO, HttpStatus.OK);

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
