package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequestDTO {

    @NotBlank
    private String modelName;

    private boolean usesElectricity;

    private double consumptionAmount;

    private int propertyId;

    private ArrayList<String> regimes;

    private int minTemp;

    private int maxTemp;

    private double panelSize;

    private double efficiency;

    private double capacity;

    private int portNumber;


//    @NotBlank(message = "Field property type is required")
//    @Pattern(regexp = "HOUSE|APARTMENT", message="Field property type has incorrect value")
//    private String DeviceType;

}
