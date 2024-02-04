package projectnwt2023.backend.devices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.devices.SharedProperty;

import java.util.ArrayList;

public interface SharedPropertyRepository extends JpaRepository<SharedProperty, Long> {

    ArrayList<SharedProperty> findAllByShareWith(AppUser shareWith);

}
