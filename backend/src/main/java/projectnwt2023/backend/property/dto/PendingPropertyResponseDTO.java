package projectnwt2023.backend.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.property.Property;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingPropertyResponseDTO {

    private Long id;

    private String propertyType;

    private String address;

    private CityDTO city;

//    private AccountDTO propertyOwner;

    private String imagePath;

    private double area;

    private int floorCount;

    private String propertyStatus;

    public PendingPropertyResponseDTO(Property property) {
        this.id = property.getId();
        this.propertyType = property.getPropertyType().toString();
        this.address = property.getAddress();
        this.city = new CityDTO(property.getCity());
//        this.propertyOwner = new AccountDTO(property.getPropertyOwner());
        this.area = property.getArea();
        this.floorCount = property.getFloorCount();
        this.propertyStatus = property.getPropertyStatus().toString();
    }

}
