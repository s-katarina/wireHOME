package projectnwt2023.backend.devices.dto;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequestDTO {

    @NotBlank
    private String modelName;

    private boolean usesElectricity;

    @NotNull
    @Max(value = 10000, message = "Field consumptionAmount must be less than or equal to 10000")
    @Min(value = 0, message = "Field consumptionAmount must be 0 or greater")
    private double consumptionAmount;

    @NotNull
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
