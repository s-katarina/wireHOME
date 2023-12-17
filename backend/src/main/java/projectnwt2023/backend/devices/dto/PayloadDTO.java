package projectnwt2023.backend.devices.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PayloadDTO {
    private int deviceId;
    private String usedFor;
   private String timeStamp;
}
