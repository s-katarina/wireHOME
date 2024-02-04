package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PayloadWithCallerDTO extends PayloadDTO {
    private String caller;
}
