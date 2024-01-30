package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.devices.SharedDevice;
import projectnwt2023.backend.devices.SharedProperty;

import java.util.ArrayList;

public interface ISharingService {

    ArrayList<SharedProperty> findAllSharedPropertiesByShareWith(AppUser shareWith);
    void deleteSharedPropertyById(Long id);
    SharedProperty saveSharedProperty(SharedProperty sharedProperty);

    ArrayList<SharedDevice> findAllSharedDevicesByShareWith(AppUser shareWith);
    void deleteSharedDeviceById(Long id);
    SharedDevice saveSharedDevice(SharedDevice sharedDevice);

}
