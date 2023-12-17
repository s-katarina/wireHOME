package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Battery;

import java.util.ArrayList;

public interface IBatteryService {
    public ArrayList<Battery> getBatteriesByPropertyId(Long propertyId);
    public ArrayList<Battery> getOnlineBatteriesByPropertyId(Long propertyId);

    ArrayList<Battery> getAllBatteries();
    ArrayList<Battery> getOnlineAllBatteries();
}
