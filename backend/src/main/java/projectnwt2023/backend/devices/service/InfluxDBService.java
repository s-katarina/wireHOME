package projectnwt2023.backend.devices.service;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Measurement.BulbOnOffMeasurement;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.property.dto.BarChartDTO;
import projectnwt2023.backend.property.dto.ByTimeOfDayDTO;
import projectnwt2023.backend.property.dto.CityGraphDTO;
import projectnwt2023.backend.property.dto.LabeledGraphDTO;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InfluxDBService {

    private final InfluxDBClient influxDbClient;
    private final String bucket;

    @Autowired
    public InfluxDBService(InfluxDBClient influxDbClient, Environment env) {
        this.influxDbClient = influxDbClient;
        this.bucket = env.getProperty("influxdb.bucket");
    }

    private List<Measurement> query(String fluxQuery) {
        List<Measurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                result.add(new Measurement(fluxRecord.getMeasurement(),
                        fluxRecord.getValue() == null ? 0 : ((double) fluxRecord.getValue()),
                        fluxRecord.getTime() == null ? null : LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault())));
            }
        }
        return result;
    }

    private ArrayList<EnergyDTO> queryForEnergy(String fluxQuery) {
        ArrayList<EnergyDTO> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Map<String, Object> tagValues = fluxRecord.getValues();

                // tags
                String deviceId = tagValues.containsKey("device-id") ? tagValues.get("device-id").toString() : null;
                String propertyId = tagValues.containsKey("property-id") ? tagValues.get("property-id").toString() : null;

                result.add(new EnergyDTO(fluxRecord.getMeasurement(),
                        (deviceId != null) ? Integer.parseInt(deviceId) : 0,
                        (propertyId != null) ? Integer.parseInt(propertyId) : 0,
                        fluxRecord.getValue() == null ? 0 : ((double) fluxRecord.getValue()),
                        fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime())));
            }
        }
        return result;
    }

    public ArrayList<EnergyDTO> findLastMinEnergyOuttake() {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -1m, stop: now())" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")", this.bucket, "energy-maintaining");
        return this.queryForEnergy(fluxQuery);
    }

    public List<Measurement> findLastTenByName(String measurementName) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -24h, stop: now())" +  // from bucket "measurements" get rows (data) from last 24h
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")" +     // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: true)" +               // sort rows by timestamp (_time) descending
                        "|> limit(n: 10)", this.bucket, measurementName);           // get last 10 values (limit(n : 10))
        return this.query(fluxQuery);
    }

    public List<Measurement> findLastByName(String measurementName) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -24h, stop: now())" +          // from bucket "measurements" get rows (data) from last 24h
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")" +     // where measurement name (_measurement) equals value measurementName
                        "|> last()", this.bucket, measurementName);                 // get last value
        return this.query(fluxQuery);
    }

    public List<Measurement> findAggregatedDataByName(String measurementName) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -2h, stop: now())" +           // from bucket "measurements" get rows (data) from last 2h
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")" +     // where measurement name (_measurement) equals value measurementName
                        "|> aggregateWindow(every: 1m, fn: mean)",                  // aggregate (group) data based on 1 minute intervals, for each group calculate mean value
                this.bucket, measurementName);
        return this.query(fluxQuery);
    }

    public List<Measurement> findDeviationFromMeanByName(String measurementName) {
        String meanValueQuery = String.format(
                "meanValue = " +                                                        // into variable meanValue, put result of following query:
                        "from(bucket:\"%s\") |> range(start: -24h, stop: now())" +      // from bucket "measurements" get rows (data) from last 24h
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")" +         // where measurement name (_measurement) equals value measurementName
                        "|> mean()" +                                                   // calculate mean value for filtered records, presented as table with one record
                        "|> findRecord(fn: (key) => true, idx: 0)",                     // find record with index 0 from result table (from previous line)
                this.bucket, measurementName);
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -24h, stop: now())" +                      // from bucket "measurements" get rows (data) from last 24h
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\")" +                 // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: true)" +                           // sort rows by timestamp (_time) descending
                        "|> limit(n: 10)" +                                                     // get last 10 values (limit(n : 10))
                        "|> map(fn: (r) => ({r with _value: r._value - meanValue._value}))",    // from each record subtract previously calculated mean value
                this.bucket, measurementName);
        return this.query(meanValueQuery + "\n" + fluxQuery);
    }

    public void save(String name, float value, Date timestamp, Map<String, String> tags) {
        try {
            WriteApiBlocking writeApi = this.influxDbClient.getWriteApiBlocking();

            Point point = Point.measurement(name)
                    .addTags(tags)
                    .addField("value", value)
                    .time(timestamp.toInstant(), WritePrecision.MS);

            writeApi.writePoint(point);

        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }

    public ArrayList<GraphDTO> findDeviceEnergyForDate(GraphRequestDTO graphRequestDTO) {
//        System.out.println("graphRequestDTO.getF


        // Print the result
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +                 // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: false)", this.bucket,
                graphRequestDTO.getFrom(), graphRequestDTO.getTo(),
                graphRequestDTO.getMeasurement(), graphRequestDTO.getId());
        ArrayList<EnergyDTO> energies = queryForEnergy(fluxQuery);
        ArrayList<GraphDTO> grapfValue = new ArrayList<>();
        for (EnergyDTO energyDTO: energies){
            grapfValue.add(new GraphDTO(energyDTO.getTimestamp().getTime(), energyDTO.getConsumptionAmount()));
        }
        return grapfValue;
    }


    private List<GateEventMeasurement> queryGate(String fluxQuery) {
        List<GateEventMeasurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                String measurementName = fluxRecord.getMeasurement();
                String value = fluxRecord.getValueByKey("value") == null ? null : fluxRecord.getValueByKey("value").toString();
                String caller = fluxRecord.getValueByKey("caller") == null ? null : fluxRecord.getValueByKey("caller").toString();
                Date timestamp = fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime());
                result.add(new GateEventMeasurement(measurementName, value, timestamp, caller));
