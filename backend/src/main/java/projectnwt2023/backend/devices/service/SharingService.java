package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.devices.SharedProperty;
import projectnwt2023.backend.devices.repository.SharedPropertyRepository;
import projectnwt2023.backend.devices.service.interfaces.ISharingService;
import projectnwt2023.backend.exceptions.EntityAlreadyExistsException;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SharingService implements ISharingService {

    @Autowired
    private SharedPropertyRepository sharedPropertyRepository;

    @Override
    public ArrayList<SharedProperty> findAllSharedPropertiesByShareWith(AppUser shareWith) {
        return sharedPropertyRepository.findAllByShareWith(shareWith);
    }

    @Override
    public void deleteSharedPropertyById(Long id) {
        sharedPropertyRepository.deleteById(id);
    }

    @Override
    public SharedProperty save(SharedProperty sharedProperty) {

        for (SharedProperty s : sharedPropertyRepository.findAll())
            if (s.getShareWith().getId() == sharedProperty.getShareWith().getId() &&
                s.getProperty().getId() == sharedProperty.getProperty().getId())
                throw new EntityAlreadyExistsException(SharedProperty.class);

        return sharedPropertyRepository.save(sharedProperty);
    }
}
