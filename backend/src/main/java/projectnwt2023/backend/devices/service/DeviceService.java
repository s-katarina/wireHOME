package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.appUser.service.interfaces.IAppUserService;
import projectnwt2023.backend.devices.Charger;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
import projectnwt2023.backend.devices.dto.Measurement;
import projectnwt2023.backend.devices.dto.PyChartDTO;
import projectnwt2023.backend.devices.dto.ValueTimestampDTO;
import projectnwt2023.backend.devices.repository.ChargerRepository;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Math.abs;

@Service
public class DeviceService implements IDeviceService {


    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    ChargerRepository chargerRepository;

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
//        if (id == 0) return null;
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
    public List<GateEventMeasurement> getDateRangeEvents(Long id, String start, String end, String measurement) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        try {
            List<GateEventMeasurement> res = influxDBService.findDateRangeEvents(String.valueOf(id), Long.parseLong(start), Long.parseLong(end), measurement);
            return res;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<GateEventMeasurement> getRecentEvents(Long id, String measurment) {
        Optional<Device> device = deviceRepository.findById(id);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        return influxDBService.findRecentEvents(id.toString(), measurment);
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
    public List<ValueTimestampDTO> getOnlinePerTimeUnit(Integer deviceId, String start, String end) {
        Optional<Device> device = deviceRepository.findById(Long.valueOf(deviceId));
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Device.class);
        }


        try {
            List<ValueTimestampDTO> ret = new ArrayList<>();

            long startInSecondsSinceEpoch = Long.parseLong(start);
            long endInSecondsSinceEpoch = Long.parseLong(end);

            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startInSecondsSinceEpoch / 1000), ZoneId.systemDefault());
            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endInSecondsSinceEpoch / 1000), ZoneId.systemDefault());
            endDateTime = endDateTime.withMinute(0).withSecond(0).withNano(0);
            startDateTime = startDateTime.withMinute(0).withSecond(0).withNano(0);

            Duration duration = Duration.between(startDateTime, endDateTime);
            String interval = "1h";       // Calculate percentage for every hour
            if (duration.getSeconds() > 60 * 60 * 24) {
                interval = "24h";      // Calculate percentage every 24 hours
                endDateTime = endDateTime.withHour(0);
                startDateTime = startDateTime.withHour(0);
            }
            endDateTime = endDateTime.minusHours(interval.equals("1h") ? 1 : 24);