//                System.out.println(value);
            }
        }
        return result;
    }

    public List<List<Measurement>> getOnlineOfflinePerTimeUnit(String deviceId,  long startTime, long endTime, String interval) {
        List<List<Measurement>> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\")\n" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n" +
                        "|> window(every: %s)\n" +
                        "|> fill(usePrevious: true)\n" +
                        "|> sort(columns: [\"_time\"], desc: false)",
                this.bucket, startTime / 1000, endTime / 1000, "online/offline", deviceId, interval);
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            List<Measurement> tableResults = new ArrayList<>();
            for (FluxRecord fluxRecord : records) {
                String measurementName = fluxRecord.getMeasurement();
                Double value = fluxRecord.getValueByKey("value") == null ? null : (Double) fluxRecord.getValueByKey("value");
                LocalDateTime timestamp = fluxRecord.getTime() == null ? null : LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                tableResults.add(new Measurement(measurementName, value == null ? 0 : value, timestamp));
            }
            result.add(tableResults);
        }
        return result;
    }

    public List<GateEventMeasurement> findRecentGateEvents(String deviceId) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -2h, stop: now())" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, "gate-event", deviceId);
        return this.queryGate(fluxQuery);
    }

    public List<GateEventMeasurement> findDateRangeGateEvents(String deviceId, Long startTimestamp, Long endTimestamp) {
//        System.out.println(deviceId);
//        System.out.println(startTimestamp);
//        System.out.println(endTimestamp);
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, "gate-event", deviceId);
        return this.queryGate(fluxQuery);
    }


    public ArrayList<GraphDTO> findPropertyEnergyForDate(GraphRequestDTO graphRequestDTO) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"property-id\"] == \"%s\")" +                 // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: false)", this.bucket,
                graphRequestDTO.getFrom(), graphRequestDTO.getTo(),
                graphRequestDTO.getMeasurement(), graphRequestDTO.getId());
        ArrayList<EnergyDTO> energies = queryForEnergy(fluxQuery);
        ArrayList<GraphDTO> grapfValue = new ArrayList<>();
        for (EnergyDTO energyDTO: energies){
            grapfValue.add(new GraphDTO(energyDTO.getTimestamp().getTime(), energyDTO.getConsumptionAmount()));
        }
        return grapfValue;
    }

    private List<Measurement> queryLightSensor(String fluxQuery) {
        List<Measurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                String measurementName = fluxRecord.getMeasurement();
                Double value = fluxRecord.getValueByKey("_value") == null ? null : (Double) fluxRecord.getValueByKey("_value");
                LocalDateTime timestamp = fluxRecord.getTime() == null ? null : LocalDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault());
                result.add(new Measurement(measurementName, value == null ? 0 : value, timestamp));
            }
        }
        return result;
    }

    public List<Measurement> findDateRangeLightSensor(String deviceId, Long startTimestamp, Long endTimestamp) {
        LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimestamp / 1000), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTimestamp / 1000), ZoneId.systemDefault());
        endDateTime = endDateTime.withMinute(0).withSecond(0).withNano(0);
        startDateTime = startDateTime.withMinute(0).withSecond(0).withNano(0);

        Duration duration = Duration.between(startDateTime, endDateTime);
        String interval = "1m";       // Aggregate window
        if (duration.getSeconds() > 60 * 60 * 24) {
            interval = "30m";      // Calculate percentage every 24 hours
        }
        if (duration.getSeconds() > 60 * 60 * 24 * 7) {
            interval = "1h";      // Calculate percentage every 24 hours
        }
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> aggregateWindow(every: %s, fn: mean, createEmpty: false)",
//                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, "light-sensor", deviceId, interval);
        return this.queryLightSensor(fluxQuery);
    }

    private List<BulbOnOffMeasurement> queryBulbOn(String fluxQuery) {
        List<BulbOnOffMeasurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Date timestamp = fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime());
                Double value = fluxRecord.getValueByKey("value") == null ? null : (Double) fluxRecord.getValueByKey("value");
                result.add(new BulbOnOffMeasurement(String.valueOf(value), String.valueOf(timestamp.getTime())));
            }
        }

        return result;

    }

    public List<BulbOnOffMeasurement> findDateRangeBulb(String deviceId, Long startTimestamp, Long endTimestamp) {
//        System.out.println(deviceId);
//        System.out.println(startTimestamp);
//        System.out.println(endTimestamp);
        String fluxQuery = String.format(
                "from(bucket: \"%s\")" +
                        "  |> range(start: %d, stop: %d)" +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"bulb\" and r[\"device-id\"] == \"%s\")" +
                        "  |> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, deviceId);
        return this.queryBulbOn(fluxQuery);
    }

    public List<GateEventMeasurement> findRecentEvents(String deviceId, String measurement) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -2h, stop: now())" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, measurement, deviceId);
        return this.queryGate(fluxQuery);
    }

    public List<GateEventMeasurement> findDateRangeEvents(String deviceId, Long startTimestamp, Long endTimestamp,  String measurement) {
//        System.out.println(deviceId);
//        System.out.println(startTimestamp);
//        System.out.println(endTimestamp);
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, measurement, deviceId);
        return this.queryGate(fluxQuery);
    }


    public List<GateEventMeasurement> getOnlineOfflineData(Integer deviceId, Long startTimestamp, Long endTimestamp) {

        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")" +                 // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: false)",
                this.bucket, startTimestamp / 1000, endTimestamp / 1000, "online/offline", deviceId);
        return this.queryGate(fluxQuery);

    }

    public List<GateEventMeasurement> getOnlineOfflineDataLast(Integer deviceId) {

        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -1y)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> last()",
                this.bucket, "online/offline", deviceId);
        return this.queryGate(fluxQuery);

    }

    public AmbientSensorDateValueDTO getAllHumForAmbientSensorInPeriod(int id, Long from, Long to) {

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %d, stop: %d) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"hum\") " +
                        "|> filter(fn: (r) => r[\"device-id\"] == \"%d\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> yield(name: \"value\")",
                bucket, from, to, id
        );

        AmbientSensorDateValueDTO dto = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                dto.getValues().add((Double) fluxRecord.getValueByKey("_value"));
                dto.getDates().add(String.valueOf(fluxRecord.getValueByKey("_time")));

