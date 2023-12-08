package projectnwt2023.backend.property;

import lombok.*;
import projectnwt2023.backend.appUser.AppUser;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    private PropertyType propertyType;

    private String address;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne
    @JoinColumn(name = "property_owner_id")
    private AppUser propertyOwner;

    private String imagePath;

    private double area;

    private int floorCount;

    private PropertyStatus propertyStatus;

    public Property(PropertyType propertyType, String address, City city, AppUser propertyOwner,double area, int floorCount,
                    PropertyStatus propertyStatus) {
        this.propertyType = propertyType;
        this.address = address;
        this.city = city;
        this.propertyOwner = propertyOwner;
        this.area = area;
        this.floorCount = floorCount;
        this.propertyStatus = propertyStatus;
    }
}
