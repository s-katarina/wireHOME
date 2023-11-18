package projectnwt2023.backend.property.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;

import java.util.List;

public interface IPropertyService {

    Property getById(Long id);

    Property add(PropertyRequestDTO property, String username);

    List<City> getCities();

    List<Property> getProperties(Long userId);
    List<Property> getPropertiesPendingOrAcceptedForUser(Long userId);
    Page<Property> getPropertiesByStatus(PropertyStatus status, Pageable page);

    Property acceptProperty(Long id);

    Property rejectProperty(Long id, String reason);

    String sendMailApproved(Property property);
    String sendMailRejected(Property property, String reason);

}