//                System.out.println(fluxRecord.getValues());
            }
        }

        Integer step = (int) Math.ceil((double) dto.getValues().size() / 70);
        if (step == 0)
            step = 1;
//        System.out.println(step);
        AmbientSensorDateValueDTO ret = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        for (int i = 0; i < dto.getValues().size(); i += step) {
            ret.getDates().add(dto.getDates().get(i));
            ret.getValues().add(dto.getValues().get(i));
        }
//        System.out.println(ret.getValues().size());

        return ret;
    }

    public ArrayList<AirConditionerActionDTO> getAllAirConditionerActions(int id) {

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: 0) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"airEvent\") " +
                        "|> filter(fn: (r) => r[\"device-id\"] == \"%d\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> yield(name: \"value\")",
                bucket, id
        );

        ArrayList<AirConditionerActionDTO> ret = new ArrayList<>();

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                AirConditionerActionDTO dto = new AirConditionerActionDTO();

                dto.setAction(String.valueOf(fluxRecord.getValueByKey("_value")));
                dto.setDate(String.valueOf(fluxRecord.getValueByKey("_time")));
                dto.setEmail(String.valueOf(fluxRecord.getValueByKey("email")));

                ret.add(dto);

//                System.out.println(fluxRecord.getValues());
            }
        }

        return ret;
    }

    public ArrayList<AirConditionerActionDTO> getAllWashingMachineActions(int id) {

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: 0) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"washingEvent\") " +
                        "|> filter(fn: (r) => r[\"device-id\"] == \"%d\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> yield(name: \"value\")",
                bucket, id
        );

        ArrayList<AirConditionerActionDTO> ret = new ArrayList<>();

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                AirConditionerActionDTO dto = new AirConditionerActionDTO();

                dto.setAction(String.valueOf(fluxRecord.getValueByKey("_value")));
                dto.setDate(String.valueOf(fluxRecord.getValueByKey("_time")));
                dto.setEmail(String.valueOf(fluxRecord.getValueByKey("email")));

                ret.add(dto);

