package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
import projectnwt2023.backend.devices.dto.PyChartDTO;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DeviceService implements IDeviceService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    InfluxDBService influxDBService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired

    IAppUserService appUserService;

    @Override
    public Device save(Device device) {
        Device saved = deviceRepository.save(device);
        saved.setImagePath((String.format("images/device-%s.jpg", saved.getId())));
        return deviceRepository.save(saved);
    }

    @Override
    public Device getById(Long id) {
        Optional<Device> device = deviceRepository.findById(id);

        if (!device.isPresent())
            throw new EntityNotFoundException(Device.class);

        return device.get();
    }

    @Override
    public Device changeDeviceState(Long id, State state) {
        Device device = getById(id);
        if (device.getState() != state) {
            //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        AppUser sender = appUserService.findByEmail(authentication.getName());
            Map<String, String> values = new HashMap<>();
            values.put("device-id", String.valueOf(device.getId()));
            values.put("caller", "kata");
            influxDBService.save("online/offline", state.getNumericValue(), new Date(), values);
        }
        device.setState(state);
        device.setLastHeartbeat(LocalDateTime.now());
        this.simpMessagingTemplate.convertAndSend("/device/" + id + "/state", state.getNumericValue());
        return deviceRepository.save(device);
    }

    @Override
    public ArrayList<Device> getAppliancesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("airConditioner", "ambientSensor", "washingMachine"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public ArrayList<Device> getOutdoorDevicesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("gate", "lamp", "sprinkler"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public ArrayList<Device> getElectricalDevicesByProperty(Long propertyId) {
        ArrayList<String> types = new ArrayList<>(Arrays.asList("battery", "charger", "solarPanel"));
        return deviceRepository.findByTopicInAndPropertyId(types, propertyId);
    }

    @Override
    public Device changeDeviceOnOff(long deviceId, boolean isOn) {
        Device device = getById(deviceId);
        if (device.getState() == State.offline){
            System.out.println("offline je");
            return null;
        }
        device.setDeviceOn(isOn);
        device.setLastHeartbeat(LocalDateTime.now());
        Map<String, String> values = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Optional<AppUser> sender = appUserService.findByEmail(authentication.getName());
            if (!sender.isPresent()) return null;
            values.put("caller", sender.get().getEmail());
        } else {
            values.put("caller", "kata");
        }
        values.put("device-id", String.valueOf(device.getId()));

        influxDBService.save("on/off", isOn ? 1 : 0, new Date(), values);
        return deviceRepository.save(device);
    }

    @Override
    public List<GateEventMeasurement> getDateRangeEvents(Long id, String start, String end) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        try {
            List<GateEventMeasurement> res = influxDBService.findDateRangeEvents(String.valueOf(id), Long.parseLong(start), Long.parseLong(end), "on/off");
            return res;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<GateEventMeasurement> getRecentEvents(Long id) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        return influxDBService.findRecentEvents(id.toString(), "on/off");
    }

    @Override
    public ArrayList<PyChartDTO> getOnlineOfflineTime(Integer deviceId, String start, String end) {
        Optional<Device> device = deviceRepository.findById(Long.valueOf(deviceId));
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Device.class);
        }

        try {
            List<GateEventMeasurement> data = influxDBService.getOnlineOfflineData(deviceId, Long.parseLong(start), Long.parseLong(end));
//            System.out.println(data.size());
            PyChartDTO on = new PyChartDTO("online", 0);
            PyChartDTO off = new PyChartDTO("offline", 0);
            for (int i = 0; i < data.size() - 1; i++) {
                if (data.get(i).getValue().equals("0.0") && data.get(i + 1).getValue().equals("1.0")) {
                    double dateDistance = data.get(i + 1).getTimestamp().getTime() - data.get(i).getTimestamp().getTime();
                    off.setY(off.getY() + dateDistance);
                }
                if (data.get(i).getValue().equals("1.0") && data.get(i + 1).getValue().equals("0.0")) {
                    double dateDistance = data.get(i + 1).getTimestamp().getTime() - data.get(i).getTimestamp().getTime();
                    on.setY(on.getY() + dateDistance);
                }
            }
            if (data.size() == 0) {
                off.setY(100);
            }else{
                if (data.get(data.size()-1).getValue().equals("0.0")) {
                    double dateDistance = (new Date()).getTime() - data.get(data.size()-1).getTimestamp().getTime();
                    off.setY(off.getY() + dateDistance);
                }
                if (data.get(data.size()-1).getValue().equals("1.0")) {
                    double dateDistance = (new Date()).getTime() - data.get(data.size()-1).getTimestamp().getTime();
                    on.setY(on.getY() + dateDistance);
                }
            }

            ArrayList<PyChartDTO> pyChartDTOS = new ArrayList<>();
            pyChartDTOS.add(on);
            pyChartDTOS.add(off);
            return pyChartDTOS;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ArrayList<PyChartDTO> getOnlineOfflinePerTimeUnit(Integer deviceId, String start, String end) {
        Optional<Device> device = deviceRepository.findById(Long.valueOf(deviceId));
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Device.class);
        }

        try {

            long startInSecondsSinceEpoch = Long.parseLong(start);
            long endInSecondsSinceEpoch = Long.parseLong(end);
            List<GateEventMeasurement> data = influxDBService.getOnlineOfflineData(deviceId, startInSecondsSinceEpoch, endInSecondsSinceEpoch);
            System.out.println(data.size());

            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startInSecondsSinceEpoch), ZoneId.systemDefault());
            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endInSecondsSinceEpoch/1000), ZoneId.systemDefault());
            System.out.println("Start time " + startDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("End time " + endDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            Duration duration = Duration.between(startDateTime, endDateTime);
            int hourStep = 1;       // Calculate percentage for every hour
            if (duration.getSeconds() > 60 * 60 * 24) {
                hourStep = 24;      // Calculate percentage every 24 hours
            }

            Map<LocalDateTime, Integer> intervals = new HashMap<>();
            // key: interval start
            // value: online percentage
            LocalDateTime intervalStart = LocalDateTime.of(startDateTime.toLocalDate(), startDateTime.toLocalTime());
            intervals.put(intervalStart, 0);
            do {
                LocalDateTime intervalStartNew = intervalStart.plusHours(hourStep);
                intervals.put(intervalStartNew, 0);
            } while (intervalStart.compareTo(endDateTime) <= 0);

            int i = 0; // data index

            for (Map.Entry<LocalDateTime, Integer> interval : intervals.entrySet()) {
                double dateDistanceToNextState = data.get(i + 1).getTimestamp().getTime() - data.get(i).getTimestamp().getTime();

                if (interval.getKey().isBefore(LocalDateTime.ofInstant(Instant.ofEpochMilli(data.get(i + 1).getTimestamp().getTime()), ZoneId.systemDefault()))) {
                    if (data.get(i).getValue().equals("1.0")) interval.setValue(100);
                    else interval.setValue(0);
                }
                if (data.get(i).getValue().equals("1.0")) {

                }
                i += 1;
            }
            if (data.size() == 0) {
                // get last
            }else{
                if (data.get(data.size()-1).getValue().equals("0.0")) {
                    double dateDistance = (new Date()).getTime() - data.get(data.size()-1).getTimestamp().getTime();
                }
                if (data.get(data.size()-1).getValue().equals("1.0")) {
                    double dateDistance = (new Date()).getTime() - data.get(data.size()-1).getTimestamp().getTime();
                }
            }

            ArrayList<PyChartDTO> pyChartDTOS = new ArrayList<>();
            return pyChartDTOS;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ArrayList<GateEventMeasurement> getOnlineOfflineIntervals(Integer deviceId, String start, String end) {
        Optional<Device> device = deviceRepository.findById(Long.valueOf(deviceId));
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Device.class);
        }

        try {
            List<GateEventMeasurement> data = influxDBService.getOnlineOfflineData(deviceId, Long.parseLong(start), Long.parseLong(end));
            ArrayList<GateEventMeasurement> res = new ArrayList<>();
            for (int i = 0; i < data.size() - 1; i++) {
                res.add(data.get(i));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(data.get(i+1).getTimestamp());
                calendar.add(Calendar.SECOND, -1);
                Date oneSecondBefore = calendar.getTime();
                res.add(new GateEventMeasurement(data.get(i).getName(), data.get(i).getValue(), oneSecondBefore, ""));
            }
            if (data.size() == 0) {
                ArrayList<GateEventMeasurement> lastResult = (ArrayList<GateEventMeasurement>) influxDBService.getOnlineOfflineDataLast(deviceId);
                if (!lastResult.isEmpty()) {
                    res.add(new GateEventMeasurement(lastResult.get(0).getName(), lastResult.get(0).getValue(), Date.from(Instant.ofEpochMilli(Long.parseLong(start))), ""));
                    res.add(new GateEventMeasurement(lastResult.get(0).getName(), lastResult.get(0).getValue(), Date.from(Instant.now()), ""));
                }
            }else{
                res.add(data.get(data.size()-1));
                res.add(new GateEventMeasurement(data.get(data.size()-1).getName(), data.get(data.size()-1).getValue(), Date.from(Instant.now()), ""));

            }
            return res;

        } catch (NumberFormatException e) {
            return null;
        }
    }


}
