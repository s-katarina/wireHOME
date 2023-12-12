package projectnwt2023.backend.devices.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class BatteryScheduler {

    @Scheduled(fixedRate = 60000) // na min
    public void myScheduledTask() {
        // Your task logic goes here
        System.out.println("Scheduled task executed!");
    }


}