//                System.out.println(fluxRecord.getValues());
            }
        }

        return ret;
    }


    public AmbientSensorDateValueDTO getAllTempForAmbientSensorInPeriod(int id, Long from, Long to) {

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %d, stop: %d) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"temp\") " +
                        "|> filter(fn: (r) => r[\"device-id\"] == \"%d\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> yield(name: \"value\")",
                bucket, from, to, id
        );

        AmbientSensorDateValueDTO dto = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                dto.getValues().add((Double) fluxRecord.getValueByKey("_value"));
                dto.getDates().add(String.valueOf(fluxRecord.getValueByKey("_time")));

//                System.out.println(fluxRecord.getValues());
            }
        }

//        System.out.println(dto.getDates().size());
//        System.out.println(dto.getValues().size());

        Integer step = (int) Math.ceil((double) dto.getValues().size() / 70);
        if (step == 0)
            step = 1;
        System.out.println(step);
        AmbientSensorDateValueDTO ret = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        for (int i = 0; i < dto.getValues().size(); i += step) {
            ret.getDates().add(dto.getDates().get(i));
            ret.getValues().add(dto.getValues().get(i));
        }
        System.out.println(ret.getValues().size());
        return ret;
    }

    public double getElectricityForPropertyInRange(Long propertyId, Long start, Long end, String measurement) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"property-id\"] == \"%s\")" +
                        "|> sum(column: \"_value\")", // Summing the values
                this.bucket, start, end, measurement, propertyId);
        //abs
        return this.returnSum(fluxQuery);
    }

    private double returnSum(String fluxQuery) {
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
//            System.out.println("Zelim sumu svih property potrosnje " + fluxTable);
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

//                System.out.println(fluxRecord.getValues());
                Object valueObj = fluxRecord.getValueByKey("_value");

                // Add a null check before unboxing
                if (valueObj != null) {
                    return (double) valueObj;
                }
            }
        }
        return 0;
    }

    public ArrayList<GraphDTO> findCityEnergyForDate(CityGraphDTO graphRequestDTO) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"city-id\"] == \"%s\")" +
                        "|> aggregateWindow(every: 10m, fn: sum, createEmpty: true)" + // Aggregate over 1-minute intervals using mean
                        "|> sort(columns: [\"_time\"], desc: false)",
                this.bucket,
                graphRequestDTO.getFrom(), graphRequestDTO.getTo(),
                graphRequestDTO.getMeasurement(), graphRequestDTO.getId()
        );
