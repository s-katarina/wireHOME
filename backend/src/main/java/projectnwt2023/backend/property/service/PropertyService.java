package projectnwt2023.backend.property.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.exceptions.UserForbiddenOperationException;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.PropertyType;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;
import projectnwt2023.backend.property.repository.CityRepository;
import projectnwt2023.backend.property.repository.PropertyRepository;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService implements IPropertyService {

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    CityRepository cityRepository;


    @Override
    public Property getById(Long id) {
        Optional<Property> property = propertyRepository.findById(id);

        if (!property.isPresent())
            throw new EntityNotFoundException(Property.class);

        return property.get();
    }

    @Override
    public Property add(PropertyRequestDTO dto) {

        Optional<City> city = cityRepository.findById(dto.getCityId().longValue());
        if (!city.isPresent()) {
            throw new EntityNotFoundException(City.class);
        }

        Property p = new Property(PropertyType.valueOf(dto.getPropertyType()), dto.getAddress(), city.get(),
                dto.getArea(), dto.getFloorCount(), PropertyStatus.PENDING);
        return propertyRepository.save(p);
    }

    @Override
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @Override
    public List<Property> getProperties(Long userId) {
        return propertyRepository.findAll();
    }

    @Override
    public List<Property> getPropertiesPendingOrAcceptedForUser(Long userId) {
        ArrayList<PropertyStatus> statuses = new ArrayList<>();
        statuses.add(PropertyStatus.PENDING);
        statuses.add(PropertyStatus.ACCEPTED);
//        return propertyRepository.findByPropertyOwnerIdAndPropertyStatusIn(userId, statuses);
        return propertyRepository.findAll();
    }

    @Override
    public Page<Property> getPropertiesByStatus(PropertyStatus status, Pageable page) {
        return propertyRepository.findByPropertyStatus(PropertyStatus.PENDING, page);
    }

    public Property changePropertyStatus(Long id, PropertyStatus status) {
        Optional<Property> p = propertyRepository.findById(id);
        if (!p.isPresent())
            throw new EntityNotFoundException(Property.class);

        Property property = p.get();
        if (property.getPropertyStatus().equals(PropertyStatus.PENDING))
            property.setPropertyStatus(status);
        else throw new UserForbiddenOperationException();

        return propertyRepository.save(property);
    }

    public Property rejectProperty(Long id, String reason) {
        Optional<Property> p = propertyRepository.findById(id);
        if (!p.isPresent())
            throw new EntityNotFoundException(Property.class);
        System.out.println(reason);
        Property property = p.get();
        if (property.getPropertyStatus().equals(PropertyStatus.PENDING))
            property.setPropertyStatus(PropertyStatus.REJECTED);
        else throw new UserForbiddenOperationException();

        return propertyRepository.save(property);
    }


}