//            System.out.println("Start time " + startDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
//            System.out.println("End time " + endDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Measurements grouped in tables by window (hour)
            List<List<Measurement>> data = influxDBService.getOnlineOfflinePerTimeUnit(deviceId.toString(), startInSecondsSinceEpoch, endInSecondsSinceEpoch, interval);

            Map<Date, Double> intervals = new HashMap<>();
            // key: interval start
            // value: online percentage
            LocalDateTime intervalStart = LocalDateTime.of(startDateTime.toLocalDate(), startDateTime.toLocalTime());
            intervalStart = intervalStart.minusHours(interval.equals("1h") ? 1 : 24);
            int windowIndex = 0;
            double lastOnlineValue = 1.0;
            do {
                if (data.size() == 0) {
                    return ret;
                }
                // Interval is:
                intervalStart = intervalStart.plusHours(interval.equals("1h") ? 1 : 24);
                Date intervalTime = java.sql.Date.from((intervalStart.atZone(ZoneId.systemDefault()).toInstant()));
                double onlineAmount = 0;
                // Window is :
                List<Measurement> window = data.get(windowIndex);
                // Window hour is same as interval hour
                if ( window.size() > 0 &&
                        ((interval.equals("1h") && window.get(0).getTimestamp().getHour() == intervalStart.getHour())
                             || (interval.equals("24h") && window.get(0).getTimestamp().getDayOfMonth() == intervalStart.getDayOfMonth()))) {
//                    System.out.println("Window hour is same as interval hour: " + window.get(0).getTimestamp().getHour() + " & " + intervalStart.getHour());
//                    System.out.println("Window day is same as interval hour: " + window.get(0).getTimestamp().getDayOfMonth() + " & " + intervalStart.getDayOfMonth());
                    // calculate percentage

                    if (lastOnlineValue == 1.0) {

                        double secondsDistance = (Duration.between((window.get(0).getTimestamp()).atZone(ZoneId.systemDefault()).toInstant(), intervalStart.atZone(ZoneId.systemDefault()).toInstant())).getSeconds();
                        if (interval.equals("24h")) {
                            LocalDateTime startInterval = window.get(0).getTimestamp().withHour(intervalStart.getHour());
                            secondsDistance = (Duration.between((window.get(0).getTimestamp()).atZone(ZoneId.systemDefault()).toInstant(), startInterval.atZone(ZoneId.systemDefault()).toInstant())).getSeconds();
                        }
                        onlineAmount += abs(secondsDistance);
//                        System.out.println("At start, Last online value is 1, add online amount , " + secondsDistance);
//                        System.out.println("First value is at" + window.get(0).getTimestamp().getHour() + " " + window.get(0).getTimestamp().getMinute());
//                        System.out.println("Start interval hour is , " + intervalStart.getHour() + " " + intervalStart.getMinute());

                    }
                    for (int i = 0; i < window.size()-1 ; i++) {
                        if (window.get(i).getValue() == 1.0  && window.get(i + 1).getValue() == 0.0) {
                            double secondsDistance = (Duration.between((window.get(0).getTimestamp()).atZone(ZoneId.systemDefault()).toInstant(), (window.get(1).getTimestamp()).atZone(ZoneId.systemDefault()).toInstant())).getSeconds();
                            onlineAmount += secondsDistance;
//                            System.out.println("Value online:  " + window.get(i).getValue());
//                            System.out.println("Adding online amount for intervals in window, " + secondsDistance);
                        }
                    }

                    lastOnlineValue = window.get(window.size()-1).getValue();
                    if (lastOnlineValue == 1.0) {
                        LocalDateTime endInterval = intervalStart.plusHours(interval.equals("1h") ? 1 : 24);
                        double secondsDistance = (Duration.between((window.get(window.size()-1).getTimestamp()).atZone(ZoneId.systemDefault()).toInstant(), endInterval.atZone(ZoneId.systemDefault()).toInstant())).getSeconds();
                        onlineAmount += abs(secondsDistance);
//                        System.out.println("At end, Last online value is 1, add online amount , " + secondsDistance);
//                        System.out.println("last value is at" + window.get(0).getTimestamp().getHour() + " " + window.get(0).getTimestamp().getMinute());
//                        System.out.println("end interval hour is , " + endInterval.getHour() + " " + endInterval.getMinute());

                    }
                    if (windowIndex + 1 < data.size()) {
                        windowIndex += 1;
                    }
                // Window hour is missing, fill with last value
                } else {
//                    System.out.println("Window hour missing, interval hour: " + intervalStart.getHour());
//                    System.out.println("Window day missing, interval day: " + intervalStart.getDayOfMonth());
                    if (lastOnlineValue == 1.0) {
                        intervals.put(intervalTime, 1.0);
                    } else {
                        intervals.put(intervalTime, 0.0);
                    }
                    continue;
                }
                double secondsAmount = (Duration.between((intervalStart.atZone(ZoneId.systemDefault()).toInstant()), (intervalStart.plusHours(interval.equals("1h") ? 1 : 24)).atZone(ZoneId.systemDefault()).toInstant())).getSeconds();
//                System.out.println("Seconds amount: " + secondsAmount);
//                System.out.println("Online amount: " + onlineAmount);
                double percentage = onlineAmount / secondsAmount;
                if (onlineAmount > secondsAmount) {
                    percentage = 1.0;   // Scale to 100% in case of small amount of greatness caused by time calculations
                }
                intervals.put(intervalTime, percentage);

            } while (intervalStart.compareTo(endDateTime) < 0);

            // Transform results

            for (Map.Entry<Date, Double> entry : intervals.entrySet()) {
                ret.add(new ValueTimestampDTO(entry.getValue().toString(), Long.toString(entry.getKey().getTime())));
            }
//            System.out.println("Number of intervals in ret: " + ret.size());
            return ret;
        } catch (NumberFormatException | DateTimeException e) {
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
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(data.get(data.size()-1).getTimestamp());
//                calendar.add(Calendar.SECOND, -1);
//                Date oneSecondBefore = calendar.getTime();
                res.add(new GateEventMeasurement(data.get(data.size()-1).getName(), data.get(data.size()-1).getValue(), data.get(data.size()-1).getTimestamp(), ""));
            }
            return res;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ArrayList<Device> findAllByOwnerOfProperty(AppUser owner) {
        ArrayList<Device> ret = new ArrayList<>();
        for (Device device : deviceRepository.findAll())
            if (device.getProperty().getPropertyOwner().getId() == owner.getId())
                ret.add(device);
        return ret;
    }

    @Override
    public void preprocessDevices() {
        for (Device device : deviceRepository.findByState(State.online)){
            device.setState(State.offline);
            deviceRepository.save(device);
        }
    }

    @Override
    public void preprocessCharger() {
        for (Charger device : chargerRepository.findByTopic("charger")){
            device.setAvailablePortNumber(device.getPortNumber());
            deviceRepository.save(device);
        }
    }


}
