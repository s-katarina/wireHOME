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
    @Max(value = 1000000, message = "Field consumptionAmount must be less than or equal to 10000")
    @Min(value = 0, message = "Field consumptionAmount must be 0 or greater")
    private double consumptionAmount;

    @NotNull
    private int propertyId;

    private ArrayList<String> regimes;

    @Max(value = 60, message = "Field minTemp must be less than or equal to 10000")
    @Min(value = 0, message = "Field minTemp must be 0 or greater")
    private int minTemp;

    @Max(value = 60, message = "Field maxTemp must be less than or equal to 10000")
    @Min(value = 0, message = "Field maxTemp must be 0 or greater")
    private int maxTemp;

    @Max(value = 10000, message = "Field panelSize must be less than or equal to 10000")
    @Min(value = 0, message = "Field panelSize must be 0 or greater")
    private double panelSize;

    @Max(value = 100, message = "Field efficiency must be less than or equal to 10000")
    @Min(value = 0, message = "Field efficiency must be 0 or greater")
    private double efficiency;

    @Max(value = 1000000, message = "Field capacity must be less than or equal to 10000")
    @Min(value = 0, message = "Field capacity must be 0 or greater")
    private double capacity;

    @Max(value = 100, message = "Field portNumber must be less than or equal to 10000")
    @Min(value = 0, message = "Field portNumber must be 0 or greater")
    private int portNumber;


}
