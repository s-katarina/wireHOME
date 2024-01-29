package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.GateEventMeasurement;
import projectnwt2023.backend.devices.dto.PyChartDTO;

import java.util.ArrayList;
import java.util.List;

public interface IDeviceService {
    public Device save(Device device);
    public Device getById(Long id);

    public Device changeDeviceState(Long id, State state);

    public ArrayList<Device> getAppliancesByProperty(Long propertyId);
    public ArrayList<Device> getOutdoorDevicesByProperty(Long propertyId);
    public ArrayList<Device> getElectricalDevicesByProperty(Long propertyId);

    public Device changeDeviceOnOff(long deviceId, boolean b);

    List<GateEventMeasurement> getDateRangeEvents(Long valueOf, String start, String end);

    List<GateEventMeasurement> getRecentEvents(Long valueOf);

    ArrayList<PyChartDTO> getOnlineOfflineTime(Integer deviceId, String start, String end);

    ArrayList<PyChartDTO> getOnlineOfflinePerTimeUnit(Integer deviceId, String start, String end);

    ArrayList<GateEventMeasurement> getOnlineOfflineIntervals(Integer deviceId, String start, String end);
}
