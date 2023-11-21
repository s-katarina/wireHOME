package projectnwt2023.backend.devices;

import lombok.*;
import projectnwt2023.backend.devices.dto.DeviceRequestDTO;
import projectnwt2023.backend.property.Property;

import javax.persistence.*;

import static javax.persistence.InheritanceType.JOINED;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@TableGenerator(name="device_id_generator", table="primary_keys", pkColumnName="key_pk", pkColumnValue="appUser", valueColumnName="value_pk")
@Inheritance(strategy=JOINED)
public abstract class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelName;

    private State state;

    private boolean usesElectricity;  //false ako je na bateriju true ako koristi struju iz distribucije,
                                      // mozda i visak atribut ali neka ga

    private double consumptionAmount; //koliko struje iz distribucije koristi

    @ManyToOne
    private Property property;

//    private String topic;

    private String imagePath;

    public Device(DeviceRequestDTO deviceRequestDTO) {
        this.modelName = deviceRequestDTO.getModelName();
        this.state = State.offline;
        this.usesElectricity = deviceRequestDTO.isUsesElectricity();
        this.consumptionAmount = deviceRequestDTO.getConsumptionAmount();
    }
    //bice i slika
}

