package projectnwt2023.backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.property.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {
}
