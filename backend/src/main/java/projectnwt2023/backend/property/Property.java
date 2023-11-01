package projectnwt2023.backend.property;

import lombok.*;

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

    private String Address;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

//    private Account propertyOwner;

    private String imagePath;

    private double area;

    private int floorCount;

    private PropertyStatus propertyStatus;

    public Property(PropertyType propertyType, String address, City city, double area, int floorCount,
                    PropertyStatus propertyStatus) {
        this.propertyType = propertyType;
        this.Address = address;
        this.city = city;
        this.area = area;
        this.floorCount = floorCount;
        this.propertyStatus = propertyStatus;
    }
}
