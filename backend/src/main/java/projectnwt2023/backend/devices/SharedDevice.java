package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.property.Property;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class SharedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "share_with_id")
    private AppUser shareWith;
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

}
