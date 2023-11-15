package projectnwt2023.backend.devices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projectnwt2023.backend.devices.dto.MessageDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;

@RestController
@RequestMapping("/api/mqtt")
//@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class CommunicationController {
    @Autowired
    Gateway mqttGateway;

    @PostMapping("/simpleMsg")
    public ResponseEntity<?> publish(@RequestBody MessageDTO mqttMessage){
        try {
            mqttGateway.sendToMqtt(mqttMessage.getMessage(), mqttMessage.getTopic());
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            System.out.println("greeeska");
            e.printStackTrace();
            return ResponseEntity.ok("Nije hteo da posaljke");
        }


    }
}










