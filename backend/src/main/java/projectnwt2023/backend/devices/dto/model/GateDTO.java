package projectnwt2023.backend.devices.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GateDTO extends DeviceDTO {

    private boolean isOpen;
    private boolean isPublic;
    private List<String> licencePlates;

    public GateDTO(Gate gate) {
        this.setId(Math.toIntExact(gate.getId()));
        this.setState(gate.getState() == State.online);
        this.setModelName(gate.getModelName());
        this.setUsesElectricity(gate.isUsesElectricity());
        this.setConsumptionAmount(gate.getConsumptionAmount());
        this.setPropertyId(Math.toIntExact(gate.getProperty().getId()));
        this.setImagePath(gate.getImagePath());
        this.setDeviceType(gate.getTopic());
        this.setOpen(gate.isOpen());
        this.setPublic(gate.isPublic());
        this.setLicencePlates(gate.getLicencePlates());
    }


}
