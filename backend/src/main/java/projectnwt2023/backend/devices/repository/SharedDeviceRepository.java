package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.devices.SharedDevice;
import projectnwt2023.backend.devices.SharedProperty;

import java.util.ArrayList;

public interface SharedDeviceRepository extends JpaRepository<SharedDevice, Long> {

    ArrayList<SharedDevice> findAllByShareWith(AppUser shareWith);

}
