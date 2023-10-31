package projectnwt2023.backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.property.City;

public interface CityRepository extends JpaRepository<City, Long> {
}
