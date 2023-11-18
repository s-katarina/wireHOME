package projectnwt2023.backend.property.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.exceptions.UserForbiddenOperationException;
import projectnwt2023.backend.mail.MailService;
import projectnwt2023.backend.property.City;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;
import projectnwt2023.backend.property.PropertyType;
import projectnwt2023.backend.property.dto.PropertyRequestDTO;
import projectnwt2023.backend.property.repository.CityRepository;
import projectnwt2023.backend.property.repository.PropertyRepository;
import projectnwt2023.backend.property.service.interfaces.IPropertyService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService implements IPropertyService {

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    MailService mailService;

    @Autowired
    IAppUserService appUserService;

    @Override
    public Property getById(Long id) {
        Optional<Property> property = propertyRepository.findById(id);

        if (!property.isPresent())
            throw new EntityNotFoundException(Property.class);

        return property.get();
    }

    @Override
    public Property add(PropertyRequestDTO dto, String username) {

        Optional<AppUser> user = appUserService.findByEmail(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException(AppUser.class);
        }

        Optional<City> city = cityRepository.findById(dto.getCityId().longValue());
        if (!city.isPresent()) {
            throw new EntityNotFoundException(City.class);
        }

        Property p = new Property(PropertyType.valueOf(dto.getPropertyType()), dto.getAddress(), city.get(),
                user.get(), dto.getArea(), dto.getFloorCount(), PropertyStatus.PENDING);
        return propertyRepository.save(p);
    }

    @Override
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @Override
    public List<Property> getPropertiesPendingOrAcceptedForUser(String username) {

        Optional<AppUser> user = appUserService.findByEmail(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException(AppUser.class);
        }

        List<PropertyStatus> statuses = new ArrayList<>();
        statuses.add(PropertyStatus.PENDING);
        statuses.add(PropertyStatus.ACCEPTED);
        return propertyRepository.findByPropertyOwner_IdAndPropertyStatusIn(user.get().getId(), statuses);
    }

    @Override
    public Page<Property> getPropertiesByStatus(PropertyStatus status, Pageable page) {
        return propertyRepository.findByPropertyStatus(PropertyStatus.PENDING, page);
    }

    public Property acceptProperty(Long id) {
        Optional<Property> p = propertyRepository.findById(id);
        if (!p.isPresent())
            throw new EntityNotFoundException(Property.class);

        Property property = p.get();
        if (property.getPropertyStatus().equals(PropertyStatus.PENDING))
            property.setPropertyStatus(PropertyStatus.ACCEPTED);
        else throw new UserForbiddenOperationException();

        this.sendMailApproved(property);

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

        this.sendMailRejected(property, reason);

        return propertyRepository.save(property);
    }

    public String sendMailApproved(Property p){
        try {
            String title = "Property approval";
            String content = String.format("Your %s property at %s, %s has been approved. \uD83C\uDF89 \n" +
                            "Get ready to register devices for your smart Wire HOME experience!",
                    p.getPropertyType().toString().toLowerCase(),
                    p.getAddress(),
                    p.getCity().getName());
            return mailService.sendTextEmail(p.getPropertyOwner().getEmail(), title, content);
        } catch (IOException ex) {
            return "";
        }
    }

    public String sendMailRejected(Property p, String reason){
        try {
            String title = "Property rejection";
            String content = String.format("We regret to inform you that after careful consideration, " +
                            "your registration request for %s property at %s, %s " +
                            "has been declined for the following reason:\n%s" +
                            "\nWe apologize for the inconvenience and hope you consider trying again in the future " +
                            "after revising your request." +
                            "\nBest regards,\nAdmin",
                    p.getPropertyType().toString().toLowerCase(),
                    p.getAddress(),
                    p.getCity().getName(),
                    reason
                    );
            return mailService.sendTextEmail(p.getPropertyOwner().getEmail(), title, content);
        } catch (IOException ex) {
            return "";
        }
    }

}

