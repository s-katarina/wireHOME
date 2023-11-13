package projectnwt2023.backend.property.service.interfaces;

import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;

import java.util.List;

public interface IPropertyService {

    Property getById(Long id);

    Property add(PropertyRequestDTO property);

    List<City> getCities();

    List<Property> getProperties(Long userId);

}
