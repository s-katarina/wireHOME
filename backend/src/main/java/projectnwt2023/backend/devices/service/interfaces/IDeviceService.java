package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Device;

import java.util.ArrayList;

public interface IDeviceService {
    public Device save(Device device);
    public Device getById(Long id);

    public ArrayList<Device> getAppliancesByProperty(Long propertyId);
    public ArrayList<Device> getOutdoorDevicesByProperty(Long propertyId);
    public ArrayList<Device> getElectricalDevicesByProperty(Long propertyId);

}
