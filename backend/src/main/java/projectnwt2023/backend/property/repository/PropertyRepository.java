package projectnwt2023.backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.property.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
