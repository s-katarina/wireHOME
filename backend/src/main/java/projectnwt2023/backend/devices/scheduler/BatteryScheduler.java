package projectnwt2023.backend.devices.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.dto.EnergyDTO;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IBatteryService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class BatteryScheduler {
    @Autowired
    InfluxDBService influxDBService;

    @Autowired
    IBatteryService batteryService;

    @Scheduled(fixedRate = 60000) // na min
    public void myScheduledTask() {
        ArrayList<EnergyDTO> energies = influxDBService.findLastMinEnergyOuttake();
        HashMap<Integer, Double> propertyEnergy = new HashMap<>();
        for (EnergyDTO energy:
             energies) {
            int propertyId = energy.getPropertyId();
            System.out.println(energy);
            if (!propertyEnergy.containsKey(propertyId)) {
                propertyEnergy.put(propertyId, 0.0);
            }
            propertyEnergy.put(propertyId, propertyEnergy.get(propertyId) + energy.getConsumptionAmount());
        }

        for (Map.Entry<Integer, Double> entry : propertyEnergy.entrySet()) {
            processProperty(entry);
        }


    }

    private void processProperty(Map.Entry<Integer, Double> entry) {
        int propertyId = entry.getKey();
        double aggregatedAmount = entry.getValue();

        Map<String, String> values = new HashMap<>();
        values.put("property-id", String.valueOf(propertyId));
        influxDBService.save("property-electricity", (float) aggregatedAmount, new Date(), values);

        ArrayList<Battery> batteries = batteryService.getBatteriesByPropertyId((long) propertyId);
        System.out.println(batteries.size());
        if (batteries.size() == 0) {
            sendToElectroDistibution(propertyId, (float) aggregatedAmount);
            return;
        }
        processBateruesInProperty(propertyId, aggregatedAmount, batteries);
    }

    private void processBateruesInProperty(int propertyId, double aggregatedAmount, ArrayList<Battery> batteries) {
        double perBattery = aggregatedAmount / batteries.size();
        for(Battery battery: batteries)
        {
            double electrisity = battery.getCurrentFill() + perBattery;
            if (electrisity<0){
                battery.setCurrentFill(0);
                //posalji kkoliko je pozajmio
                sendToElectroDistibution(propertyId, (float) electrisity);
            } else if (electrisity>battery.getCapacity()) {
                battery.setCapacity(battery.getCapacity());
                sendToElectroDistibution(propertyId, (float) electrisity);
                //posalji koliko je poslao
            } else {
                battery.setCapacity(electrisity);
                //ne salje se nista distribuciji
            }
        }
    }


    private void sendToElectroDistibution(int propertyId, float aggregatedAmount) {
        Map<String, String> values = new HashMap<>();
        values.put("property-id", String.valueOf(propertyId));
        influxDBService.save("electrodeposition", aggregatedAmount, new Date(), values);
    }

}



