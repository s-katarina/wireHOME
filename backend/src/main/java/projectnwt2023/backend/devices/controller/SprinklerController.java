package projectnwt2023.backend.devices.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.Sprinkler;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.dto.model.GateDTO;
import projectnwt2023.backend.devices.dto.model.SprinklerDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.ISprinklerService;
import projectnwt2023.backend.helper.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprinkler")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SprinklerController {

    @Autowired
    IDeviceService deviceService;

    @Autowired
    IAppUserService appUserService;

    @Autowired
    Gateway mqttGateway;

    @Autowired
    ISprinklerService sprinklerService;

    @GetMapping(value = "/{deviceId}", produces = "application/json")
    ResponseEntity<SprinklerDTO> getSprinkler(@PathVariable Integer deviceId){

        Sprinkler device = (Sprinkler) deviceService.getById(deviceId.longValue());
        return new ResponseEntity<>(new SprinklerDTO(device), HttpStatus.OK);
    }

    @PutMapping(value = "/{deviceId}/on", produces = "application/json")
    ResponseEntity<?> setOn(@PathVariable Integer deviceId,
                              @RequestParam("val") Boolean newOn){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<AppUser> user = appUserService.findByEmail(userDetails.getUsername());
        if (!user.isPresent()) return (ResponseEntity<?>) ResponseEntity.badRequest();

        try {
            ObjectMapper mapper = new ObjectMapper();
            SetOnPayload payload = new SetOnPayload(newOn, user.get().getEmail());
            String jsonString = mapper.writeValueAsString(payload);
            mqttGateway.sendToMqtt(jsonString, "sprinkler/"+String.valueOf(deviceId)+"/on/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Sprinkler on/off set failed");
        }
    }

    @PutMapping(value = "/{deviceId}/schedule", produces = "application/json")
    ResponseEntity<?> setSchedule(@PathVariable Integer deviceId,
                                  @RequestBody SprinklerScheduleDTO scheduleDTO){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<AppUser> user = appUserService.findByEmail(userDetails.getUsername());
        if (!user.isPresent()) return (ResponseEntity<?>) ResponseEntity.badRequest();
        try {
            ObjectMapper mapper = new ObjectMapper();
            SetSchedulePayload payload = new SetSchedulePayload(scheduleDTO.getStartHour(), scheduleDTO.getEndHour(), scheduleDTO.getWeekdays(), user.get().getEmail(), null);
            String jsonString = mapper.writeValueAsString(payload);
            mqttGateway.sendToMqtt(jsonString, "sprinkler/"+String.valueOf(deviceId)+"/schedule/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Sprinkler on/off set failed");
        }
    }

    @PutMapping(value = "/{deviceId}/schedule/off", produces = "application/json")
    ResponseEntity<?> turnOffSchedule(@PathVariable Integer deviceId){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<AppUser> user = appUserService.findByEmail(userDetails.getUsername());
        if (!user.isPresent()) return (ResponseEntity<?>) ResponseEntity.badRequest();
        try {
            ObjectMapper mapper = new ObjectMapper();
            SetSchedulePayload payload = new SetSchedulePayload(null, null, new int[0], user.get().getEmail(), true);
            String jsonString = mapper.writeValueAsString(payload);
            mqttGateway.sendToMqtt(jsonString, "sprinkler/"+String.valueOf(deviceId)+"/schedule/set");
            return ResponseEntity.ok("Success");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Sprinkler on/off set failed");
        }
    }

    @GetMapping(value = "/{deviceId}/recent", produces = "application/json")
    ResponseEntity<ApiResponse<List<SprinklerCommandDTO>>> getRecentCommands(@PathVariable Integer deviceId){

        List<SprinklerCommandMeasurement> res = sprinklerService.getRecentCommands(Long.valueOf(deviceId));
        List<SprinklerCommandDTO> ret = new ArrayList<>();
        for (SprinklerCommandMeasurement measurement : res) {
            String caller = measurement.getCaller();
            Optional<AppUser> user = appUserService.findByEmail(measurement.getCaller());
            if (user.isPresent()) {
                caller = user.get().getName() + " " + user.get().getLastName();
            }
            ret.add(new SprinklerCommandDTO(caller, measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
        }
        return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
    }

    @GetMapping(value = "/{deviceId}/range", produces = "application/json")
    ResponseEntity<ApiResponse<List<SprinklerCommandDTO>>> getRangeCommands(@PathVariable Integer deviceId,
                                                                       @RequestParam String start,
                                                                       @RequestParam String end) {

        List<SprinklerCommandMeasurement> res = sprinklerService.getDateRangeCommands(Long.valueOf(deviceId), start, end);
        List<SprinklerCommandDTO> ret = new ArrayList<>();
        if (res != null) {
            for (SprinklerCommandMeasurement measurement : res) {
                String caller = measurement.getCaller();
                Optional<AppUser> user = appUserService.findByEmail(measurement.getCaller());
                if (user.isPresent()) {
                    caller = user.get().getName() + " " + user.get().getLastName();
                }
                ret.add(new SprinklerCommandDTO(caller, measurement.getValue(), String.valueOf(measurement.getTimestamp().getTime())));
            }
            return new ResponseEntity<>(new ApiResponse<>(200, ret), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>(400, new ArrayList<>()), HttpStatus.BAD_REQUEST);
    }

}

@Getter
@Setter
@AllArgsConstructor
class SetOnPayload {
    @JsonProperty("newOn")
    private boolean newOn;

    @JsonProperty("caller")
    private String caller;
}

@Getter
@Setter
@AllArgsConstructor
class SetSchedulePayload {
    @JsonProperty("startHour")
    private Integer startHour;
    @JsonProperty("endHour")
    private Integer endHour;
    @JsonProperty("weekdays")
    private int[] weekdays;
    @JsonProperty("caller")
    private String caller;
    @JsonProperty("off")
    private Boolean off;
}
