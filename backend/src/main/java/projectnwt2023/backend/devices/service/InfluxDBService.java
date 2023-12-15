package projectnwt2023.backend.devices.service;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.influx.InfluxDbProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.dto.EnergyDTO;
import projectnwt2023.backend.devices.dto.GraphDTO;
import projectnwt2023.backend.devices.dto.GraphRequestDTO;
import projectnwt2023.backend.devices.dto.Measurement;
import projectnwt2023.backend.helper.Constants;
import projectnwt2023.backend.helper.InfluxDbConfiguration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        DateTimeFormatter inputFormatter = Constants.dateTimeFormatterWithSeconds;

//        String fromString = graphRequestDTO.getFrom();
//        String toString = getStringDateForINflux(graphRequestDTO.getTo(), inputFormatter);
        System.out.println("graphRequestDTO.getFrom()");
        System.out.println(graphRequestDTO.getFrom());
        System.out.println(graphRequestDTO.getFrom().getClass());

        // Print the result
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device-id\"] == \"%s\")" +                 // where measurement name (_measurement) equals value measurementName
                        "|> sort(columns: [\"_time\"], desc: false)", this.bucket,
                graphRequestDTO.getFrom(), graphRequestDTO.getTo(),
                        "energy-maintaining", graphRequestDTO.getId());
        ArrayList<EnergyDTO> energies = queryForEnergy(fluxQuery);
        ArrayList<GraphDTO> grapfValue = new ArrayList<>();
        for (EnergyDTO energyDTO: energies){
            grapfValue.add(new GraphDTO(energyDTO.getTimestamp().getTime(), energyDTO.getConsumptionAmount()));
        }
        return grapfValue;
    }

    @NotNull
    private static String getStringDateForINflux(String date, DateTimeFormatter inputFormatter) {
        LocalDateTime localDateStart = LocalDateTime.parse(date, inputFormatter);
        // Convert LocalDateTime to ZonedDateTime with ZoneOffset.UTC
        ZonedDateTime zonedDateTime = localDateStart.atZone(ZoneOffset.UTC);
        // Format the ZonedDateTime in the desired format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return zonedDateTime.format(outputFormatter);
    }
}
