package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.ACInterval;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.RegimeAirConditioner;
import projectnwt2023.backend.devices.repository.ACIntervalRepository;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IAirConditionerService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.exceptions.IntervalNotValidException;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class AirConditionerService implements IAirConditionerService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ACIntervalRepository acIntervalRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        if (isStringMatchingPattern(topic, "airConditioner/\\d+/response")) {
            String payload = (String) message.getPayload();
            String response = payload.split(";")[0];
            Integer deviceId = Integer.valueOf(payload.split(";")[1]);
            simpMessagingTemplate.convertAndSend("/air-conditioner/" + deviceId + "/response", response);

            AirConditioner airConditioner = (AirConditioner) deviceRepository.findById(deviceId.longValue()).get();
            airConditioner.setCurrentAction(response);
            deviceRepository.save(airConditioner);
        }

        if (isStringMatchingPattern(topic, "airConditioner/\\d+/temp")) {
            String payload = (String) message.getPayload();
            String temp = payload.split(";")[0];
            Integer deviceId = Integer.valueOf(payload.split(";")[1]);
            simpMessagingTemplate.convertAndSend("/air-conditioner/" + deviceId + "/temp", temp);

            AirConditioner airConditioner = (AirConditioner) deviceRepository.findById(deviceId.longValue()).get();
            airConditioner.setTemp(Integer.valueOf(temp));
            deviceRepository.save(airConditioner);
        }
    }

    @Override
    public ArrayList<ACInterval> findIntervalsByAirConditioner(AirConditioner airConditioner) {
        return acIntervalRepository.findByAirConditioner(airConditioner);
    }

    @Override
    public ACInterval saveInterval(ACInterval acInterval) {
        String startTime = acInterval.getStartTime();
        String endTime = acInterval.getEndTime();
        Long start = getSecondsFromTime(startTime);
        Long end = getSecondsFromTime(endTime);
        System.out.println(startTime + " " + endTime + " " + start + " " + end);

        ArrayList<ACInterval> intervals = (ArrayList<ACInterval>) acIntervalRepository.findByAirConditioner(acInterval.getAirConditioner());

        Boolean pass = true;

        for (ACInterval interval : intervals) {
            Long iStart = getSecondsFromTime(interval.getStartTime());
            Long iEnd = getSecondsFromTime(interval.getEndTime());
            if (!(
                    (start < iStart && end < iStart) ||
                    (start > iEnd && end > iEnd)
            ))
                pass = false;
        }

        if (!pass)
            throw new IntervalNotValidException(ACInterval.class);

        String action = acInterval.getAction();
        Boolean supported = false;

        for (RegimeAirConditioner regime : acInterval.getAirConditioner().getAvailableRegimes())
            if (regime.toString().contains(action))
                supported = true;

        if (action.contains("temp")) {
            String temp = action.split("#")[1];
            Integer tempNum = Integer.valueOf(temp);
            if (tempNum < acInterval.getAirConditioner().getMinTemp() ||
                tempNum > acInterval.getAirConditioner().getMaxTemp())
                supported = false;
            else
                supported = true;
        }

        if (!supported)
            throw new IntervalNotValidException(ACInterval.class);

        return acIntervalRepository.save(acInterval);
    }

    public Long getSecondsFromTime(String time) {
        String[] tokens = time.split(":");

        Long ret = 0L;
        ret += Long.parseLong(tokens[0]) * 60 * 60;
        ret += Long.parseLong(tokens[1]) * 60;
        ret += Long.parseLong(tokens[2]);

        return ret;
    }

    @Override
    public void deleteIntervalById(Long id) {
        acIntervalRepository.deleteById(id);
    }

}
