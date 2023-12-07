package projectnwt2023.backend.devices.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.dto.TelemetryPayloadDTO;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;

@Configuration
public class Beans {
    @Autowired
    IDeviceService deviceService;

    @Value("${mosquitto.username}")
    private String username;

    @Value("${mosquitto.password}")
    private String mosquttoPassword;

    public MqttPahoClientFactory mqttPahoClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{"tcp://localhost:1883"});
        options.setUserName(username);
        String password = mosquttoPassword;
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel(){
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound(){
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
                mqttPahoClientFactory(), "#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(){
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
                PayloadDTO payloadDTO = getPayload(message);
//                System.out.println(payloadDTO);
                if (topic == null){
                    System.out.println("null je topic");
                }
                else if(topic.equals("KILLED")) {
                    System.out.println("lost connection " + payloadDTO.getDeviceId());
                    deviceService.changeDeviceState((long) payloadDTO.getDeviceId(), State.offline);
                }
                else if(topic.equals("ON")) {
                    deviceService.changeDeviceState((long) payloadDTO.getDeviceId(), State.online);
                }
                else if(topic.equals("OFF")) {
                    deviceService.changeDeviceState((long) payloadDTO.getDeviceId(), State.offline);
                } else if (topic.contains("bulb")) {

                } else if (topic.contains("light-sensor")) {
                    TelemetryPayloadDTO telemetryPayloadDTO = getTelemetryPayload(message);
                }
                System.out.println(message.getPayload());
            }
        };
    }

    private static PayloadDTO getPayload(Message<?> message) {
        Object payload = message.getPayload();
        String jsonPayload = (String) payload;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonPayload, PayloadDTO.class);
        } catch (JsonProcessingException e) {
            return new PayloadDTO();
        }
    }

    private static TelemetryPayloadDTO getTelemetryPayload(Message<?> message) {
        Object payload = message.getPayload();
        String jsonPayload = (String) payload;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonPayload, TelemetryPayloadDTO.class);
        } catch (JsonProcessingException e) {
            return new TelemetryPayloadDTO();
        }
    }

    @Bean
    public MessageChannel mqttOutboundChannel(){
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(){
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqttPahoClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("#");
        return  messageHandler;
    }
}
