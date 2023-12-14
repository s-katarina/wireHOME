package projectnwt2023.backend.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Configuration
public class InfluxDbConfiguration {
//    @Autowired
//    private Environment env;
    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Value("${influxdb.org}")
    private String organization;

    @Value("${influxdb.bucket}")
    private String bucket;

//    public InfluxDbConfiguration() {
//        this.url = String.format("http://%s:%s", env.getProperty("influxdb.host"),
//                env.getProperty("influxdb.port"));
//        this.token = env.getProperty("influxdb.token");
//        this.organization = env.getProperty("influxdb.organization");
//        this.bucket = env.getProperty("influxdb.bucket");
//    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getOrganization() {
        return organization;
    }

    public String getBucket() {
        return bucket;
    }


    @Bean
    public InfluxDBClient influxDbClient() {
        return InfluxDBClientFactory.create(this.url, this.token.toCharArray(),
                this.organization, this.bucket);
    }
}