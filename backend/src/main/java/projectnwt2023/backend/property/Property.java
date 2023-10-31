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


}
