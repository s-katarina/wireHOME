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
                        fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime())));
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
        System.out.println("graphRequestDTO.getFrom()");
        System.out.println(graphRequestDTO.getFrom());
        System.out.println(graphRequestDTO.getMeasurement());

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
                System.out.println(value);
            }
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
        System.out.println(deviceId);
        System.out.println(startTimestamp);
        System.out.println(endTimestamp);
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
                Double value = fluxRecord.getValueByKey("value") == null ? null : (Double) fluxRecord.getValueByKey("value");
                Date timestamp = fluxRecord.getTime() == null ? null : Date.from(fluxRecord.getTime());
                result.add(new Measurement(measurementName, value, timestamp));
            }
        }
        return result;
    }

    public List<Measurement> findDateRangeLightSensor(String deviceId, Long startTimestamp, Long endTimestamp) {
        System.out.println(deviceId);
        System.out.println(startTimestamp);
        System.out.println(endTimestamp);
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %d, stop: %d)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                this.bucket, startTimestamp/1000, endTimestamp/1000, "light-sensor", deviceId);
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
        System.out.println(deviceId);
        System.out.println(startTimestamp);
        System.out.println(endTimestamp);
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
        System.out.println(deviceId);
        System.out.println(startTimestamp);
        System.out.println(endTimestamp);
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

        AmbientSensorDateValueDTO ret = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                ret.getValues().add((Double) fluxRecord.getValueByKey("_value"));
                ret.getDates().add(String.valueOf(fluxRecord.getValueByKey("_time")));

//                System.out.println(fluxRecord.getValues());
            }
        }

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

        AmbientSensorDateValueDTO ret = new AmbientSensorDateValueDTO(new ArrayList<>(), new ArrayList<>());

        QueryApi queryApi = influxDbClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {

                ret.getValues().add((Double) fluxRecord.getValueByKey("_value"));
                ret.getDates().add(String.valueOf(fluxRecord.getValueByKey("_time")));

//                System.out.println(fluxRecord.getValues());
            }
        }

        return ret;
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