//        System.out.println(fluxQuery);
        return queryForGraph(fluxQuery);
    }

    private ArrayList<GraphDTO> queryForGraph(String fluxQuery) {
        ArrayList<GraphDTO> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Map<String, Object> tagValues = fluxRecord.getValues();

                // tags

                result.add(new GraphDTO(fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime()).getTime(),
                        fluxRecord.getValue() == null ? 0 : ((double) fluxRecord.getValue())));
            }
        }
        return result;
    }

    public ArrayList<BarChartDTO> findPropertyEnergyByMonth(Integer propertyId, int year, String measurement) {
        ArrayList<BarChartDTO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            // Calculate the start and end timestamps for each month
            Date startOfMonth = Date.from(YearMonth.of(year, month).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfMonth = Date.from(YearMonth.of(year, month).atEndOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant());
//            System.out.println("mesec " + month);
//            System.out.println(startOfMonth);
//            System.out.println(endOfMonth);
//            System.out.println(startOfMonth.getTime());

            String fluxQuery = String.format(
                    "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                            "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"property-id\"] == \"%s\")" +
                            "|> sum(column: \"_value\")", // Summing the values
                    this.bucket, startOfMonth.getTime()/1000, endOfMonth.getTime()/1000, measurement, propertyId);

//            System.out.println(fluxQuery);
            double sum = this.returnSum(fluxQuery);

            BarChartDTO barChartDTO = new BarChartDTO(getMonthName(month), Math.abs(sum));
            result.add(barChartDTO);
        }

        return result;
    }

    private String getMonthName(int month) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        LocalDate firstDayOfMonth = LocalDate.of(2024, Month.of(month), 1);  // 2000 is an arbitrary year, you can use any year
        return monthFormat.format(Date.from(firstDayOfMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    public ByTimeOfDayDTO getByTimeOfDayForPropertyInRange(Integer propertyId, Long start, Long end) {
        ByTimeOfDayDTO electro = naIzmakuSam(propertyId, start, end, "property-electricity");
        ByTimeOfDayDTO dist = naIzmakuSam(propertyId, start, end, "electrodeposition");
        electro.setDayDist(dist.getDayElec());
        electro.setNightDist(dist.getNightElec());
        return electro;
    }

    private ByTimeOfDayDTO naIzmakuSam(Integer propertyId, Long start, Long end, String measurement){
        long interval = 12 * 60 * 60 * 1000;  // 12 hours in milliseconds
        double firstInterval = 0.0;
        double secondInterval = 0.0;
        boolean isFirst = true;
        for (long currentTime = start * 1000; currentTime <= end * 1000; currentTime += interval) {
            long currentIntervalEnd = currentTime + interval;

            String fluxQuery = String.format(
                    "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                            "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"property-id\"] == \"%s\")" +
                            "|> sum(column: \"_value\")", // Summing the values
                    this.bucket, currentTime/1000, currentIntervalEnd/1000, measurement, propertyId);

            double sum = this.returnSum(fluxQuery);

            if (isFirst) {
                firstInterval += sum;
            } else {
                secondInterval += sum;
            }

            isFirst = !isFirst;
        }
        return new ByTimeOfDayDTO(firstInterval, secondInterval, 0.0, 0.0);
    }

    public ArrayList<LabeledGraphDTO> findPropertyEnergyByDayForDate(CityGraphDTO graphRequestDTO) {
        Long start = graphRequestDTO.getFrom() * 1000;
        Instant instant = Instant.ofEpochMilli(start);
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        // Set the time to midnight (00:00:00)
        LocalDateTime startOfDay = localDate.atStartOfDay();
        start = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        ArrayList<LabeledGraphDTO> labeledGraph = new ArrayList<>();
        long interval = 24 * 60 * 60 * 1000;  // 24 hours in milliseconds
        long end = start + interval * 6;  // 7 days


        for (long currentTime = start; currentTime <= end; currentTime += interval) {
            long currentIntervalEnd = currentTime + interval;
            Date date = new Date(currentTime);
//            System.out.println(date);

            date = new Date(currentIntervalEnd);
//            System.out.println(date);

            String fluxQuery = String.format(
                    "from(bucket:\"%s\") |> range(start: %s, stop: %s)" +
                            "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"property-id\"] == \"%s\")" +
                            "|> aggregateWindow(every: 1h, fn: sum, createEmpty: true)" +
                            "|> sort(columns: [\"_time\"], desc: false)",
                    this.bucket,
                    currentTime/1000, currentIntervalEnd/1000,
                    graphRequestDTO.getMeasurement(), graphRequestDTO.getId()
            );

//            System.out.println(fluxQuery);

            instant = Instant.ofEpochMilli(currentTime);
            localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            // Get the day of the week as a string
            String dayOfWeekString = localDate.getDayOfWeek().name();
            labeledGraph.add(new LabeledGraphDTO(dayOfWeekString, queryForGraph(fluxQuery)));
//            System.out.println(queryForGraph(fluxQuery).size());
        }
//        System.out.println(labeledGraph);
        return labeledGraph;
    }

    private List<SprinklerCommandMeasurement> querySprinklerCommand(String fluxQuery) {
        List<SprinklerCommandMeasurement> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                String measurementName = fluxRecord.getMeasurement();
                String value = fluxRecord.getValueByKey("value") == null ? null : fluxRecord.getValueByKey("value").toString();
                String caller = fluxRecord.getValueByKey("caller") == null ? null : fluxRecord.getValueByKey("caller").toString();
                Date timestamp = fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime());
                result.add(new SprinklerCommandMeasurement(measurementName, value, timestamp, caller));
            }
        }
        return result;
    }

    public List<SprinklerCommandMeasurement> findRecentSprinklerCommands(String deviceId) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -2h, stop: now())" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, "sprinkler-command", deviceId);
        return this.querySprinklerCommand(fluxQuery);
    }

    public List<SprinklerCommandMeasurement> findDateRangeSprinklerCommands(String deviceId, Long startTimestamp, Long endTimestamp) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, "sprinkler-command", deviceId);
        return this.querySprinklerCommand(fluxQuery);
    }

}
