package projectnwt2023.backend.devices.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projectnwt2023.backend.devices.Battery;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.EnergyDTO;
import projectnwt2023.backend.devices.service.InfluxDBService;
import projectnwt2023.backend.devices.service.interfaces.IBatteryService;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;

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

    @Autowired
    IDeviceService deviceService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Scheduled(fixedRate = 60000) // na min
    public void myScheduledTask() {
        ArrayList<EnergyDTO> energies = influxDBService.findLastMinEnergyOuttake();
        HashMap<Integer, Double> propertyEnergy = new HashMap<>();
        if (energies.size() == 0) {
            justSendBatteryState();
        }
        for (EnergyDTO energy:
             energies) {
            int propertyId = energy.getPropertyId();
            System.out.println(energy);
            if (!propertyEnergy.containsKey(propertyId)) {
                propertyEnergy.put(propertyId, 0.0);
            }
            if (isDeviceOnline(energy.getDeviceId())) {
                propertyEnergy.put(propertyId, propertyEnergy.get(propertyId) + energy.getConsumptionAmount() / 60);
            }
        }

        for (Map.Entry<Integer, Double> entry : propertyEnergy.entrySet()) {
            processProperty(entry);
        }


    }

    private void justSendBatteryState() {
        ArrayList<Battery> batteries = batteryService.getAllBatteries();
//        ArrayList<Battery> batteries = batteryService.getOnlineAllBatteries();
        for (Battery battery: batteries) {
            Map<String, String> values = new HashMap<>();
            values.put("device-id", String.valueOf(battery.getId()));
            influxDBService.save("battery", (float) battery.getCurrentFill(), new Date(), values);
        }
    }

    private boolean isDeviceOnline(int deviceId) {
        Device device = deviceService.getById((long) deviceId);
        if (device.getTopic().equals("solarPanel")) return device.isDeviceOn();
        return device.getState() == State.online;
    }

    private void processProperty(Map.Entry<Integer, Double> entry) {
        int propertyId = entry.getKey();
        double aggregatedAmount = entry.getValue();

        Map<String, String> values = new HashMap<>();
        values.put("property-id", String.valueOf(propertyId));
        influxDBService.save("property-electricity", (float) aggregatedAmount, new Date(), values);
        this.simpMessagingTemplate.convertAndSend("/energy/" + propertyId, aggregatedAmount);

        ArrayList<Battery> batteries = batteryService.getBatteriesByPropertyId((long) propertyId);
        //ArrayList<Battery> batteries = batteryService.getOnlineBatteriesByPropertyId((long) propertyId); //TODO samo online baterija

        System.out.println(batteries.size());
        if (batteries.size() == 0) {
            sendToElectroDistibution(propertyId, (float) aggregatedAmount);
            return;
        }
        processBateruesInProperty(propertyId, aggregatedAmount, batteries);
    }

    private void processBateruesInProperty(int propertyId, double aggregatedAmount, ArrayList<Battery> batteries) {
        double perBattery = aggregatedAmount / batteries.size();
        double finalElectisity = 0;
        for(Battery battery: batteries)
        {
            double electrisity = battery.getCurrentFill() + perBattery;
            if (electrisity<0){
                battery.setCurrentFill(0);
                finalElectisity += electrisity;
                //posalji kkoliko je pozajmio
            } else if (electrisity>battery.getCapacity()) {
                battery.setCapacity(battery.getCapacity());
                finalElectisity += electrisity;

                //posalji koliko je poslao
            } else {
                battery.setCapacity(electrisity);
            }
            Map<String, String> values = new HashMap<>();
            values.put("device-id", String.valueOf(battery.getId()));
            influxDBService.save("battery", (float) battery.getCurrentFill(), new Date(), values);
            sendToElectroDistibution(propertyId, (float) finalElectisity);
        }
    }


    private void sendToElectroDistibution(int propertyId, float aggregatedAmount) {
        Map<String, String> values = new HashMap<>();
        values.put("property-id", String.valueOf(propertyId));
        influxDBService.save("electrodeposition", aggregatedAmount, new Date(), values);
    }

}



