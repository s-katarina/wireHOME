package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.*;
import projectnwt2023.backend.devices.repository.ACIntervalRepository;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.repository.WMTaskRepository;
import projectnwt2023.backend.devices.service.interfaces.IWashingMachineService;
import projectnwt2023.backend.exceptions.IntervalNotValidException;

import java.util.ArrayList;
import java.util.Optional;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class WashingMachineService implements IWashingMachineService {

    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private WMTaskRepository wmTaskRepository;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        if (isStringMatchingPattern(topic, "washingMachine/\\d+/response")) {
            String payload = (String) message.getPayload();
            String response = payload.split(";")[0];
            Integer deviceId = Integer.valueOf(payload.split(";")[1]);
            simpMessagingTemplate.convertAndSend("/washing-machine/" + deviceId + "/response", response);

            WashingMachine washingMachine = (WashingMachine) deviceRepository.findById(deviceId.longValue()).get();
            washingMachine.setCurrentAction(response);
            deviceRepository.save(washingMachine);
        }
    }

    @Override
    public ArrayList<WMTask> findWMTaskByWashingMachine(WashingMachine washingMachine) {
        return wmTaskRepository.findByWashingMachine(washingMachine);
    }

    @Override
    public WMTask saveWMTask(WMTask wmTask) {

        String action = wmTask.getAction();
        Boolean supported = false;

        for (RegimeWashingMachine regime : wmTask.getWashingMachine().getAvailableRegimes())
            if (regime.toString().contains(action))
                supported = true;

        if (!supported)
            throw new IntervalNotValidException(WMTask.class);

        return wmTaskRepository.save(wmTask);
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
    public void deleteWMTaskById(Long id) {
        Optional<WMTask> wmTask = wmTaskRepository.findById(id);
        wmTaskRepository.delete(wmTask.get());
    }
}
