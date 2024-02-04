package projectnwt2023.backend.devices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import projectnwt2023.backend.appUser.dto.AppUserDTO;
import projectnwt2023.backend.devices.SharedDevice;
import projectnwt2023.backend.devices.SharedProperty;
import projectnwt2023.backend.devices.dto.model.DeviceDTO;
import projectnwt2023.backend.property.dto.PropertyResponseDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SharedDeviceDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private AppUserDTO shareWith;
    private DeviceDTO device;

    public SharedDeviceDTO(SharedDevice sharedDevice) {
        this.id = sharedDevice.getId();
        this.shareWith = new AppUserDTO(sharedDevice.getShareWith());
        this.device = new DeviceDTO(sharedDevice.getDevice());
    }

}
