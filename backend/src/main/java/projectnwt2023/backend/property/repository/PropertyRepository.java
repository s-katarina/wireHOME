package projectnwt2023.backend.property.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import projectnwt2023.backend.property.Property;
import projectnwt2023.backend.property.PropertyStatus;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByPropertyStatus(PropertyStatus status, Pageable pageable);
//    List<Property> findByPropertyOwner_IdAndPropertyStatusIn(Long id, List<PropertyStatus> statuses);

}
