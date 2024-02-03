package projectnwt2023.backend.property.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.PyChartDTO;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IPropertyService {

    Property getById(Long id);

    Property add(PropertyRequestDTO property, String username);

    List<City> getCities();

    List<Property> getPropertiesPendingOrAcceptedForUser(String username);
    Page<Property> getPropertiesByStatus(PropertyStatus status, Pageable page);

    Property acceptProperty(Long id);

    Property rejectProperty(Long id, String reason);

    List<Property> getAllPropertyes();

    ArrayList<PyChartDTO> getPychartForCities(Map<City, List<Property>> propertiesByCity, Long start, Long end, String measurement);

    double getElictricityForProperty(Long id, Long start, Long end, String measurement);

    ArrayList<GraphDTO> findPropertyEnergyForDate(CityGraphDTO graphRequestDTO);

    ArrayList<LabeledGraphDTO> findPropertyEnergyByDayForDate(CityGraphDTO graphRequestDTO);


    ArrayList<BarChartDTO> getBarChartForPropertyForYear(Integer propertyId, int year, String measurement);

    ByTimeOfDayDTO getByTimeOfDayForPropertyInRange(Integer propertyId, Long start, Long end);

    ArrayList<PyChartDTO> getPychartByDeviceType(Integer id, Long start, Long end, String s);
}
