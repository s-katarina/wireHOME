package projectnwt2023.backend.property.service.interfaces;

import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;

public interface IPropertyService {

    Property getById(Long id);

    Property add(PropertyRequestDTO property);

}
