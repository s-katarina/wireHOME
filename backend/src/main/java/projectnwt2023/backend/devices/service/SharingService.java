package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.devices.SharedDevice;
import projectnwt2023.backend.devices.SharedProperty;
import projectnwt2023.backend.devices.repository.SharedDeviceRepository;
import projectnwt2023.backend.devices.repository.SharedPropertyRepository;
import projectnwt2023.backend.devices.service.interfaces.ISharingService;
import projectnwt2023.backend.exceptions.EntityAlreadyExistsException;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SharingService implements ISharingService {

    @Autowired
    private SharedPropertyRepository sharedPropertyRepository;
    @Autowired
    private SharedDeviceRepository sharedDeviceRepository;

    @Override
    public ArrayList<SharedProperty> findAllSharedPropertiesByShareWith(AppUser shareWith) {
        return sharedPropertyRepository.findAllByShareWith(shareWith);
    }

    @Override
    public ArrayList<SharedProperty> findAllSharedPropertiesByOwner(AppUser owner) {
        ArrayList<SharedProperty> ret = new ArrayList<>();
        for (SharedProperty sharedProperty : sharedPropertyRepository.findAll())
            if (sharedProperty.getProperty().getPropertyOwner().getId() == owner.getId())
                ret.add(sharedProperty);
        return ret;
    }

    @Override
    public void deleteSharedPropertyById(Long id) {
        sharedPropertyRepository.deleteById(id);
    }

    @Override
    public SharedProperty saveSharedProperty(SharedProperty sharedProperty) {

        for (SharedProperty s : sharedPropertyRepository.findAll())
            if (s.getShareWith().getId() == sharedProperty.getShareWith().getId() &&
                s.getProperty().getId() == sharedProperty.getProperty().getId())
                throw new EntityAlreadyExistsException(SharedProperty.class);

        return sharedPropertyRepository.save(sharedProperty);
    }

    @Override
    public ArrayList<SharedDevice> findAllSharedDevicesByShareWith(AppUser shareWith) {
        return sharedDeviceRepository.findAllByShareWith(shareWith);
    }

    @Override
    public void deleteSharedDeviceById(Long id) {
        sharedDeviceRepository.deleteById(id);
    }

    @Override
    public SharedDevice saveSharedDevice(SharedDevice sharedDevice) {

        for (SharedDevice s : sharedDeviceRepository.findAll())
            if (s.getShareWith().getId() == sharedDevice.getShareWith().getId() &&
                    s.getDevice().getId() == sharedDevice.getDevice().getId())
                throw new EntityAlreadyExistsException(SharedProperty.class);

        return sharedDeviceRepository.save(sharedDevice);
    }
}
